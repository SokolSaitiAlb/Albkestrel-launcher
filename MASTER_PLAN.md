# Master Plan

This document describes the long-term product vision and engineering strategy for Albkestrel
Launcher / AlbkestrelOS. It is a direction document, not a claim that the described features exist.
See [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) and [TODO.md](TODO.md) for current evidence.

## 1. Product identity

AlbkestrelOS is a local-first Android launcher that treats the home screen as a calm, persistent
workspace rather than an advertising surface. The launcher is the first product surface; the longer
term AlbkestrelOS vision may integrate with a device image, but this repository contains only the
Android launcher application.

### Product principles

1. **Local-first by default.** Layouts, preferences, and app references stay on the device unless a
   future feature has an explicit, documented, opt-in cloud boundary.
2. **Real platform behavior over simulation.** Installed apps, icons, launch intents, app details,
   uninstall flows, shortcuts, and widgets must use supported Android APIs.
3. **Honest scope.** Mock surfaces are labelled. Unavailable actions are hidden. Planned work is
   described as planned.
4. **Calm motion.** Animation exists to explain state changes, not to decorate every interaction.
   Reduced-motion settings must materially reduce movement.
5. **User control.** The user chooses the home app, the layout, the theme, the icon scale, and the
   feedback level. The launcher never silently removes packages or changes system settings.
6. **Accessibility is part of correctness.** A feature that cannot be reached by TalkBack, switch
   access, or keyboard navigation is incomplete.
7. **Small dependency surface.** Every dependency must justify its permissions, binary size, and
   maintenance cost.
8. **Phase discipline.** Do not blend future-phase experiments into an unfinished phase.

## 2. Feature architecture

### 2.1 Launcher shell

- `MainActivity` declares the HOME role and hosts a single Compose tree.
- `LauncherRoot` renders the home screen plus mutually exclusive overlays: app drawer, search,
  settings, folder expansion, prototype information, and app context menu.
- The overlay state is a sealed `LauncherOverlay` type in `LauncherViewModel`.
- Home-button behavior collapses an overlay first, then returns to page 0.

### 2.2 App discovery and launching

- `PackageManagerAppRepository` resolves `MAIN`/`LAUNCHER` activities.
- Labels, icons, categories, and system flags are mapped once into `LauncherApp`.
- The launcher excludes its own package.
- Package-change broadcasts invalidate caches and trigger refresh through a `Flow`.
- Launching uses an explicit component when the activity is known, with a front-door fallback.
- App details and uninstall delegate to Android's official screens.
- Shortcuts use `LauncherApps` on API 25+ with host-permission checks.

### 2.3 Workspace persistence

- Room owns pages, workspace items, folder references, folder items, and bootstrap metadata.
- The bootstrap operation is idempotent and transactional.
- A persisted metadata marker prevents reseeding after a user intentionally clears the workspace.
- Migrations are explicit and schema JSON is exported.
- Placement operations perform collision checks before writing.
- Remove-from-launcher deletes launcher references only.

### 2.4 Preferences

- DataStore Preferences stores icon size, grid columns, theme, animation speed, haptics, dock
  labels, dynamic color, and wallpaper dim.
- A `SharedPreferences` migration preserves older settings.
- Invalid persisted enum/value combinations fall back to safe defaults.

### 2.5 Design system

- `LuminousSurface` provides the common translucent card treatment.
- `LivingAppIcon` renders real icons or a deterministic vector fallback, press scaling, labels, and
  notification badges.
- `FolderIcon` renders a folder preview.
- Theme, typography, and color tokens are centralized under `core/designsystem`.
- Living Flow motion uses spring specs, with a reduced-motion option.

### 2.6 Context menu

- The context-menu policy is a pure domain object: actions are built from the app, system-app flag,
  and discovered shortcuts.
- Pause and widget actions are conditional on capability flags that currently return `false`.
- The UI never fabricates an unavailable picker.
- The ViewModel owns dismissal and action execution.
- Repository actions enforce their own guards as defense in depth.

### 2.7 Widgets

The Universal Widget System is Phase 1.3. It requires a real `AppWidgetHost`, a supported bind
permission flow, provider discovery, persistence, resizing, and lifecycle handling. In-app Compose
"glance" cards are not third-party widgets and must not be described as such. See
[WIDGET_ARCHITECTURE.md](docs/WIDGET_ARCHITECTURE.md) and
[ADR-003](docs/decisions/ADR-003-WIDGET-HOST.md).

### 2.8 Icon appearance

The Icon Appearance Engine is Phase 1.4. It will add icon-pack parsing, per-app overrides, adaptive
shapes, and themed icons. Until then, the launcher uses the real `PackageManager` icon with a
vector-symbol fallback. See [ICON_ENGINE.md](docs/ICON_ENGINE.md).

## 3. Development phases

| Phase | Theme | Primary risk |
| --- | --- | --- |
| 1.0 | Persistent launcher foundation | Data loss during bootstrap or migration |
| 1.1 | Real installed-app integration | Package visibility, cache invalidation, launch correctness |
| 1.2 | Long-press context menu | Destructive-action safety, action availability, accessibility |
| 1.3 | Universal Widget System | Host lifecycle and platform permission flow |
| 1.4 | Icon Appearance Engine | Format support, cache bounds, visual correctness |
| 2.0 | Workspace editing | Collision safety and gesture accessibility |
| 2.1 | Search and shortcut expansion | Determinism and privacy |
| 2.2 | Profiles and multi-user | Data isolation |
| 3.0 | AlbkestrelOS integration | Interface stability and platform assumptions |

Each phase must end with:

1. A reviewed commit on the development branch.
2. Unit tests for business logic.
3. Instrumentation or physical-device validation where Android integration is involved.
4. Updated status, changelog, and roadmap.
5. An explicit statement of remaining limitations.

