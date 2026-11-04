# Project Status

Snapshot date: 2026-11-04

This ledger records repository evidence, not intentions. It must be updated after every phase.

## 1. Repository state

| Item | Value |
| --- | --- |
| Repository | `SokolSaitiAlb/Albkestrel-launcher` |
| Worktree audited | `/home/alb/orca/workspaces/Albkestrel-launcher/main` |
| Branch | `migration-orca` |
| HEAD | `4b0f06673844e3476408884f9859f2cd81726bb3` |
| HEAD subject | `docs(license): add GPL-3.0-or-later license` |
| Public `main` | `24932c3a5e740b1034e3243fc5c5a7b3256dafb6` |
| Merge base of `main` and `migration-orca` | `24932c3` |
| `migration-orca` ahead of `main` | 4 commits (`b93c39f`, `8747b09`, `157e446`, `4b0f066`) |
| `main` ahead of `migration-orca` | 0 commits |
| Project license | `GPL-3.0-or-later` (`LICENSE`) |

## 2. Current development phase

**Phase 1.2 — long-press app context menu.**

The implementation is committed on `migration-orca` as `157e446`. It is not present on the public
`main` branch and must not be described as merged or released. The project owner reports 12/12
physical scenarios passed on a Google Pixel 8a; this is owner-reported validation, not an
independently reproduced run.

Phase 1.1 (`8747b09`) is committed on `migration-orca`. Phase 1.3 (widgets) and Phase 1.4 (icon
engine) are planned, not implemented.

## 3. Git evidence for phase status

```text
main                 24932c3  chore: initialize Android project structure
migration-orca       4b0f066  docs(license): add GPL-3.0-or-later license
migration-orca       157e446  feat(launcher): complete phase 1.2 app context menu
migration-orca       8747b09  feat(apps): integrate real installed-app discovery, search, and Room v3 metadata
```

Phase 1.2 was committed as `157e446` on top of `8747b09`:

- 13 modified tracked files.
- 5 new Phase 1.2 implementation/test files, including the icon accessibility test.
- 18 files total.

The GPL license and third-party notices were committed as `4b0f066`. Documentation and CI are
committed separately. The untracked `skills/` directory is unrelated external agent content and is
excluded from all commits.

## 4. Verified features

### 4.1 Committed on `migration-orca` (Phase 1.0/1.1)

- HOME activity with `MAIN`, `LAUNCHER`, `HOME`, and `DEFAULT` categories.
- Compose launcher shell with overlay rendering.
- Room database v3 with pages, items, folders, folder items, and metadata.
- Migrations `1 → 2` and `2 → 3` with exported schema JSON.
- DataStore settings with legacy SharedPreferences migration.
- Real installed-app discovery through `PackageManager`.
- Real labels, icons, categories, and system-app flags.
- Deterministic app mapping, self-exclusion, and alphabetical search.
- Package-change cache invalidation.
- App drawer, search overlay, category filtering, loading and empty states.
- Persistent dock and folder modal with title editing.
- Clock/date and real battery telemetry.
- Mock weather, media, and wellbeing cards, visibly labelled.
- Prototype scope/limitations dialog.

### 4.2 Committed Phase 1.2 work (`157e446`)

- Long press on home, dock, drawer, and folder icons.
- Anchored, clamped, scrollable context menu.
- App info, remove from launcher, and uninstall actions.
- System-app uninstall blocking in policy and repository.
- API 25+ shortcut discovery and launch with host-permission guard.
- Pause and Widgets actions hidden because the platform integration is unavailable.
- Outside-tap, back, and action dismissal.
- Stale-menu cleanup after package refresh.
- Explicit click and long-click accessibility semantics on `LivingAppIcon`.
- Focused unit tests for policy, repository actions, shortcuts, ViewModel behavior, and icon
  semantics.

### 4.3 Latest recorded validation

Re-run after adding the focused icon accessibility test and updating the documentation:

