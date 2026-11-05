# Project Status

Snapshot date: 2026-11-05

This ledger records repository evidence, not intentions. It must be updated after every phase.

## 1. Repository state

| Item | Value |
| --- | --- |
| Repository | `SokolSaitiAlb/Albkestrel-launcher` |
| Worktree audited | `/home/alb/orca/workspaces/Albkestrel-launcher/main` |
| Branch | `migration-orca` |
| HEAD | `63cb0c51862642b5a518ced82ffe222e7bbeef37` |
| HEAD subject | `test(launcher): stabilize ViewModel coroutine test dispatchers` |
| Public `main` | `24932c3a5e740b1034e3243fc5c5a7b3256dafb6` |
| Merge base of `main` and `migration-orca` | `24932c3` |
| `migration-orca` ahead of `main` | 8 commits (`b93c39f`, `8747b09`, `157e446`, `4b0f066`, `29e5170`, `fc84cff`, `b226446`, `63cb0c5`) |
| `main` ahead of `migration-orca` | 0 commits |
| Working tree | Phase 1.2.1 hardening changes present and **uncommitted** |
| Project license | `GPL-3.0-or-later` (`LICENSE`) |

## 2. Current development phase

**Phase 1.2.1 — hardening of the long-press context-menu release.** Phase 1.2 itself is complete at
`157e446`.

The Phase 1.2 implementation is committed on `migration-orca` as `157e446`. It is not present on the
public `main` branch and must not be described as merged or released. The project owner reports 12/12
physical scenarios passed on a Google Pixel 8a; this is owner-reported validation, not an
independently reproduced run.

Phase 1.2.1 hardening is present only as **uncommitted working-tree changes** on top of HEAD
`63cb0c5`. It removes unused Firebase/App Check scaffolding, defines and validates the backup policy,
bounds the icon cache, moves shortcut discovery off the composition thread, expands accessibility
and keyboard support, and adds the Plus Jakarta Sans OFL text. None of that work is committed or
device-tested.

Phase 1.1 (`8747b09`) is committed on `migration-orca`. Phase 1.3 (widgets) and Phase 1.4 (icon
engine) are planned, not implemented.

## 3. Git evidence for phase status

```text
main                 24932c3  chore: initialize Android project structure
migration-orca       63cb0c5  test(launcher): stabilize ViewModel coroutine test dispatchers
migration-orca       b226446  fix(ci): generate debug keystore on runner
migration-orca       fc84cff  fix(ci): avoid obsolete Android SDK tools package
migration-orca       29e5170  docs: add project documentation and GitHub CI
migration-orca       4b0f066  docs(license): add GPL-3.0-or-later license
migration-orca       157e446  feat(launcher): complete phase 1.2 app context menu
migration-orca       8747b09  feat(apps): integrate real installed-app discovery, search, and Room v3 metadata
```

Phase 1.2 was committed as `157e446` on top of `8747b09`:

- 13 modified tracked files.
- 5 new Phase 1.2 implementation/test files, including the icon accessibility test.
- 18 files total.

The GPL license and third-party notices were committed as `4b0f066`. Documentation and CI were
committed as `29e5170`, `fc84cff`, and `b226446`; the coroutine-dispatcher test fix is `63cb0c5`. The
untracked `skills/` directory is unrelated external agent content and is excluded from all commits.

The Phase 1.2.1 hardening changes are not part of any commit. They must be reviewed, committed, and
validated before they are reported as durable.

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

### 4.3 Phase 1.2.1 working-tree changes (uncommitted)

- Removed Firebase BOM/AI/App Check dependencies, the `secrets` and `googleServices` blocks, plugin
  aliases, catalog entries, `googleServices.missing.passthrough`, and `.env.example`.
- Debug merged manifest contains no Firebase/GMS/Play Core entry and no `INTERNET` permission.
- Explicit `backup_rules.xml` and `data_extraction_rules.xml`; both XML documents pass `xmllint`
  and Python `ElementTree` parsing. Policy documented in
  [BACKUP_POLICY.md](BACKUP_POLICY.md).
- `IconMemoryCache` bounds decoded icons at 1/10 heap (clamped 4–32 MiB) and does not cache failed
  decodes; `PackageManagerAppRepository` clears it on package invalidation.
- Context-menu shortcut lookup uses `produceState` + `Dispatchers.IO`.
- Clock ticker is minute-aligned and lifecycle-aware; infinite pulse is disabled under
  `AnimationSpeed.REDUCED_MOTION`.
- 48 dp targets, button/radio roles, merged spoken labels, focusability, and keyboard activation
  were added across launcher surfaces; labels were extracted to `strings.xml`.
- New `FolderIconAccessibilityTest`, extended `LivingAppIconAccessibilityTest`, new
  `IconMemoryCacheTest` and `ClockTickerTest`, plus a compiled
  `AccessibilityInstrumentationTest` suite.
- `licenses/PlusJakartaSans-OFL.txt` added; `THIRD_PARTY_NOTICES.md` records SHA-256 hashes and
  retains the unresolved JPEG/mipmap blockers.

