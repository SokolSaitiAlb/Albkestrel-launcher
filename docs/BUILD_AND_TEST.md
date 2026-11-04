# Build and Test

## 1. Toolchain

| Component | Value |
| --- | --- |
| Gradle wrapper | 9.3.1 |
| Android Gradle Plugin | 9.1.1 |
| Kotlin | 2.2.10 |
| Compose BOM | 2024.09.00 |
| JDK | 17 |
| Java bytecode target | 11 |
| `compileSdk` | 36 (minor API level 1) |
| `minSdk` | 24 |
| `targetSdk` | 36 |

## 2. Standard verification

Run from the repository root:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
git diff --check
```

Expected results:

- Unit tests pass.
- The debug APK assembles.
- No whitespace errors are reported.

The latest recorded Phase 1.2 validation was **64 tests, 0 failures, 0 errors, 0 skipped**, with a
successful debug assembly. Re-run the commands to produce current evidence; do not rely on a history
entry. The project owner separately reports 12/12 physical scenarios passed on a Google Pixel 8a;
that report is not a substitute for the commands above.

## 3. Unit tests

```bash
./gradlew :app:testDebugUnitTest
```

Reports:

```text
app/build/reports/tests/testDebugUnitTest/index.html
app/build/test-results/testDebugUnitTest/
```

The suite uses:

- JUnit 4.
- Robolectric for Android framework behavior.
- Kotlin coroutine test utilities.
- In-memory Room for repository tests.
- `ApplicationProvider` for Robolectric application context.
- `@Config(sdk = [34])` on Robolectric test classes.

### Test inventory

| Test class | Focus |
| --- | --- |
| `AlbkestrelLauncherTest` | Installed-app mapping, DataStore defaults and updates |
| `AppContextMenuPolicyTest` | Action ordering, capability flags, system-app rules, shortcuts |
| `LauncherAppContextMenuTest` | Long-press overlay, dismissal, remove, app info, failed uninstall |
| `LauncherHomeLifecycleTest` | HOME intent collapse and page reset |
| `WorkspaceRepositoryTest` | Bootstrap, preservation, migration, collisions, folders, remove |
| `InstalledAppsMappingTest` | Mapping, exclusion, sort, dedupe, fallbacks |
| `PackageManagerAppRepositoryTest` | Launch intents, broadcasts, details, uninstall, shortcuts |
| `LivingAppIconAccessibilityTest` | Click/long-click semantics and no-long-press fallback |
| `AppSearchTest` | Label filtering, ordering, categories, empty results |

Total: 64 tests.

### What unit tests do not prove

Robolectric does not prove:

- HOME-role behavior on a real device.
- Real package broadcasts.
- Real uninstall confirmation.
- Shortcut-host permission behavior.
- Widget rendering.
- Haptic hardware.
- OEM popup/inset behavior.
- TalkBack traversal and announcements.

The owner's Pixel 8a report covers basic TalkBack click/long-press and the core context-menu flows.
Everything else above remains pending device validation.

## 4. Debug build

```bash
./gradlew :app:assembleDebug
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The debug build uses the debug signing configuration. It is installable but not a release artifact.

## 5. Release build

Only when explicitly authorized:

```bash
KEYSTORE_PATH=/secure/path/upload.jks \
STORE_PASSWORD=... \
KEY_PASSWORD=... \
./gradlew :app:assembleRelease
```

The release signing configuration uses alias `upload`. See [RELEASE_PROCESS.md](RELEASE_PROCESS.md).

Do not run a release build with placeholder credentials and report it as a successful release.

## 6. Lint

```bash
./gradlew :app:lintDebug
```

Lint findings are not currently part of the recorded acceptance gate. Treat new errors as blockers
and review warnings rather than suppressing them blindly.

## 7. Instrumentation tests

There is no `app/src/androidTest` source set yet. The test runner dependency is configured, but
`connectedDebugAndroidTest` has no tests to run.

When the suite is added:

```bash
./gradlew :app:connectedDebugAndroidTest
```

This requires a connected device or emulator and should be reported with the API level and device
model.

## 8. Install and smoke test

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.albkestrel.launcher/.app.MainActivity
```

Manual smoke checklist:

- [ ] Launcher starts without a crash.
- [ ] Installed apps appear.
- [ ] App launch works.
- [ ] App drawer opens and closes.
- [ ] Search returns matches.
- [ ] Dock items persist after restart.
- [ ] Folder opens, title edits, and persists.
- [ ] Long press opens the context menu.
- [ ] App info opens the system screen.
- [ ] Remove from launcher does not uninstall.
- [ ] Uninstall shows the system confirmation.
- [ ] System apps do not show uninstall.
- [ ] Outside tap and back dismiss the menu.
- [ ] Home button collapses overlays.
- [ ] Settings persist after restart.

## 9. APK evidence

When an APK is produced, record:

```bash
ls -l app/build/outputs/apk/debug/app-debug.apk
sha256sum app/build/outputs/apk/debug/app-debug.apk
```

Do not claim an artifact exists unless the path was verified.

## 10. CI parity

The GitHub workflow runs:

- Wrapper validation.
- JDK 17 setup.
- Unit tests.
- Debug assembly.
- Debug APK and test-report upload.

The workflow has not run on GitHub yet. CI does not run release signing, publish releases, or run
instrumentation tests. A green CI run is
not a substitute for physical-device validation.

## 11. Clean build guidance

Avoid destructive cleanup of user work. If a build cache issue is suspected:

```bash
./gradlew clean
```

This deletes generated build outputs only; it does not delete source or uncommitted work. Do not run
`git clean`, `git reset`, or `git checkout` to solve build problems.

## 12. Troubleshooting test failures

- Check the HTML report for the exact assertion.
- Confirm the test uses the correct coroutine dispatcher.
- Confirm Robolectric SDK configuration.
- For Room tests, confirm the database is closed in `@After`.
- For DataStore tests, remember the DataStore is process-scoped; use a fresh context or file when
  isolation matters.
- Do not delete a failing test to make the suite green. Fix the behavior or document the limitation.
