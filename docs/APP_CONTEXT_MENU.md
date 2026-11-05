# App Context Menu

Phase 1.2 adds a long-press context menu for installed applications. This document describes the
behavior that exists on the `migration-orca` branch, the guards around destructive actions, and what
remains unverified.

> Phase status: implemented, committed as `157e446`, unit-tested, and **owner-reported physically
> validated** (12/12 scenarios on a Google Pixel 8a, 2026-11-04). The report has not been
> independently reproduced.

## 1. Entry points

The same menu is used from every app surface:

| Surface | File |
| --- | --- |
| Home workspace grid | `feature/home/HomeScreen.kt` |
| Dock | `feature/home/LuminousDock.kt` |
| App drawer | `feature/appdrawer/AppDrawerScreen.kt` |
| Folder modal | `feature/folders/FolderModalScreen.kt` |
| Shared icon primitive | `core/designsystem/LivingAppIcon.kt` |

`LivingAppIcon` reports the long press to its caller with the icon's position. The caller asks
`LauncherViewModel` to open the overlay:

```kotlin
LauncherOverlay.AppContextMenu(app = app, anchorPosition = offset)
```

Because the overlay lives in the single sealed `LauncherOverlay` state, opening the context menu
closes the drawer, search, settings, or folder surface that launched it.

## 2. UI composition

`feature/common/AppContextMenuScreen.kt` renders the menu.

- A Compose `Popup` anchored at `Alignment.TopStart`.
- Fixed width of `250.dp`.
- Maximum height of `screenHeight - 32.dp`, floored at `120.dp`.
- Vertical scroll for long action lists.
- 20dp rounded shape, 94% opacity surface, primary-tinted 1dp border.
- 48dp menu-item rhythm through Material 3 `DropdownMenuItem`.
- Test tags: `app_context_menu` and `context_menu_action_<action-id>`.

### Placement

The anchor is clamped to a 16dp screen margin:

- If `x + menuWidth` would exceed the right margin, the menu shifts left.
- If `y + estimatedHeight` would exceed the bottom margin, the menu flips above the anchor.
- The result is coerced inside the screen bounds.

The height is estimated as `actions.size * 48 + 24` dp, capped by the maximum height. This is an
estimate, not a measured layout; a long list that scrolls can still place a few pixels differently
than the estimate. The owner's Pixel 8a validation covered screen bounds and scrolling. Landscape,
wide/foldable, and RTL placement remain unvalidated.

### Dismissal

| Trigger | Behavior |
| --- | --- |
| Tap outside | `PopupProperties.dismissOnClickOutside` calls `dismissAppContextMenu()` |
| Back press | `BackHandler` and `dismissOnBackPress` dismiss |
| Action selected | The action handler dismisses where appropriate |
| Package disappears | ViewModel dismisses the stale menu after refresh |
| Home intent | `onHomeIntentReceived()` preserves or clears overlay according to the Home flow |

## 3. Action policy

`domain/contextmenu/AppContextMenu.kt` contains a pure `AppContextMenuPolicy` object. The UI never
decides which actions are legal.

### Order

1. **App info**
2. **Discovered shortcuts** for the exact package, in discovery order
3. **Pause app** — only if `isPauseSupported()` returns `true`
4. **Widgets** — only if `isWidgetSupportAvailable()` returns `true`
5. **Remove from launcher**
6. **Uninstall** — only if the app is not a system app

### Availability rules

```text
app.isInstalledApp == false        -> no actions at all
app.isSystemApp == true            -> no Uninstall
isPauseSupported() == false        -> no Pause app
isWidgetSupportAvailable() == false-> no Widgets
```

`isPauseSupported()` and `isWidgetSupportAvailable()` currently return `false`. The branches are
reserved integration points, not fake features. Do not surface these actions until a supported
public Android integration exists.

### Why pause is unavailable

Android's Digital Wellbeing does not expose a public per-app "pause" API that a normal launcher
process can call. There is no supported intent or permission for it. Implementing pause would
require a system-privileged integration that this project explicitly avoids.

### Why widgets are unavailable

No `AppWidgetHost` exists yet. Displaying a **Widgets** row without a real picker and host would be
a fake menu entry. Phase 1.3 covers the real implementation and its ADR.

## 4. Action behavior

### App info

- Calls `AppRepository.openAppDetails(packageName)`.
- Uses `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` with a `package:` URI.
- Dismisses the launcher menu.
- Returns `false` if the intent cannot be resolved or started.

### Remove from launcher

- Calls `WorkspaceRepository.removeAppFromWorkspace(packageName)`.
- Removes top-level workspace rows and folder memberships for that package.
- Deletes folder rows that become empty.
- **Never** uninstalls the Android package.
- Dismisses the menu.

This distinction is enforced in the domain model and tested. A user who taps "Remove from launcher"
must still find the app installed in the app drawer.

### Uninstall

- Policy omits the action for system apps.
- `PackageManagerAppRepository.uninstallApp` re-checks installation and system-app status before
  building an `Intent.ACTION_DELETE`.
- Android owns the confirmation dialog. The launcher cannot and does not bypass it.
- Returns `false` instead of starting an intent when the target is missing, is a system app, or the
  intent cannot be handled.
- If the intent starts, the menu dismisses. The actual removal happens asynchronously and arrives
  later as a package-removed broadcast.

The repository guard exists so that a future caller cannot bypass the UI policy and uninstall a
system package.

### Shortcuts