### 4.4 Latest recorded validation

Run on the uncommitted working tree at HEAD `63cb0c5`:

| Check | Result |
| --- | --- |
| `./gradlew :app:testDebugUnitTest --rerun-tasks` | 75 tests, 0 failures, 0 errors, 0 skipped |
| `./gradlew :app:assembleDebug` | Successful |
| `./gradlew :app:assembleDebugAndroidTest` | Successful; instrumentation suite compiles |
| `git diff --check` | Clean |
| `xmllint` / `ElementTree` on backup XML | Both documents valid |
| `./gradlew :app:connectedDebugAndroidTest` | **Not run** — no device or emulator attached |

The project owner separately reported 12/12 physical scenarios passed on a Google Pixel 8a before
the Phase 1.2.1 changes. That report does not cover the accessibility, backup, cache, or manifest
changes. GitHub Actions run #4 (`41804893723`) succeeded on 2026-11-04 for the committed baseline
at `63cb0c5`; the uncommitted Phase 1.2.1 working-tree changes were not covered by that run. See
[DEVICE_TEST_MATRIX.md](DEVICE_TEST_MATRIX.md) for the pending checks.

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

- All Phase 1.2.1 accessibility, keyboard, backup, cache, and manifest changes.
- The compiled `AccessibilityInstrumentationTest` suite has not been executed.
- Android 7.0/7.1 shortcut fallback and permission-denied behavior.
- Uninstall cancellation and post-uninstall refresh edge cases.
- App-details routing on OEM builds.
- HOME-role selection and Home-button overlay collapse across OEM builds.
- Edge-to-edge insets on notched and gesture-navigation devices.
- Real icon rendering at every configured icon scale.
- Landscape, wide/foldable, and RTL layouts.
- Switch-access and keyboard/D-pad traversal.
- TalkBack focus order within the context menu and every launcher surface.
- Package-change refresh while the launcher is visible.
- Cloud backup encryption gating, cloud restore, and device-to-device transfer.

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

- Phase 1.2.1 changes are uncommitted and have not been device-tested.
- The icon cache is bounded in the working tree but has not been profiled on a low-memory device.
- Dynamic color and wallpaper dim are persisted but not exposed in Settings.
- Workspace editing exists in the repository but not as drag-and-drop UI.
- Widget slots persist but render nothing.
- The `app/src/androidTest` suite exists and compiles but has not run.
- GitHub Actions run #4 (`41804893723`) succeeded on 2026-11-04 at `63cb0c5`; the uncommitted
  Phase 1.2.1 working-tree changes have not been validated by CI.
- The physical validation is owner-reported and predates Phase 1.2.1; no independent device matrix
  is recorded.
- Backup rules are explicit and XML-valid but have not been exercised by a real backup/restore.
- The bundled launcher icon and wallpaper have unresolved provenance and licensing. **Release
  blocker.**
- The SIL OFL text for Plus Jakarta Sans is present, but a full per-dependency license audit has not
  been completed.

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
| Phase 1.2.1 is uncommitted | Work can be lost or conflated with other changes | Review, commit, and validate with explicit authorization |
| Owner-reported validation only | Device-specific paths may still fail | Independently run `DEVICE_TEST_MATRIX.md` |
| Bounded cache not profiled | Memory behaviour on low-RAM devices is unmeasured | Measure on a low-memory device and adjust the budget |
| Backup rules not exercised | Restore may omit or expose data unexpectedly | Run BAK-01 through BAK-07 on a device |
| Instrumentation suite not executed | Regressions in accessibility semantics may slip through | Run `connectedDebugAndroidTest` with a device attached |
| Unresolved asset provenance | Bundled artwork may not be redistributable | Audit or replace the launcher icon and wallpaper; record in `THIRD_PARTY_NOTICES.md` |
| Incomplete dependency audit | Missing third-party notices | Generate a per-dependency license report before release |
| No release signing | Cannot produce a distributable release | Set up external signing material |

## 9. Next recommended milestone

1. Review and, with explicit authorization, commit the Phase 1.2.1 hardening changes.
2. Attach a Pixel 8a or equivalent device and execute
   [DEVICE_TEST_MATRIX.md](DEVICE_TEST_MATRIX.md), including `connectedDebugAndroidTest`.
3. Resolve the bundled JPEG and mipmap provenance before any distribution.
4. Complete the per-dependency license report.
5. Confirm that a CI run covers the Phase 1.2.1 commit before claiming CI is green; run #4
   (`41804893723`) validated only the `63cb0c5` baseline.
6. Only then begin Phase 1.3 after accepting ADR-003.

## 10. Status honesty rules

- Do not describe Phase 1.1 or Phase 1.2 as merged into public `main`.
- Do not describe mock cards as live services.
- Do not describe widget slots as widget hosting.
- Do not describe icon rendering as an icon engine.
- Do not describe unit tests as device validation.
- Update this file whenever the branch, HEAD, phase, or verification state changes.