## 4. Android API limitations

These are not implementation bugs; they are platform boundaries that shape the product:

- **No public per-app pause API.** Digital Wellbeing does not expose a launcher-callable pause
  mechanism. The menu omits "Pause app" until a supported integration exists.
- **No public widget host shortcut.** Hosting third-party widgets requires the platform
  `AppWidgetHost`/`AppWidgetManager` flow and a bind permission, not a simple intent.
- **Shortcut host permission.** `LauncherApps` shortcut queries depend on the launcher holding
  shortcut-host permission. The app degrades to normal launch actions.
- **Default home role.** The user selects the home app in system settings; an app cannot force it.
- **Work profiles.** Profile-aware behavior requires explicit user/profile handling and cannot be
  inferred safely from a single package query.
- **Notification badges.** Badge data requires a notification listener or launcher-provided
  integration; the current model field is not populated.
- **Recents/Quickstep.** Replacing the OS overview is outside a normal launcher APK and is not
  attempted.
- **System app removal.** The launcher must never initiate uninstall for system packages, even when
  a caller bypasses the UI policy.

## 5. Privacy and security

### Data inventory

| Data | Storage | Leaves device? |
| --- | --- | --- |
| Workspace layout, pages, folders | Room | No |
| User preferences | DataStore | No |
| Installed-app metadata | In-memory cache from `PackageManager` | No |
| Battery state | In-memory from sticky broadcast | No |
| Weather/media/wellbeing card values | In-memory mock constants | No |
| Crash reports/analytics | None implemented | No |

### Security boundaries

- No root, shell, or system-only APIs.
- No silent package removal.
- No accessibility-service abuse.
- No overlay over other apps.
- No network permission is declared in the source manifest.
- The package-visibility query is limited to launcher activities.

### Required cleanup before release

- Remove unused Firebase/AppCheck dependencies and verify the merged manifest. **Done in the
  uncommitted Phase 1.2.1 work**; the debug merged manifest has no Firebase/GMS/Play Core entry and
  no `INTERNET` permission.
- Decide and document a backup policy. **Done** in `docs/BACKUP_POLICY.md` and the validated
  `backup_rules.xml` / `data_extraction_rules.xml`.
- Confirm no secrets, signing keys, `.env`, or `local.properties` are tracked.
- Review dependency versions and known vulnerabilities.
- Resolve or replace the bundled launcher icon and wallpaper before distribution.

See [SECURITY.md](SECURITY.md).

## 6. Testing strategy

### Current

- JVM tests with JUnit and Robolectric.
- Pure domain tests for search and context-menu policy.
- Repository tests with in-memory Room databases.
- ViewModel tests with coroutine test dispatchers.
- Room migration validation through test helpers.
- Bounded icon-cache and clock-ticker tests.
- Shared icon/folder accessibility semantics and 48 dp target tests.
- Compiled `app/src/androidTest` accessibility suite (not yet executed on a device).
- Debug assembly as a build gate.

### Required for Android integrations

- Execute the instrumentation suite for launcher UI and context menu on a device.
- Device tests for HOME role, package changes, uninstall, shortcuts, haptics, and TalkBack.
- Migration tests from each released schema.
- Physical-device testing on at least one API 24/25 device, one API 31+ device, and the current
  target API, following `docs/DEVICE_TEST_MATRIX.md`.

### Evidence rule

A green unit test does not prove a device integration works. Reports must state which layer was
validated and which layer remains unverified.

## 7. Release strategy

1. Freeze the phase scope.
2. Confirm GPL-3.0-or-later compliance, third-party notices, and repository governance.
3. Remove unused dependencies and finalize permissions.
4. Run unit tests, debug assembly, and a release build.
5. Run the physical-device acceptance checklist.
6. Update changelog, status, and version fields.
7. Create signed artifacts outside the repository.
8. Publish manually with release notes.

GitHub Actions run #4 (`41804893723`) succeeded on 2026-11-04 for the committed baseline at
`63cb0c5`. It builds and uploads debug artifacts only, and it does not sign or publish releases.
The uncommitted Phase 1.2.1 working-tree changes were not covered by that run.

### Licensing and monetization

- The project is licensed under GPL-3.0-or-later. Distributed binaries must be accompanied by the
  corresponding source and the project license.
- Third-party dependencies, fonts, icons, images, and other assets retain their own licenses and are
  tracked in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
- Google Play Billing is an optional future monetization feature. Paid distribution and paid
  subscriptions are permitted, provided they do not restrict the GPL rights of recipients.
- A paid tier must not require proprietary-only launcher code or remove the ability to build and
  redistribute the GPL-covered application.

## 8. AlbkestrelOS integration

The launcher may eventually integrate with an AlbkestrelOS device image, but this repository does
not assume privileged access. Integration should be expressed through stable, documented interfaces:

- Launcher intent contracts.
- Widget host and provider boundaries.
- Icon and theme extension points.
- Local data export/import with explicit user consent.
- No hidden system hooks or root-only behavior.

Any integration that requires platform signing or system privileges needs a separate, explicit
architecture decision and authorization.

## 9. Decision framework

> AI can propose. Policies decide. System authorizes. User controls.

Engineering decisions follow this order:

1. **Proposal:** an agent or contributor describes the change and its evidence.
2. **Policy:** architecture, privacy, accessibility, and scope rules evaluate it.
3. **Authorization:** the repository owner approves the change and any destructive action.
4. **User control:** the shipped behavior remains visible, reversible, and consent-based.

Architecture decisions are recorded under `docs/decisions/`. Proposed decisions are not accepted
until explicitly marked as accepted by the project owner.
