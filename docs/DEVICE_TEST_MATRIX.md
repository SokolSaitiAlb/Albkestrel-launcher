# Device Test Matrix — Phase 1.2.1

Status: **Not executed.** No Android device or emulator was connected in this environment
(`adb devices` returned an empty list). Everything below is a reproducible manual and automated
checklist. Record results in the table at the end; do not mark a row PASS without running it.

Target device: Pixel 8a, Android 17 (API 37), gesture navigation, TalkBack available.

## Prerequisites

```bash
# From the repository root, on branch migration-orca.
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest

# Confirm exactly one device is attached and authorized.
adb devices -l
```

If `adb devices` does not show `device`, do not continue. `unauthorized` means the RSA prompt on
the phone has not been accepted.

## Install and launch

```bash
# Replace with the actual APK path if the build directory differs.
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

# Launch the launcher activity directly (the device may ask which HOME app to use).
adb shell am start -n com.albkestrel.launcher/.app.MainActivity
```

Select **AlbkestrelOS** as the default HOME app when prompted. The `HOME` role is what exercises
the lifecycle and `onNewIntent()` behaviour.

## Automated checks

```bash
./gradlew :app:connectedDebugAndroidTest

# Or run only the accessibility suite.
adb shell am instrument -w \
  -e class com.albkestrel.launcher.accessibility.AccessibilityInstrumentationTest \
  com.albkestrel.launcher.test/androidx.test.runner.AndroidJUnitRunner
```

Expected: `AccessibilityInstrumentationTest` passes. It asserts the button role, spoken label,
click action, and 48 dp minimum target for the app and folder primitives.

## Manual accessibility checks

| ID | Check | Steps | Pass criteria | Status |
| --- | --- | --- | --- | --- |
| A11Y-01 | TalkBack app icon | Enable TalkBack, focus a home app icon. | Announces "Open <app name>", role Button. Double-tap launches. | PENDING |
| A11Y-02 | TalkBack app menu | Focus an app icon, use TalkBack actions. | "Open app menu" opens the context menu at the icon. | PENDING |
| A11Y-03 | TalkBack folder | Focus a folder icon. | Announces the folder label, role Button. Double-tap opens the folder. | PENDING |
| A11Y-04 | TalkBack search | Focus the home quick-search pill and open search. | Announces "Open app search"; search field receives focus; results announce "Launch <app>". | PENDING |
| A11Y-05 | TalkBack drawer | Open the app drawer from the dock handle. | Handle and close button announce a close-drawer action; tabs are selectable. | PENDING |
| A11Y-06 | TalkBack settings | Open settings. | Close button and every choice row are reachable and announce selected state. | PENDING |
| A11Y-07 | TalkBack telemetry | Focus the clock widget telemetry row. | Battery announces "<n> percent" (and ", charging" when plugged in); Wi-Fi/cellular glyphs are silent. | PENDING |
| A11Y-08 | TalkBack media mock | Focus the media glance widget. | Previous/Play or Pause/Next announce their controls. | PENDING |
| A11Y-09 | TalkBack wellbeing | Focus the wellbeing widget. | Announces "Open wellbeing settings" and opens settings. | PENDING |
| A11Y-10 | Touch target size | Enable "Display size" at default, inspect all icon buttons and the drawer handle. | Every interactive target is at least 48 × 48 dp; no target requires precise tapping. | PENDING |
| A11Y-11 | Keyboard / D-pad | Connect a keyboard or use D-pad. | Focus is visible; Enter, Numpad Enter, or Space activates a focused app icon. | PENDING |
| A11Y-12 | Switch access | Enable Switch Access and scan the home screen. | Every actionable element is reachable and labelled; no focus trap. | PENDING |
| A11Y-13 | Font scale | Set font size to the largest setting. | Labels wrap or ellipsize without overlapping controls; targets remain at least 48 dp. | PENDING |
| A11Y-14 | Reduced motion | Settings → Fluid Motion & Accessibility → Reduced Motion. | Clock status dot stops pulsing; navigation still works. | PENDING |

## Functional and lifecycle checks

