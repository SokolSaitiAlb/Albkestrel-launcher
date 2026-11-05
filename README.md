# Albkestrel Launcher

A local-first Android home screen built with Kotlin and Jetpack Compose. Albkestrel Launcher
explores the "Living Flow" design language: luminous surfaces, spring-based motion, real installed
applications, persistent workspace layouts, and a focused long-press context menu.

This repository is the Android launcher application. It is not a complete operating system image.

Albkestrel Launcher is free and open-source software licensed under
[GPL-3.0-or-later](LICENSE). You may use, study, share, and modify it under the terms of the GNU
General Public License.

## Repository status snapshot

> Snapshot date: 2026-11-05

| Item | State |
| --- | --- |
| Public `main` | `24932c3` — initial Android project scaffold only |
| Active development branch | `migration-orca` (Phase 1.1, Phase 1.2, documentation, and licensing commits ahead of `main`) |
| Phase 1.2 context menu | Implemented and owner-validated; committed as `157e446` |
| Phase 1.2.1 hardening | Implemented in the working tree, **uncommitted**; see [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) |
| Production release | None |
| License | **GPL-3.0-or-later** — see [LICENSE](LICENSE) |
| Device validation | Owner-reported 12/12 on Google Pixel 8a (2026-11-04); not independently re-run in this audit |
| CI workflow | Added on `migration-orca`, **not yet verified on GitHub** |

Phase 1.2 is committed on `migration-orca` as `157e446` and owner-validated on a Google Pixel 8a.
It is not merged into the public `main` branch. The Phase 1.2.1 hardening work described in
[docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) exists only as uncommitted changes in the working
tree; it has not been committed, merged, or device-tested. Phase 1.3 (widgets) and Phase 1.4 (icon
engine) are planned, not implemented. Read the status document before treating any feature as
shipped.

## Introduction

Albkestrel Launcher replaces the default home screen with a Compose-rendered workspace. It queries
Android's `PackageManager` for launchable applications, renders the real app labels and icons,
persists home pages, dock items, and folders in Room, and stores user preferences in DataStore.

The project intentionally distinguishes between:

- **Implemented** behavior backed by Android platform APIs and local persistence.
- **Mocked** surfaces that are visibly labelled as mock data.
- **Planned** architecture that exists only as a design document or an integration hook.
- **Unavailable** capabilities blocked by missing public Android APIs.

## Product vision

AlbkestrelOS aims to be a calm, private, local-first launcher that:

1. Puts the user's installed applications and layout first.
2. Keeps personal data on the device by default.
3. Uses motion and haptics for feedback without becoming distracting.
4. Exposes only actions that are genuinely available on the current Android version.
5. Treats accessibility and reduced-motion preferences as first-class concerns.
6. Grows through explicit, documented phases instead of accumulating hidden prototype behavior.

The long-term product and engineering strategy is in [MASTER_PLAN.md](MASTER_PLAN.md).

## Features actually implemented

The following features exist on the `migration-orca` branch. Most were introduced by Phase 1.1 and
Phase 1.2; see the status table for caveats.

### Launcher foundation

- Android HOME activity declared with `MAIN`, `LAUNCHER`, `HOME`, and `DEFAULT` categories.
- `singleTask` launch mode and `onNewIntent()` handling so pressing Home closes an active overlay
  before returning to the primary workspace page.
- Edge-to-edge Compose UI with status-bar and navigation-bar insets.
- Two horizontal home pages implemented with Compose `HorizontalPager`.
- Spring-based overlay transitions with a reduced-motion option.
- Haptic feedback controlled by a persisted user setting.

### Installed applications

- Real app discovery through `PackageManager.queryIntentActivities` for `MAIN`/`LAUNCHER`.
- The launcher excludes its own package from the app list.
- Multiple launcher activities in one package are collapsed to one stable entry.
- Real labels, icons, system-app flags, and platform categories where available.
- Explicit launch intents for the discovered activity, with a front-door fallback.
- Package add/remove/change/replace broadcasts invalidate the app and icon caches.
- Alphabetical ordering and case-insensitive label search.

### Workspace persistence

- Room database version 3 with pages, workspace items, folders, folder items, and bootstrap
  metadata.
