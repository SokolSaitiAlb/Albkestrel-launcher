# Project Structure

This document maps the actual repository tree and the responsibility of each major package.

## Repository root

```text
Albkestrel-launcher/
├── .github/                         GitHub workflows and templates
├── app/
│   ├── schemas/                     Exported Room schemas (v1, v2, v3)
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/albkestrel/launcher/
│   │   │   └── res/
│   │   └── test/java/com/albkestrel/launcher/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── docs/
│   ├── decisions/                   Architecture decision records
│   ├── APP_CONTEXT_MENU.md
│   ├── BUILD_AND_TEST.md
│   ├── DESIGN_SYSTEM.md
│   ├── DEVELOPMENT_SETUP.md
│   ├── ICON_ENGINE.md
│   ├── PROJECT_STATUS.md
│   ├── RELEASE_PROCESS.md
│   └── WIDGET_ARCHITECTURE.md
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── AGENTS.md
├── ARCHITECTURE.md
├── CHANGELOG.md
├── CONTRIBUTING.md
├── GEMINI.md
├── LICENSE                          GNU GPL v3 license text
├── MASTER_PLAN.md
├── PROJECT_STRUCTURE.md
├── README.md
├── ROADMAP.md
├── SECURITY.md
├── THIRD_PARTY_NOTICES.md           Dependency and asset license audit
├── TODO.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── gradlew.bat
```

`local.properties`, `.env`, signing files, build outputs, Gradle caches, and IDE files are ignored
by Git.

> The untracked `skills/` directory in some agent workspaces is external tooling content. It is not
> part of the Android application and must not be included in application commits.

## Application packages

```text
com.albkestrel.launcher
├── app
├── core
│   ├── common
│   └── designsystem
├── data
│   ├── database
│   │   ├── dao
│   │   └── entity
│   ├── preferences
│   └── repository
├── domain
│   ├── contextmenu
│   ├── model
│   ├── repository
│   └── search
└── feature
    ├── appdrawer
    ├── common
    ├── folders
    ├── home
    ├── search
    └── settings
```

## `app`

| File | Responsibility |
| --- | --- |
| `AlbkestrelApplication.kt` | Composition root; lazy database and repository singletons |
| `MainActivity.kt` | HOME activity, theme wiring, root Compose tree, overlay rendering |

## `core/common`

| File | Responsibility |
| --- | --- |
| `BatteryMonitor.kt` | Sticky battery broadcast converted to a `Flow<BatteryStatus>` |
| `IconMemoryCache.kt` | Heap-bounded `LruCache` for decoded app icons with no negative caching |

## `core/designsystem`

| File | Responsibility |
| --- | --- |
| `Color.kt` | Living Flow color tokens and Material 3 palette mappings |
| `Type.kt` | Plus Jakarta Sans typography scale |
| `Theme.kt` | Dark, light, emerald, system, and dynamic-color theme selection |
| `LuminousSurface.kt` | Shared translucent surface with gradient border |
| `LivingAppIcon.kt` | App icon rendering, press scaling, long press, labels, badges, vector fallback |
| `FolderIcon.kt` | Folder preview icon |

## `data/database`

| File | Responsibility |
| --- | --- |
| `LauncherDatabase.kt` | Room database v3, singleton builder, migrations `1 → 2` and `2 → 3` |
| `dao/WorkspaceDao.kt` | Pages and workspace-item queries and mutations |
| `dao/FolderDao.kt` | Folder and folder-item queries and mutations |
| `dao/WorkspaceMetadataDao.kt` | Bootstrap key/value metadata |
| `entity/WorkspacePageEntity.kt` | Page row |
| `entity/WorkspaceItemEntity.kt` | App, folder reference, or widget-slot row |
| `entity/FolderEntity.kt` | Folder row |
| `entity/FolderItemEntity.kt` | Folder app membership with cascading foreign key |
| `entity/WorkspaceMetadataEntity.kt` | Metadata row and bootstrap keys |

## `data/preferences`

| File | Responsibility |
| --- | --- |
| `DataStoreSettingsRepository.kt` | Preference DataStore, legacy SharedPreferences migration, settings updates |

## `data/repository`

| File | Responsibility |
| --- | --- |
| `PackageManagerAppRepository.kt` | Installed-app discovery, bounded icon cache, package broadcasts, launch, details, uninstall, shortcuts |
| `InstalledAppsMapper.kt` | Deterministic mapping from resolved launcher activities to `LauncherApp` |
| `RoomWorkspaceRepository.kt` | Bootstrap, observation, collision-safe placement, folder cleanup, remove-from-workspace |

## `domain/contextmenu`

| File | Responsibility |
| --- | --- |
| `AppContextMenu.kt` | Action IDs, capability flags, action policy, shortcut model |

## `domain/model`

| File | Responsibility |
| --- | --- |
| `AppModels.kt` | App, settings, theme, category, icon size, animation speed, mock weather models |
| `WorkspaceModels.kt` | Grid coordinates, page, folder, workspace item models |

## `domain/repository`

| File | Responsibility |
| --- | --- |
| `AppRepository.kt` | Installed-app and system-action contract |
| `WorkspaceRepository.kt` | Workspace persistence contract |
| `SettingsRepository.kt` | Preferences contract |

## `domain/search`

| File | Responsibility |
| --- | --- |
| `AppSearch.kt` | Pure label/category filtering and stable alphabetical sorting |

## `feature/appdrawer`

| File | Responsibility |
| --- | --- |
| `AppDrawerScreen.kt` | Drawer overlay with local search, category tabs, grid, loading/empty states |

## `feature/common`

