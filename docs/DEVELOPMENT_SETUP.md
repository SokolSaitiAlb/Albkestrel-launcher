# Development Setup

## 1. Prerequisites

| Tool | Requirement |
| --- | --- |
| JDK | 17 |
| Android Studio | Recent stable release with Android Gradle Plugin 9.1.1 support |
| Android SDK | Platform 36 with the minor API level 1 package required by `compileSdk` |
| Gradle | Use the committed wrapper (`./gradlew`); do not install a separate Gradle |
| Git | Any current version |

The wrapper uses Gradle 9.3.1. The build compiles Java/Kotlin bytecode for Java 11, but the Gradle
daemon itself requires a modern JDK. JDK 17 is the supported setup and CI target.

## 2. Verify Java

```bash
java -version
```

Expected major version: `17`.

If multiple JDKs are installed, point `JAVA_HOME` at JDK 17 before invoking Gradle.

## 3. Install the Android SDK

In Android Studio:

1. Open **SDK Manager**.
2. Install **Android SDK Platform 36**.
3. Install the minor API level 1 package required by:

```kotlin
compileSdk { version = release(36) { minorApiLevel = 1 } }
```

4. Install SDK command-line tools and platform tools.

The build also needs the SDK packages referenced by Android Gradle Plugin 9.1.1. Android Studio
normally installs them automatically.

## 4. Create `local.properties`

The repository ignores `local.properties`. Create it locally:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

Linux/macOS example:

```properties
sdk.dir=/home/you/Android/Sdk
```

Windows example:

```properties
sdk.dir=C\:\\Users\\you\\AppData\\Local\\Android\\Sdk
```

Do not commit this file.

## 5. Secrets and `.env`

The build applies the Secrets Gradle plugin and reads `.env`, falling back to `.env.example`. The
current launcher features do not require an API key.

- `.env` is ignored by Git.
- `.env.example` contains only a commented placeholder.
- The Google Services plugin is configured with `MissingGoogleServicesStrategy.WARN`, so a missing
  `google-services.json` should not fail the build.
- Do not add real keys to `.env.example`.
- Unused Firebase/AppCheck dependencies are a cleanup item; see [SECURITY.md](../SECURITY.md).

## 6. Open the project

```bash
cd /path/to/Albkestrel-launcher
git switch migration-orca
```

Open the repository root in Android Studio. Let Gradle sync use the wrapper.

Do not let the IDE silently upgrade the Android Gradle Plugin, Gradle wrapper, or Kotlin version
without a reviewed build change.

## 7. Build

```bash
./gradlew :app:assembleDebug
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 8. Run unit tests

```bash
./gradlew :app:testDebugUnitTest
```

Reports:

```text
app/build/reports/tests/testDebugUnitTest/index.html
app/build/test-results/testDebugUnitTest/
```

## 9. Install and select as home app

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Then select the launcher as the home app:

- Stock Android: **Settings → Apps → Default apps → Home app**.
- Some OEM builds use a different path under **Settings → Home** or **Settings → Apps → Default**.
- If the launcher is not listed, confirm the APK is installed for the current user and that the
  HOME intent filter is present in the merged manifest.

## 10. Release signing (only when authorized)

The release build expects a keystore. Defaults:

```text
KEYSTORE_PATH      default: <repo>/my-upload-key.jks
STORE_PASSWORD     required
KEY_PASSWORD       required
key alias          upload
```

The debug build uses:

```text
DEBUG_KEYSTORE_PATH default: ~/.android/debug.keystore
store password      android
key alias           androiddebugkey
key password        android
```

Never commit `.jks`, `.keystore`, passwords, or `local.properties`. See
[RELEASE_PROCESS.md](RELEASE_PROCESS.md).

## 11. Common problems

### `SDK location not found`

Create `local.properties` with a valid `sdk.dir`, or set `ANDROID_HOME`.

### Unsupported class file major version / Gradle JVM error

Use JDK 17:

```bash
java -version
./gradlew --version
```

Check that the Gradle daemon JVM is also 17.

### `compileSdk` package missing

Install the required SDK Platform 36 minor API package in SDK Manager. Do not lower `compileSdk`
just to make the build pass.

### Missing `google-services.json`

The project is configured to warn rather than fail. If the behavior changes, do not add a fake
configuration file; investigate the Google Services plugin configuration instead.

### Release signing failure

The release keystore is not in the repository. Provide `KEYSTORE_PATH`, `STORE_PASSWORD`, and
`KEY_PASSWORD`, or use a local keystore with the expected alias. Do not commit signing material.

### Gradle wrapper missing

The wrapper JAR and properties are committed. Do not fabricate wrapper files. If they are missing
from a checkout, report the repository issue and restore them from version control.

### Untracked `skills/` directory

Some agent workspaces contain an untracked `skills/` directory. It is external tooling content, not
part of the Android app. Do not stage or commit it.

## 12. Environment checklist

- [ ] JDK 17 active.
- [ ] Android SDK Platform 36 (minor API 1) installed.
- [ ] `local.properties` points to the SDK.
- [ ] Gradle wrapper is present and executable.
- [ ] `./gradlew --version` reports Gradle 9.3.1 and a JDK 17 daemon.
- [ ] No secrets or signing files are tracked.
- [ ] Worktree is on the requested branch and existing changes are preserved.