- Migrations `1 → 2` and `2 → 3`, plus exported Room schemas.
- Idempotent, transaction-based first-run seeding with a persisted bootstrap marker.
- Existing user layouts are preserved across restarts and upgrades.
- Dock and home grids use independent coordinate spaces.
- Collision checks prevent overlapping items during programmatic placement.
- One-time cleanup removes only the exact legacy prototype package names.

### App drawer and search

- Full app drawer with a search field, category tabs, loading state, and empty states.
- Search overlay with auto-focus, suggested apps, and matching results.
- Category filtering and label filtering share the same pure `AppSearch` helpers.

### Home screen, dock, and folders

- Clock and date rendered from the device clock.
- Real battery percentage and charging state from `ACTION_BATTERY_CHANGED`.
- Persistent dock populated from Room.
- Folder modal with a three-column app grid and editable folder title persisted to Room.
- Mock weather, media, and wellbeing cards are clearly labelled as mock or preview content.

### Long-press app context menu (Phase 1.2)

- Long press is wired on home, dock, app drawer, and folder app icons.
- The menu is anchored near the pressed icon and clamped to the screen.
- Actions: **App info**, discovered **shortcuts**, **Remove from launcher**, and **Uninstall** for
  eligible user apps.
- **Pause app** and **Widgets** are deliberately hidden because no supported public integration
  exists yet.
- Uninstall uses Android's official `ACTION_DELETE` flow; system apps never expose or execute
  uninstall.
- Remove deletes only the launcher's workspace references and never uninstalls the package.
- Dismissal works through outside tap, back press, and action selection.
- A stale menu closes if its package disappears during a refresh.
- Shortcut discovery and launch are guarded to Android 7.1 (API 25) and the launcher shortcut-host
  permission.
- Accessibility semantics expose both `onClick` (launch) and `onLongClick` (menu) actions for
  TalkBack and switch access. The owner's Pixel 8a validation covered TalkBack click and long-press.
  A focused Robolectric/Compose test guards both semantics actions.

See [docs/APP_CONTEXT_MENU.md](docs/APP_CONTEXT_MENU.md) for behavior and limitations.

### Settings

- Icon scale: compact, balanced, expansive.
- Workspace grid density: 4 or 5 columns.
- Theme modes: Living Dark, Luminous Light, Radiant Emerald, System Adaptive.
- Animation speed: Living Flow Spring, Snappy, Reduced Motion.
- Haptic feedback toggle.
- Dock label toggle.
- Prototype scope/limitations dialog.

### Tests

- JVM unit tests covering app mapping, search, settings, workspace bootstrapping, migrations,
  collision handling, package actions, context-menu policy, context-menu ViewModel behavior,
  Home lifecycle behavior, and icon click/long-click semantics.
- Robolectric is used for Android framework behavior in unit tests, including Compose semantics
  checks for `LivingAppIcon`.
- No `androidTest` instrumentation suite exists yet.

## Physical validation status

The project owner reported **12/12 passed** on a Google Pixel 8a on 2026-11-04:

- App launch by tap.
- Long-press on Home, App Drawer, Dock, and inside folders.
- App Info and Remove from Home.
- Official Android uninstall flow.
- Shortcut discovery and execution.
- Context-menu bounds and scrolling.
- Haptics and animations.
- TalkBack click and long-press accessibility.

That result is attributed to the owner's report and was not independently re-run during this audit.

These areas still have no recorded physical validation:

- Android 7.0/7.1 shortcut fallback behavior.
- Default-home-role behavior and Home-button overlay collapse across OEM builds.
- Visual quality of real app icons at every configured icon size.
- Edge-to-edge insets on notched and gesture-navigation devices.
- Switch-access and keyboard/D-pad traversal.
- Wide-device, landscape, and RTL layouts.

## Partially implemented

| Area | What exists | What is missing |
| --- | --- | --- |
| Context-menu shortcuts | API 25+ discovery and launch through `LauncherApps`; owner-validated on Pixel 8a | Cross-OEM, permission-denied, and API 25 fallback coverage |
| Dynamic color | DataStore key, repository toggle, theme integration | No control in the Settings UI |
| Wallpaper dimming | Persisted value and rendering in `HomeScreen` | No Settings slider |
| Notification badges | Model field and badge rendering | No notification listener or data source |
| Widget slots | `WIDGET_SLOT` persistence and an empty render branch | Real `AppWidgetHost` integration |
| Workspace editing | Repository `moveItem`/`addItem`/collision logic | No drag-and-drop or add-to-workspace UI |
| Backup | `allowBackup="true"` and sample XML rules | No intentional include/exclude policy |
| Instrumentation testing | Test runner dependency is configured | No `androidTest` source set |