| ID | Check | Steps | Pass criteria | Status |
| --- | --- | --- | --- | --- |
| FUNC-01 | Default HOME | Set AlbkestrelOS as default HOME, press Home from another app. | Launcher resumes on the last page with the correct overlay state. | PENDING |
| FUNC-02 | `onNewIntent()` | Open the drawer, press Home from the drawer. | Overlay collapses; no duplicate activity instance. | PENDING |
| FUNC-03 | Workspace persistence | Add/remove apps, create a folder, rename it, force-stop, relaunch. | Layout and folder title survive. | PENDING |
| FUNC-04 | Settings persistence | Change theme, grid, icon size, animation speed, haptics, dock labels; force-stop and relaunch. | Every setting survives. | PENDING |
| FUNC-05 | Package change | Install and uninstall a launcher app while the launcher is running. | Drawer updates; stale icons do not remain. | PENDING |
| FUNC-06 | Shortcut discovery | Long-press an app that publishes dynamic shortcuts. | Menu shows shortcuts; main thread is not blocked while the menu opens. | PENDING |
| FUNC-07 | Missing app | Restore a layout referencing an uninstalled package. | Item shows as unavailable/dimmed without crashing. | PENDING |

## Backup and restore checks

Prerequisites: a Google account with Backup enabled, a secure screen lock, and a second device or
a factory-reset path. See [BACKUP_POLICY.md](BACKUP_POLICY.md) for the data model.

```bash
# Inspect what the platform would back up without uploading it.
adb shell bmgr enabled
adb shell bmgr list transports

# Force a local backup pass for this package (device must be idle).
adb shell bmgr backupnow com.albkestrel.launcher

# Restore after reinstall or factory reset.
adb shell bmgr restore com.albkestrel.launcher
```

| ID | Check | Steps | Pass criteria | Status |
| --- | --- | --- | --- | --- |
| BAK-01 | Cloud backup transport | Run `bmgr backupnow com.albkestrel.launcher`. | Backup completes without error and reports the package. | PENDING |
| BAK-02 | Encryption requirement | Remove the screen lock, then attempt cloud backup. | Cloud backup is skipped (Android 12+ `disableIfNoEncryptionCapabilities`). D2D is unaffected. | PENDING |
| BAK-03 | Cloud restore | Uninstall, reinstall, restore from the same account. | Workspace pages, folders, and settings return exactly. | PENDING |
| BAK-04 | Device-to-device | Use the setup wizard's copy step between two devices. | Same data returns; caches and decoded icons are not transferred. | PENDING |
| BAK-05 | Legacy prefs migration | Restore a build that still has `albkestrel_launcher_prefs.xml`. | DataStore migration reads the restored legacy preferences. | PENDING |
| BAK-06 | Factory reset | Back up, factory reset, restore during setup. | Launcher opens with restored data; an intentionally empty workspace is not re-seeded. | PENDING |
| BAK-07 | Directory exclusions | Inspect the backup set. | `cache`, `code_cache`, `no_backup`, and APK/signing files are absent. | PENDING |

## Performance checks

| ID | Check | Steps | Pass criteria | Status |
| --- | --- | --- | --- | --- |
| PERF-01 | Icon cache bound | Scroll the drawer through 100+ apps, then check memory. | Heap does not grow without bound; cache is capped at 32 MiB. | PENDING |
| PERF-02 | Cache invalidation | Install/uninstall an app, reopen the drawer. | Icon for the changed package refreshes; other icons do not flicker. | PENDING |
| PERF-03 | No main-thread I/O | Long-press an app with shortcuts; watch `adb shell top`. | Menu appears promptly; no ANR; shortcut lookup runs off the main thread. | PENDING |
| PERF-04 | Clock ticker | Leave the launcher on the home screen for 5 minutes; observe CPU. | No per-second recomposition; clock updates at minute boundaries. | PENDING |

## Firebase and manifest checks

```bash
# The merged manifest must not contain Firebase, GMS, Play Core, or INTERNET.
adb shell dumpsys package com.albkestrel.launcher | grep -iE 'firebase|gms|playcore|INTERNET' || echo 'clean'
```

Expected: no matches. The debug merged manifest was already verified in the build output; this
confirms the installed package matches.

## Results

| Date | Device / API | Build | Automated | A11Y | FUNC | BAK | PERF | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| — | Pixel 8a / Android 17 | — | PENDING | PENDING | PENDING | PENDING | PENDING | No device attached in this environment. |

Do not change `PENDING` to `PASS` without an actual run. Attach logs or screenshots for failures.