- Discovery uses `LauncherApps.getShortcuts` on API 25+.
- Requires the launcher to be the current shortcut host
  (`hasShortcutHostPermission()`).
- Returns an empty list on older versions, missing permission, exceptions, or no shortcuts.
- Only shortcuts whose `packageName` matches the selected app are emitted.
- Action id format: `shortcut:<shortcutId>`.
- Launch uses `LauncherApps.startShortcut` with the app's package and activity.
- Guarded again on API level, installed state, and non-blank shortcut id.

Shortcut discovery currently runs inside a `remember` block during composition of the menu. It is
resolved once per app rather than on every recomposition, but it is still a synchronous platform
query on the composition path. Moving it to a background state flow is a recorded performance task.

## 5. State ownership

`LauncherViewModel` owns:

- Opening and dismissing the context menu.
- Dismissing a menu whose package disappeared after a package refresh.
- Haptic feedback for menu actions.
- Dispatching actions to repositories.
- Keeping context menu dismissal consistent with the overlay state machine.

```kotlin
fun showAppContextMenu(app: LauncherApp, anchor: Offset)
fun dismissAppContextMenu()
fun onContextMenuActionAppInfo(app: LauncherApp)
fun onContextMenuActionRemoveApp(app: LauncherApp)
fun onContextMenuActionUninstallApp(app: LauncherApp)
fun onContextMenuActionLaunchShortcut(app: LauncherApp, shortcutId: String)
```

Pause and widget handlers exist as safe no-ops or dismissals because the actions are never emitted
today.

## 6. Stale menu handling

When the installed-app flow refreshes:

- The ViewModel checks whether the open menu's package is still present.
- If the package is gone, the overlay closes.
- If the package is still present, the menu stays open with refreshed app data.

This prevents acting on a package that was uninstalled or replaced between opening the menu and
tapping an action.

## 7. Accessibility

Implemented:

- `LivingAppIcon` uses raw `pointerInput` for the touch path but exposes both `onClick` (launch) and
  `onLongClick` (menu) through semantics, so TalkBack and switch access do not depend on synthetic
  touch gestures.
- The long-click semantic anchor is the icon centre, making popup placement predictable.
- Menu rows are standard `DropdownMenuItem`s with text and an icon content description.
- Back press dismisses the popup.
- `LivingAppIconAccessibilityTest` verifies click and long-click semantics, including the case where
  no long-press handler is supplied.

Owner-reported validation:

- TalkBack click and long-press passed on a Google Pixel 8a.

Still unverified:

- TalkBack focus order and announcement of every popup row.
- Switch-access and keyboard/D-pad traversal.
- Menu item touch targets on hardware with large font/display settings.

The previous click-semantics gap is resolved. Accessibility still needs broader traversal testing
outside the owner's 12-scenario report.

## 8. Test coverage

| Test class | Coverage |
| --- | --- |
| `AppContextMenuPolicyTest` | Action ordering, installed-state gate, system-app uninstall block, hidden pause/widgets, shortcut filtering |
| `LauncherAppContextMenuTest` | Long-press opens the overlay, dismissal paths, remove behavior, app-info dispatch, failed uninstall |
| `PackageManagerAppRepositoryTest` | Details intent, uninstall guards, shortcut discovery/launch guards |
| `WorkspaceRepositoryTest` | Remove-from-workspace does not uninstall and cleans folder references |
| `LivingAppIconAccessibilityTest` | Click/long-click semantics and no-long-press fallback |

These are JVM/Robolectric tests. They do not prove Android confirmation dialogs, OEM settings
screens, shortcut-host behavior, popup placement, or TalkBack behavior on hardware. The owner's
Pixel 8a report covers the basic hardware paths; the gaps are listed in section 9.

## 9. Physical-device acceptance checklist

Owner-reported passed on a Google Pixel 8a (12/12, 2026-11-04):

- [x] Long press opens the menu on home, dock, drawer, and folder icons.
- [x] A short tap still launches the app.
- [x] Menu stays on screen and scrolls on the validated device.
- [x] App info opens the correct app's system details screen.
- [x] Remove from launcher keeps the app installed and visible in the drawer.
- [x] Official Android uninstall flow starts correctly.
- [x] Shortcuts appear and launch where supported and permitted.
- [x] TalkBack click and long-press work on app icons.
- [x] Haptics and animations behave as expected.

Still pending:

- [ ] Menu stays on screen in landscape and on wide/foldable displays.
- [ ] Outside tap, back press, and Home-button dismissal are each re-confirmed.
- [ ] Uninstall cancellation leaves everything intact.
- [ ] System apps do not show Uninstall.
- [ ] TalkBack can reach and announce every menu row.
- [ ] Haptics respect the disabled setting.
- [ ] Uninstalling the app from system settings while the menu is open closes the menu.
- [ ] Android 7.0/7.1 fallback behavior.

## 10. Known limitations

1. Menu placement uses an estimated height rather than a measured popup.
2. Shortcut discovery runs off-composition on `Dispatchers.IO` in the Phase 1.2.1 working tree, but it
   is still scoped to the composition lifecycle rather than a long-lived state flow.
3. Pause and Widgets remain unavailable by design.
4. The `app/src/androidTest` accessibility suite compiles but does not cover the menu, and it has not
   been executed without a device.
5. Physical validation is owner-reported and does not yet cover every checklist item.
6. Shortcut-host permission behavior varies by device and launcher role.