## Mocked or preview-only surfaces

These are intentionally not presented as production services:

- Weather card: fixed mock data, labelled `MOCK WEATHER DATA`.
- Media card: interactive mock playback state, labelled `MOCK SERVICE`.
- Digital wellbeing card: fixed screen-time data, labelled `DIGITAL WELLBEING (MOCK)`.
- Wi-Fi and cellular glyphs in the clock card: decorative, not live telemetry.
- Widget slot rendering: reserved for the future widget phase.

## Not implemented

- Third-party Android widget hosting (`AppWidgetHost` / `AppWidgetManager`).
- Icon pack parsing, themed icon engine, or per-app icon overrides.
- Quickstep/Recents integration or OS-level overview replacement.
- Work-profile or multi-user launcher separation.
- Fully interactive drag-and-drop workspace editing.
- Real weather, media, or Digital Wellbeing integrations.
- Notification badge data collection.
- Release signing material and a published release.
- A physical-device test matrix.

## Screenshots

No screenshots are committed to the repository yet. When screenshots become available, place them
under `docs/images/` and add at most one representative image per surface:

| Surface | Suggested file |
| --- | --- |
| Home screen and dock | `docs/images/home.png` |
| App drawer | `docs/images/app-drawer.png` |
| Search overlay | `docs/images/search.png` |
| Folder modal | `docs/images/folder.png` |
| App context menu | `docs/images/context-menu.png` |
| Settings | `docs/images/settings.png` |

Do not add mock screenshots that imply unavailable functionality.

## Architecture overview

```text
Compose UI
  MainActivity / LauncherRoot
  Home, Dock, App Drawer, Search, Folders, Settings, Context Menu
        |
        v
LauncherViewModel
  combine(app list, workspace flows, preferences)
  overlay state machine, haptics, action dispatch
        |
        v
Domain repositories (interfaces)
  AppRepository, WorkspaceRepository, SettingsRepository
        |
        v
Data implementations
  PackageManagerAppRepository  -> Android PackageManager / LauncherApps
  RoomWorkspaceRepository      -> Room database v3
  DataStoreSettingsRepository  -> Preferences DataStore
```

The application uses manual dependency composition in `AlbkestrelApplication`; there is no DI
framework. Full diagrams and layer rules are in [ARCHITECTURE.md](ARCHITECTURE.md). Architecture
decisions are recorded in [docs/decisions/](docs/decisions/), including the proposed widget-host
decision.

## Technology stack

| Component | Version / choice |
| --- | --- |
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose, Material 3 |
| Compose BOM | `2024.09.00` |
| Build | Gradle 9.3.1, Android Gradle Plugin 9.1.1 |
| JDK | 17 (toolchain/CI target) |
| Java bytecode target | 11 |
| Persistence | Room 2.7.0 |
| Preferences | DataStore Preferences 1.1.7 |
| Async | Kotlin Coroutines 1.10.2 |
| Testing | JUnit 4.13.2, Robolectric 4.16.1, Compose UI Test |
| `compileSdk` / `targetSdk` | 36 |
| `minSdk` | 24 |

## Android compatibility

- **Minimum:** Android 7.0 (API 24).
- **Target:** Android 16 / API 36.
- **Shortcuts:** Android 7.1 (API 25) and newer, and only when the launcher holds shortcut-host
  permission.
- **Dynamic color:** Android 12 (API 31) and newer, when enabled through the repository; there is
  no Settings control yet.
- **Vibrator manager:** Android 12 (API 31) and newer, with a legacy `Vibrator` fallback.
- **Platform categories:** Android 8.0 (API 26) and newer; older devices fall back to `TOOLS`.

The launcher must be selected as the device's home app manually through Android settings. An app
cannot silently become the default home screen.

## Build instructions

Prerequisites:

- JDK 17.
- Android SDK Platform 36 (and the SDK tools required by Android Gradle Plugin 9.1.1).
- A local `local.properties` file with `sdk.dir` pointing to the Android SDK. This file is ignored
  by Git.

