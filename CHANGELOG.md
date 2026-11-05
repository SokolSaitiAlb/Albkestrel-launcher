# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/). This project does
not yet have released versions, so no release comparison links are provided.

## [Unreleased]

### Added — Phase 1.2: long-press app context menu

- Long-press context menu on home, dock, app drawer, and folder app icons.
- Anchored popup with screen-edge clamping, bounded height, and scrolling.
- App info action that opens Android application details and dismisses the launcher menu.
- Remove-from-launcher action that deletes only workspace and folder references.
- Uninstall action using Android's official `ACTION_DELETE` flow.
- System-app uninstall blocked in both the domain policy and the repository.
- Discovered launcher shortcuts on Android 7.1 (API 25) and newer when shortcut-host permission is
  available.
- Pause and Widgets actions withheld because no supported public integration exists.
- Dismissal by outside tap, back press, and action selection.
- Stale context-menu dismissal when the package disappears during refresh.
- Accessibility `onClick` and `onLongClick` semantics for icon launch and context-menu actions.
- Focused Robolectric/Compose semantics test for click and long-click behavior.
- Unit tests for context-menu policy, repository actions, shortcut guards, and ViewModel behavior.
- Owner-reported physical validation: 12/12 scenarios passed on a Google Pixel 8a.

### Added — Phase 1.2.1 hardening (uncommitted)

These entries describe working-tree changes on top of `63cb0c5`; they are not committed.

- Explicit backup policy in `backup_rules.xml` and `data_extraction_rules.xml`, documented in
  `docs/BACKUP_POLICY.md`; cloud backup requires encryption and device-to-device transfer carries the
  same data.
- Bounded icon cache (`IconMemoryCache` / `LruCache`) with a heap-derived byte budget and no negative
  caching; cleared on package changes.
- Shortcut discovery moved off the composition thread onto `Dispatchers.IO` via `produceState`.
- Minute-aligned, lifecycle-aware clock ticker; infinite pulse disabled under reduced motion.
- Memoized search and drawer filtering with `remember`.
- Button roles, spoken labels, 48 dp targets, focusability, and Enter/Numpad Enter/Space activation
  across launcher surfaces; accessibility labels extracted to `strings.xml`.
- `FolderIconAccessibilityTest`, extended `LivingAppIconAccessibilityTest`, and a compiled
  `AccessibilityInstrumentationTest` suite.
- `IconMemoryCacheTest` and `ClockTickerTest`.
- `docs/DEVICE_TEST_MATRIX.md` with accessibility, functional, backup, and performance checks.
- SIL OFL 1.1 text for Plus Jakarta Sans in `licenses/PlusJakartaSans-OFL.txt`; asset SHA-256
  inventory in `THIRD_PARTY_NOTICES.md`.

### Added — licensing

- Complete GNU General Public License v3 text in `LICENSE`.
- Project licensing declaration changed to `GPL-3.0-or-later`.
- Third-party notices and asset audit in `THIRD_PARTY_NOTICES.md`, including dependency, font, image,
  and trademark attribution.
- GPL contribution and compatibility rules in the contribution and agent documentation.
- Optional Google Play Billing positioned as future monetization that does not restrict GPL rights.

### Added — documentation and repository governance

- Root README, TODO checklist, roadmap, master plan, architecture, project structure, contribution
  guidelines, security policy, changelog, and AI agent instructions.
- Project status ledger.
- Development setup, build/test, release, design-system, context-menu, widget-architecture, and
  icon-engine documentation.
- Architecture decision records for local-first design, launcher architecture, and a proposed widget
  host.
- GitHub issue templates, pull-request template, and Android CI workflow.

### Changed

- Real installed-app discovery, labels, icons, categories, and launch intents.
- Room database upgraded to version 3 with exported schemas and migrations `1 → 2` and `2 → 3`.
- Workspace bootstrap made idempotent with a persisted completion marker.
- Legacy prototype package cleanup limited to exact known package names and run once.
- Search and category filtering centralized in pure `AppSearch` helpers.
- `LivingAppIcon` now renders real app icons with a vector fallback and supports long press.

### Fixed

- Pre-v3 empty workspaces are no longer reseeded after restart.
- Intentionally cleared workspaces remain empty.
- Collision checks prevent overlapping workspace items.
- Folder deletion cleans up folder items and workspace references.
- Uninstalled workspace apps render as dimmed, non-launchable entries.
- Package-change broadcasts invalidate app and icon caches.
- Unavailable apps no longer open a prototype preview dialog.

### Security

- Uninstall is blocked for system packages at both policy and repository levels.
- The package-change receiver is registered as not exported.
- Shortcut operations are API-gated and permission-checked.
- No network, analytics, or telemetry code is present in the launcher implementation.

### Known limitations

- Phase 1.2.1 hardening is **uncommitted**; HEAD remains `63cb0c5`.
- Phase 1.2 is committed as `157e446`; it is not merged into `main` and not released.
- The Pixel 8a validation is owner-reported, predates Phase 1.2.1, and has not been independently
  re-run.
- The `app/src/androidTest` accessibility suite compiles but has not been executed; no device or
  emulator was attached.
- The GitHub Actions workflow has not yet run on GitHub.
- Firebase/AppCheck dependencies were removed in the working tree; verify again after committing.
- Dynamic color and wallpaper dim are persisted but not exposed in Settings.
- Notification badge data is not collected.
- Widgets, icon packs, Quickstep, work profiles, and drag-and-drop editing are not implemented.
- Provenance and licensing for the bundled launcher icon and wallpaper remain unresolved; the SIL
  OFL text for Plus Jakarta Sans is now present.
- A per-dependency license audit has not been completed.

## Project history

The following entries reflect commits that exist in the repository. They are not release versions.

### 2026-11-04 — `4b0f066`

- Added the complete GNU GPL v3 text as `LICENSE` and declared `GPL-3.0-or-later`.
- Added `THIRD_PARTY_NOTICES.md` with the dependency, font, image, and trademark audit.

### 2026-11-04 — `157e446`

- Completed Phase 1.2: long-press app context menu across home, dock, drawer, and folders.
- Added App Info, Remove from launcher, official uninstall, and guarded shortcut actions.
- Added explicit click and long-click accessibility semantics with a focused Compose semantics test.

### 2026-11-03 — `8747b09`

- Integrated real installed-app discovery, search, and Room v3 metadata.
- Added real icon rendering, package-change invalidation, and installed-app mapping.
- Added Room v3 bootstrap metadata and migration.
- Added workspace persistence tests and app-search tests.

### 2026-11-03 — `b93c39f`

- Restored the Gradle wrapper and debug signing configuration.

### 2026-11-01 — `24932c3`

- Initialized the Android project structure.
- Public `main` currently points at this commit.

### 2026-11-01 — `051fdc2`

- Initial repository commit.