| Check | Result |
| --- | --- |
| `./gradlew :app:testDebugUnitTest --rerun-tasks` | 64 tests, 0 failures, 0 errors, 0 skipped |
| `./gradlew :app:assembleDebug` | Successful (up-to-date; test-only change) |
| `git diff --check` | Clean |
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk`, 22,711,583 bytes, SHA-256 `86f938ada45d66e6175df6634be70fadc3d395eee897a528c8dcb15172657059` |

The project owner separately reported 12/12 physical scenarios passed on a Google Pixel 8a. Markdown
links, code fences, GitHub YAML, and `git diff --check` were validated separately. The GitHub Actions
workflow has not been executed on GitHub.

## 5. Physical validation

### 5.1 Owner-reported validation

The project owner reports **12/12 passed** on a Google Pixel 8a on 2026-11-04:

- App launch by tap.
- Long-press on Home, App Drawer, Dock, and inside folders.
- App Info and Remove from Home.
- Official Android uninstall flow.
- Shortcut discovery and execution.
- Context-menu screen bounds and scrolling.
- Haptics and animations.
- TalkBack click and long-press accessibility.

This is an owner report, not an independently reproduced test run. It is consistent with the current
source and automated tests, including the explicit click/long-click semantics.

### 5.2 Still without recorded physical validation

- Android 7.0/7.1 shortcut fallback and permission-denied behavior.
- Uninstall cancellation and post-uninstall refresh edge cases.
- App-details routing on OEM builds.
- HOME-role selection and Home-button overlay collapse across OEM builds.
- Edge-to-edge insets on notched and gesture-navigation devices.
- Real icon rendering at every configured icon scale.
- Landscape, wide/foldable, and RTL layouts.
- Switch-access and keyboard/D-pad traversal.
- TalkBack focus order within the context menu.
- Package-change refresh while the launcher is visible.

## 6. Known limitations

### Platform limitations

- No public per-app pause API for launchers.
- No real `AppWidgetHost`; third-party widgets are not hosted.
- Shortcut access depends on API 25+ and shortcut-host permission.
- The HOME role must be selected by the user.
- Notification badges have no data source.
- Recents/Quickstep replacement is outside a normal launcher APK.
- Work-profile separation is not implemented.

### Implementation limitations

- Shortcut discovery runs synchronously during context-menu composition.
- The icon cache is not bounded.
- Dynamic color and wallpaper dim are persisted but not exposed in Settings.
- Workspace editing exists in the repository but not as drag-and-drop UI.
- Widget slots persist but render nothing.
- No instrumentation test source set exists.
- The GitHub Actions workflow has been added but has not yet run on GitHub.
- The physical validation is owner-reported; no independent device matrix is recorded.
- Backup rules are sample templates.
- Unused Firebase/AppCheck dependencies remain in the build.
- The bundled launcher icon and wallpaper have unresolved provenance and licensing.
- A per-dependency license audit has not been completed.

### Documentation limitations

- No screenshots are committed.
- No release notes exist because no release exists.
- No security contact email is documented; use GitHub private reporting if available.

## 7. Branch assumptions

- The active development branch is `migration-orca`.
- `main` is the public branch and currently contains only the initial scaffold.
- Phase 1.2 is committed on `migration-orca` as `157e446`; integration still requires its remaining
  acceptance items and explicit authorization.
- `main` must not be modified without explicit authorization.
- No merge, rebase, cherry-pick, push, or branch switch is authorized by this status document.

## 8. Outstanding risks

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Owner-reported validation only | Some device-specific paths may still fail | Independently re-run the checklist and expand the device matrix |
| Unused Firebase dependencies | Extra permissions/surface area | Remove and verify merged manifest |
| Unbounded icon cache | Memory pressure with many apps | Bound and measure |
| Main-thread shortcut query | Menu jank | Move discovery to a background state flow |
| Sample backup rules | Unclear data exposure | Define explicit backup policy |
| No instrumentation tests | Regressions in system flows | Add `androidTest` suite |
| Unresolved asset provenance | Bundled artwork may not be redistributable | Audit or replace the launcher icon and wallpaper; record in `THIRD_PARTY_NOTICES.md` |
| Incomplete dependency audit | Missing third-party notices | Generate a per-dependency license report before release |
| No release signing | Cannot produce a distributable release | Set up external signing material |

## 9. Next recommended milestone

1. Review the committed Phase 1.2 diff (`157e446`) and licensing commit (`4b0f066`).
2. Merge or publish the documentation/CI commit only with explicit authorization.
3. Independently re-run the physical-device checklist and broaden coverage where noted in 5.2.
4. Remove unused dependencies and verify the merged manifest.
5. Verify the GitHub Actions workflow on GitHub before claiming CI is green.
6. Only then begin Phase 1.3 after accepting ADR-003.

## 10. Status honesty rules

- Do not describe Phase 1.1 or Phase 1.2 as merged into public `main`.
- Do not describe mock cards as live services.
- Do not describe widget slots as widget hosting.
- Do not describe icon rendering as an icon engine.
- Do not describe unit tests as device validation.
- Update this file whenever the branch, HEAD, phase, or verification state changes.
