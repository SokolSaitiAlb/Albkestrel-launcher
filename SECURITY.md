# Security Policy

## Supported versions

Albkestrel Launcher has no published release. The only maintained development state is the
`migration-orca` branch. The public `main` branch currently contains the initial Android scaffold
and is not a supported launcher release.

| Version | Supported |
| --- | --- |
| Released builds | None |
| `migration-orca` development work | Best-effort, no security SLA |
| `main` scaffold | No |

## Reporting a vulnerability

Do not open a public issue for a suspected vulnerability involving data loss, permission abuse,
package removal, intent hijacking, or secret exposure.

Use GitHub's private vulnerability reporting for this repository:

<https://github.com/SokolSaitiAlb/Albkestrel-launcher/security/advisories/new>

You can also reach the same form through **Repository → Security → Advisories → Report a
vulnerability**.

If private reporting is unavailable, contact the repository owner through their GitHub profile and
request a private channel. Do not invent or publish a security email address. No private security
email is documented in this repository.

Please include:

- Affected branch and commit.
- Android version and device/emulator details.
- Reproduction steps.
- Expected and observed behavior.
- Impact assessment.
- Whether the issue requires a malicious app, physical access, or a specific permission.
- Any suggested mitigation.

Do not include real signing keys, credentials, or personal data in a report.

Licensing questions are not security vulnerabilities. Send those to the repository owner through the
public issue tracker or GitHub profile instead of the private security channel.

## Scope

In scope:

- The launcher application in this repository.
- Package discovery, launching, app details, uninstall, and shortcut handling.
- Room and DataStore data handling.
- Manifest permissions and exported components.
- Intent handling and HOME-role behavior.
- Build and dependency configuration that affects the shipped APK.
- Documentation that makes unsafe or misleading security claims.

Out of scope:

- The Android operating system or vendor firmware.
- Third-party applications launched by the launcher.
- Issues requiring root or a compromised device, unless the launcher makes exploitation worse.
- Denial of service caused solely by an unsupported or malformed third-party app.
- Social engineering not involving the launcher.

## Permission minimization

Current source manifest:

- `android.permission.VIBRATE` — used for optional haptic feedback.
- No network, location, contacts, camera, microphone, SMS, storage, notification-listener, or
  accessibility-service permission is declared.
- Package visibility is scoped with a `<queries>` entry for `MAIN`/`LAUNCHER` activities.

Important caveat: `app/build.gradle.kts` still includes Firebase AI, Firebase AppCheck, the Firebase
BOM, the Secrets plugin, and the Google Services plugin even though launcher code does not use them.
Dependency manifest merging can add permissions. Before release:

```bash
./gradlew :app:processDebugMainManifest
# Inspect the merged manifest under app/build/intermediates/
# and verify the packaged APK permissions with aapt/apkanalyzer.
```

Remove unused dependencies rather than assuming the source manifest is the final permission set.

## Data handling

| Data | Where it lives | Notes |
| --- | --- | --- |
| Workspace layout and folders | Room database on device | Launcher-owned; no server sync |
| Preferences | DataStore on device | Icon size, theme, motion, haptics, dock labels |
| Installed-app metadata | In-memory cache | Rebuilt from `PackageManager` |
| App icons | In-memory `ImageBitmap` cache | Cleared on package changes |
| Battery state | In-memory | From a sticky system broadcast |
| Weather/media/wellbeing values | In-memory mock constants | No live service |
| Crash reports | None | No crash-reporting SDK wired into launcher code |
| Analytics/telemetry | None | No analytics SDK wired into launcher code |

No launcher feature intentionally transmits personal data off the device. If that changes, the
change requires an explicit privacy review, documentation, and user consent where appropriate.

## Local-first privacy

- The launcher does not require an account.
- The launcher does not require a cloud service.
- The launcher does not include advertising.
- The launcher does not collect usage analytics.
- Mock cards are labelled to avoid implying live data collection.
- Package queries are used to render launchable apps, not to build a remote profile.

## Backup behavior

`android:allowBackup="true"` is currently set. The backup and data-extraction XML files are still
sample templates, which means the platform default backup behavior may apply. Before release, decide
explicitly whether workspace layouts and preferences should be included in cloud backup or device
transfer, and encode that decision in both XML files.

Users who require strict local-only storage should understand that Android's OS-level backup is
outside the launcher's control unless the app excludes its data.

## Dependency security

Current risks:

- Unused Firebase/AppCheck dependencies increase the dependency and permission surface.
- The version catalog includes optional libraries that are not used; commented dependencies are not
  part of the build, but the active Firebase entries are.
- There is no Dependabot or dependency-verification configuration yet.
- No software bill of materials is generated.

Recommended practices:

- Remove unused dependencies and plugins.
- Keep the version catalog as the single source of dependency versions.
- Review dependency updates for permissions and transitive changes.
- Run dependency vulnerability checks before release.
- Do not add a network, analytics, or cloud dependency without a documented product need and a
  privacy review.
- Never commit signing keys or service-account files.

## Android platform security boundaries

- The launcher uses public Android APIs only.
- Uninstall delegates to Android's official `ACTION_DELETE` confirmation flow.
- The launcher never removes packages silently.
- System packages are blocked from uninstall in both domain policy and the repository.
- App details delegate to the system settings screen.
- Shortcut access is guarded by API level and launcher shortcut-host permission.
- The package-change receiver is registered as not exported.
- No accessibility service, device-admin privilege, root access, or hidden API is used.
- The launcher does not draw overlays over other applications.
- HOME-role selection is owned by the user and the system.

## Destructive-action safety

- **Remove from launcher** deletes only launcher database references.
- **Uninstall** initiates the system flow and requires user confirmation.
- **System app uninstall** is refused even if a caller bypasses the UI.
- Missing or uninstalled packages never produce a context menu.
- A context menu closes if its package disappears during refresh.

## Security checklist before release

- [ ] Remove unused dependencies and verify the merged manifest.
- [ ] Confirm the APK requests only intentional permissions.
- [ ] Confirm no secrets, keys, `.env`, or `local.properties` are tracked.
- [ ] Finalize backup/data-extraction policy.
- [ ] Review exported components and intent filters.
- [ ] Run dependency vulnerability checks.
- [ ] Run unit tests and a release build.
- [ ] Validate uninstall, app details, and shortcut behavior on a physical device.
- [ ] Review the changelog for security-relevant changes.
- [ ] Confirm GPL-3.0-or-later compliance and that third-party notices are current.
- [ ] Publish with a documented security contact path.

## Disclosure

There is no bug-bounty program. Good-faith reports will be acknowledged on a best-effort basis.
Please allow reasonable time for a fix before public disclosure.