```bash
git switch migration-orca
./gradlew :app:assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Detailed setup and troubleshooting are in [docs/DEVELOPMENT_SETUP.md](docs/DEVELOPMENT_SETUP.md).

## Test instructions

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
git diff --check
```

The unit suite currently contains 75 JVM tests. An `app/src/androidTest` accessibility suite exists
and compiles, but it has not been executed because no device or emulator was attached. See
[docs/BUILD_AND_TEST.md](docs/BUILD_AND_TEST.md) and
[docs/DEVICE_TEST_MATRIX.md](docs/DEVICE_TEST_MATRIX.md).

## Installation instructions

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Then open Android Settings and choose AlbkestrelOS as the home app. On some OEM builds the path is
**Settings → Apps → Default apps → Home app**. If the launcher is not offered, confirm that the APK
is installed for the current user and that the HOME intent filter is present.

## Project status

- **Current phase:** Phase 1.2 — long-press app context menu, committed as `157e446`. Phase 1.2.1
  hardening is present as **uncommitted working-tree changes**.
- **Code state:** HEAD is `63cb0c5` on `migration-orca`. Phase 1.2 (`157e446`), the GPL licensing
  commit (`4b0f066`), documentation, and CI commits are committed. The Phase 1.2.1 edits are not.
- **Verification state:** 75 unit tests and the debug build pass; the instrumentation suite compiles
  but has not run because no device or emulator is attached. The project owner reports 12/12
  physical scenarios passed on a Google Pixel 8a. Phase 1.3 and Phase 1.4 are planned, not
  implemented.
- **CI state:** GitHub Actions run #4 (`41804893723`) succeeded on 2026-11-04 for the committed
  baseline at `63cb0c5`. The uncommitted Phase 1.2.1 working-tree changes have not been validated by
  GitHub Actions.
- **Public `main`:** still the initial scaffold.

Read [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) for the complete status ledger.

## Roadmap

- [ROADMAP.md](ROADMAP.md) — milestone scope, dependencies, acceptance criteria, status.
- [TODO.md](TODO.md) — actionable checklist grouped by risk area.
- [MASTER_PLAN.md](MASTER_PLAN.md) — long-term product and engineering strategy.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) and [AGENTS.md](AGENTS.md) before making changes. The short
version:

- Work on an explicit branch; never push directly to `main`.
- Keep phases isolated and do not implement future-phase features opportunistically.
- Add tests for business logic and Android repository behavior.
- Never claim a feature works without build or device evidence.
- License contributions under GPL-3.0-or-later and do not add incompatible code or assets.
- Do not commit secrets, signing material, `local.properties`, `.env`, or unrelated agent files.

## Privacy principles

- Local-first: workspace layouts and preferences are stored on the device in Room and DataStore.
- No analytics, telemetry, advertising SDK, or mandatory cloud account is used by launcher code.
- Weather, media, and wellbeing cards are visibly mocked; they are not live services.
- The manifest declares only `VIBRATE`; package visibility is narrowly scoped to
  `MAIN`/`LAUNCHER` activities.
- Firebase, App Check, Google Services, and the secrets Gradle plugin were removed in Phase 1.2.1.
  The debug merged manifest contains no Firebase/GMS/Play Core component and no `INTERNET`
  permission. See [SECURITY.md](SECURITY.md).

## License

Albkestrel Launcher is free software: you can redistribute it and/or modify it under the terms of the
GNU General Public License as published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

The project is distributed **without any warranty**, including without the implied warranty of
merchantability or fitness for a particular purpose. See [LICENSE](LICENSE) for the complete terms.

SPDX identifier: `GPL-3.0-or-later`.

The GPL covers original project code and documentation. Third-party dependencies, the bundled Plus
Jakarta Sans font, icons, images, and other assets keep their own licenses and are not relicensed by
this project. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the current audit, including
unresolved provenance for the bundled launcher icon and wallpaper.

Paid distribution and paid subscriptions are permitted under the GPL. Google Play Billing remains an
optional future monetization feature; it is not a licensing restriction. If billing is added, it must
not reduce the GPL rights of users or contributors.

The GPL does not grant trademark rights over the project name or logo. See
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for trademark attribution.

---

AlbkestrelOS is an independent project. Android is a trademark of Google LLC.