| File | Responsibility |
| --- | --- |
| `LauncherViewModel.kt` | UI state, overlay state, app loading, haptics, action dispatch, stale-menu cleanup |
| `AppContextMenuScreen.kt` | Anchored, clamped, scrollable context-menu popup |
| `PrototypeInfoDialog.kt` | Implemented/mock/limitation disclosure |

## `feature/folders`

| File | Responsibility |
| --- | --- |
| `FolderModalScreen.kt` | Folder expansion, title editing, app grid |

## `feature/home`

| File | Responsibility |
| --- | --- |
| `HomeScreen.kt` | Wallpaper, pager, workspace grid, glance page, dock, page indicators |
| `ClockDateWidget.kt` | Live clock/date and battery telemetry card |
| `WeatherMockWidget.kt` | Labelled mock weather card |
| `LivingGlanceWidgets.kt` | Mock media and wellbeing cards |
| `LuminousDock.kt` | Persistent dock shelf and drawer handle |

## `feature/search`

| File | Responsibility |
| --- | --- |
| `SearchScreen.kt` | Search overlay, auto-focus, suggestions, results, empty states |

## `feature/settings`

| File | Responsibility |
| --- | --- |
| `SettingsScreen.kt` | Launcher customization controls and prototype-scope entry point |

## Resources

| Path | Responsibility |
| --- | --- |
| `res/drawable/albkestrel_wallpaper.jpg` | Home wallpaper |
| `res/font/plus_jakarta_sans.ttf` | Brand typeface |
| `res/mipmap-*` | Launcher icons |
| `res/values/colors.xml` | Base Android colors |
| `res/values/strings.xml` | App label |
| `res/values/themes.xml` | Android launch theme |
| `res/xml/backup_rules.xml` | Explicit Android 11-and-lower backup include/exclude rules |
| `res/xml/data_extraction_rules.xml` | Explicit Android 12+ cloud-backup and device-transfer rules |

## Tests

All tests currently live under `app/src/test` and run on the JVM with Robolectric where Android
framework behavior is needed.

| File | Coverage |
| --- | --- |
| `AlbkestrelLauncherTest.kt` | Installed-app mapping and DataStore defaults/updates |
| `AppContextMenuPolicyTest.kt` | Action ordering, capability flags, system-app rules, shortcut filtering |
| `LauncherAppContextMenuTest.kt` | Long-press overlay, dismissal, remove, app info, failed uninstall |
| `LauncherHomeLifecycleTest.kt` | HOME intent overlay collapse and page reset |
| `WorkspaceRepositoryTest.kt` | Bootstrap, preservation, migrations, collisions, folders, remove-from-workspace |
| `InstalledAppsMappingTest.kt` | Mapping, exclusion, sorting, deduplication, fallbacks |
| `PackageManagerAppRepositoryTest.kt` | Launch intents, broadcasts, details, uninstall, shortcuts |
| `LivingAppIconAccessibilityTest.kt` | Click/long-click semantics, role, label, 48 dp target, no-long-press fallback |
| `FolderIconAccessibilityTest.kt` | Folder role, click action, 48 dp target |
| `IconMemoryCacheTest.kt` | Bounded cache, eviction, no negative caching |
| `ClockTickerTest.kt` | Minute-aligned delay and reduced-motion pulse gating |
| `AppSearchTest.kt` | Label filtering, ordering, categories, empty results |

Instrumentation tests live under `app/src/androidTest`:

| File | Coverage |
| --- | --- |
| `accessibility/AccessibilityInstrumentationTest.kt` | Device-side role, label, click, and 48 dp checks |

The instrumentation suite compiles with `:app:assembleDebugAndroidTest` but has not been executed
because no device or emulator was attached. See [docs/DEVICE_TEST_MATRIX.md](docs/DEVICE_TEST_MATRIX.md).

## Build files

| File | Responsibility |
| --- | --- |
| `settings.gradle.kts` | Repository policy, plugin management, root name, `:app` module |
| `build.gradle.kts` | Root plugin declarations |
| `app/build.gradle.kts` | Android config, signing, Compose, dependencies, KSP/Room schema output |
| `gradle/libs.versions.toml` | Version catalog |
| `gradle/wrapper/*` | Gradle 9.3.1 wrapper |
| `gradle.properties` | JVM and Android build settings |
| `metadata.json` | AI Studio project metadata; not used by the launcher at runtime |

## Documentation map

| Document | Purpose |
| --- | --- |
| [README.md](README.md) | Project entry point and honest feature status |
| [TODO.md](TODO.md) | Actionable checklist |
| [ROADMAP.md](ROADMAP.md) | Milestones and acceptance criteria |
| [MASTER_PLAN.md](MASTER_PLAN.md) | Long-term vision and engineering strategy |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Actual layer architecture and diagrams |
| [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) | Current verified state and risks |
| [docs/BACKUP_POLICY.md](docs/BACKUP_POLICY.md) | Backup/transfer data policy and validation |
| [docs/DEVICE_TEST_MATRIX.md](docs/DEVICE_TEST_MATRIX.md) | Reproducible device, accessibility, backup, and performance checks |
| [docs/APP_CONTEXT_MENU.md](docs/APP_CONTEXT_MENU.md) | Phase 1.2 behavior and limits |
| [docs/WIDGET_ARCHITECTURE.md](docs/WIDGET_ARCHITECTURE.md) | Proposed widget-host architecture |
| [docs/ICON_ENGINE.md](docs/ICON_ENGINE.md) | Current icon handling and proposed engine |
| [AGENTS.md](AGENTS.md) | Canonical AI/engineering agent rules |
| [GEMINI.md](GEMINI.md) | Gemini/Antigravity entry point |
| [LICENSE](LICENSE) | Complete GNU GPL v3 license text |
| [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) | Third-party dependency and asset license audit |
