# Backup and Data Extraction Policy

Status: Implemented for Phase 1.2.1. No cloud backup or device-to-device (D2D) restore has been
executed in this environment; see [Validation](#validation) for the distinction between XML
validation and an actual restore test.

## Principles

- Back up only user-created launcher configuration: workspace layout, folders, and preferences.
- Do not back up disposable caches, decoded icon bitmaps, temporary files, or build artefacts.
- Keep cloud backup and device-to-device transfer rules identical for user data.
- Require encryption capabilities for cloud backup on Android 12+.
- Preserve Room schema compatibility so a restored database can be opened and migrated.

## Persisted app data

| Data | Location | Backup |
| --- | --- | --- |
| Workspace pages, items, folder contents, and metadata | Room database `albkestrel_launcher.db` in the app database directory | Included |
| Launcher preferences (theme, grid, icon size, animation speed, haptics, dock labels, wallpaper dim) | DataStore `files/datastore/albkestrel_launcher_settings.preferences_pb` | Included |
| Legacy preferences retained for the DataStore migration | `shared_prefs/albkestrel_launcher_prefs.xml` | Included |
| Decoded icon bitmaps and other in-memory caches | Process memory / runtime cache | Not persisted; never included |
| Cache, code cache, and `no_backup` directories | `cacheDir`, `codeCacheDir`, `noBackupFilesDir` | Excluded by Android and by omission from the rules |
| Search query and recent-item UI state | In-memory state only | Not applicable; not persisted |
| APK, signing material, and build output | Project / device package storage | Not app user data; never included |

There is no export/import feature and no user-imported wallpaper or image store. The bundled
wallpaper and icon resources ship inside the APK and are not backed up.

## Cloud backup

- `android:allowBackup="true"` in `AndroidManifest.xml`.
- Android 12+ uses `res/xml/data_extraction_rules.xml` with
  `disableIfNoEncryptionCapabilities="true"`. If the device cannot provide encryption
  capabilities (for example, no secure screen lock), cloud backup is skipped.
- Android 11 and lower use `res/xml/backup_rules.xml`. Android 9+ encrypts Auto Backup data with
  the user's screen-lock secret. The legacy XML format cannot require cloud encryption
  independently of a D2D transfer, so the explicit encryption requirement is enforced only on
  Android 12+; older devices rely on the platform backup transport.
- The include list is exhaustive. Because `<include>` elements are present, Auto Backup does not
  fall back to "back up almost everything"; files outside the list are excluded.

## Device-to-device transfer

- Android 12+ uses the `<device-transfer>` section of `data_extraction_rules.xml` with the same
  three include rules. D2D transfer is not blocked by the cloud encryption requirement because
  data is not sent to a server.
- Android 11 and lower use the same legacy include list for cloud and D2D transfers.
- `android:allowBackup="false"` is not used. It would also be misleading as a D2D control: on some
  Android 12+ vendor implementations, `allowBackup="false"` disables cloud backup but not D2D
  transfer. The launcher therefore makes an explicit positive policy instead of relying on that
  flag.

## Restore compatibility

- The Room database is version 3 and ships migrations 1 to 2 and 2 to 3. A restored database from an
  older install is upgraded by those migrations; a database restored from a newer install would be
  rejected by Room rather than silently downgraded.
- The DataStore file is restored before the app reads settings. `SharedPreferencesMigration` keeps
  the legacy preferences path readable during the transition.
- Restore occurs before the launcher is launched, so the first workspace read sees restored data
  rather than seeded defaults. The `workspace_metadata` pre-v3 marker is part of the database and is
  restored with it, preventing an intentionally emptied workspace from being reseeded.
- Workspace rows reference installed package names, not APK contents. Restoring a layout on a device
  without those apps is expected to show unavailable apps; the repository resolves them as not
  installed rather than crashing.

## Validation

Validated in this environment:

- XML well-formedness of both rule files.
- Manifest references to `@xml/backup_rules` and `@xml/data_extraction_rules`.
- Merged debug manifest retains `allowBackup`, `fullBackupContent`, and `dataExtractionRules`.
- Room migration 1 to 3 is covered by the unit test
  `migration 1 to 3 executes cleanly and preserves existing folder items`.
- Debug build and unit tests pass.

Not executed in this environment (no connected device or emulator):

- Actual cloud backup upload and restore.
- Actual device-to-device transfer.
- Restore after a factory reset.
- Restore with the launcher selected as the default HOME app.

A reproducible device checklist lives in [DEVICE_TEST_MATRIX.md](DEVICE_TEST_MATRIX.md). Relevant
ADB commands are included there for when a device is available.
