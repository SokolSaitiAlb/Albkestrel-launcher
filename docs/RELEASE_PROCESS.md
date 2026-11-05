# Release Process

## 1. Current release state

Albkestrel Launcher has **no published release**. There is no release tag and no signed release
artifact in the repository. The project is licensed under GPL-3.0-or-later
(`SPDX-License-Identifier: GPL-3.0-or-later`); see [LICENSE](../LICENSE).

Do not create a release, tag, GitHub release, or signed artifact without explicit authorization.

## 2. Release blockers

The following must be resolved first:

- [ ] Third-party dependency and asset license audit complete; required notices included. The SIL
      OFL text for Plus Jakarta Sans is present; a full dependency report is still missing.
- [ ] Bundled launcher icon and wallpaper provenance resolved or the artwork replaced. **Release
      blocker.**
- [ ] GPL-3.0-or-later source-distribution and notice obligations verified.
- [x] Phase 1.2 committed on `migration-orca` as `157e446`.
- [ ] Phase 1.2.1 hardening (currently uncommitted) reviewed, committed, and validated.
- [ ] Public `main` integration strategy approved; do not modify `main` before that.
- [x] Unused Firebase/AppCheck dependencies removed and merged manifest checked (uncommitted).
- [x] Merged manifest permissions verified: no Firebase/GMS/Play Core component and no `INTERNET`
      permission in the debug merged manifest.
- [x] Backup/data-extraction policy finalized and XML-validated (uncommitted).
- [x] Accessibility click semantics corrected and covered by a focused test.
- [x] Phase 1.2 owner-reported physical validation completed (Pixel 8a, 12/12).
- [ ] Independent release-scope physical acceptance completed across the device matrix, including
      the pending Phase 1.2.1 checks.
- [ ] `connectedDebugAndroidTest` executed on a device.
- [ ] Version code and name chosen.
- [ ] Signing material created outside the repository.
- [ ] CI green on the release commit.
- [ ] Release notes reviewed for accurate claims.

## 3. Versioning

The build currently declares:

```kotlin
versionCode = 1
versionName = "1.0"
```

These are build values, not a released version. Before a release:

- Choose a version policy (for example semantic versioning for the launcher product).
- Increment `versionCode` for every published artifact.
- Set `versionName` to the user-visible release version.
- Update [CHANGELOG.md](../CHANGELOG.md) under a new version heading.
- Update [docs/PROJECT_STATUS.md](PROJECT_STATUS.md).

Do not invent a version history that does not exist.

## 4. Signing

The release build reads:

| Variable | Purpose | Default |
| --- | --- | --- |
| `KEYSTORE_PATH` | Path to the release keystore | `<repo>/my-upload-key.jks` |
| `STORE_PASSWORD` | Keystore password | none |
| `KEY_PASSWORD` | Key password | none |
| key alias | Fixed in the build | `upload` |

The debug build reads `DEBUG_KEYSTORE_PATH`, defaulting to `~/.android/debug.keystore`.

Rules:

- Never commit a `.jks`, `.keystore`, password, or `local.properties`.
- Never put signing secrets in documentation, source, or CI logs.
- Do not add release signing secrets to the current CI workflow.
- Store release material in an approved secret manager or local secure location.
- Back up the keystore; losing it prevents updates for the same application id.

### 4.1 License compliance

The project is distributed under GPL-3.0-or-later, not a proprietary license.

Before distributing a binary:

- Include [LICENSE](../LICENSE) and [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md) with the
  release.
- Make the corresponding source available under GPL-3.0-or-later.
- Include the SIL OFL text and Plus Jakarta Sans notices because the font is bundled; the text is at
  `licenses/PlusJakartaSans-OFL.txt`.
- Keep all third-party notices and do not relicense dependencies or assets.
- Resolve or replace the bundled launcher icon and wallpaper while their provenance is unknown.
- Do not add a dependency or asset whose license is incompatible with GPL distribution.

Google Play Billing may be added as an optional future monetization feature. Paid distribution and
paid subscriptions are compatible with the GPL, provided recipients retain their GPL rights and the
corresponding source remains available.

## 5. Pre-release verification

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
git diff --check
```

Then, with authorized signing material:

```bash
KEYSTORE_PATH=/secure/path/upload.jks \
STORE_PASSWORD=... \
KEY_PASSWORD=... \
./gradlew :app:assembleRelease
```

Record:

- Exact commit.
- Command output summary.
- Test counts.
- Artifact path.
- Artifact size.
- SHA-256 checksum.
- Device/API validation details.

## 6. Physical-device acceptance

Minimum recommended matrix:

| Device class | API | Focus |
| --- | --- | --- |
| Minimum supported | 24 | Package discovery, launch, drawer, workspace |
| Shortcut boundary | 25 | Shortcut discovery/launch and fallback |
| Modern | 31+ | Dynamic color plumbing, vibrator manager, edge-to-edge |
| Current target | 36 | Target SDK behavior, insets, permissions |

The Phase 1.2 owner report covers 12 scenarios on one Pixel 8a. It predates the Phase 1.2.1 changes
and does not satisfy the full release matrix below; the remaining rows and devices still need
independent validation. Use [DEVICE_TEST_MATRIX.md](DEVICE_TEST_MATRIX.md) for the reproducible
accessibility, functional, backup, and performance checks.

Acceptance checklist:

- [ ] Install and launch.
- [ ] Select as home app.
- [ ] Home button collapses overlays.
- [ ] Real apps and icons render.
- [ ] App launch works.
- [ ] Package change refresh works.
- [ ] Drawer/search/folders work.
- [ ] Settings persist.
- [ ] Long press opens the menu from all surfaces.
- [ ] Menu placement is usable in portrait and landscape.
- [ ] Outside tap, back, and action dismissal work.
- [ ] App info opens the correct screen.
- [ ] Remove from launcher preserves the installed package.
- [ ] Uninstall shows the system confirmation.
- [ ] System apps do not offer uninstall.
- [ ] Shortcuts work where supported; they are absent otherwise.
- [ ] TalkBack can open the menu.
- [ ] Haptics respect the setting.
- [ ] No crash on package removal during use.
- [ ] No unexpected permission prompt.

## 7. Build artifact handling

Debug artifact:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The CI workflow uploads this as a workflow artifact. It is not a release.

Release artifact:

```text
app/build/outputs/apk/release/app-release.apk
```

Do not publish it automatically from CI. Publish manually only after authorization.

## 8. Changelog and release notes

- Follow Keep a Changelog conventions.
- Move relevant `Unreleased` entries into the new version section.
- Group under Added, Changed, Deprecated, Removed, Fixed, Security.
- State known limitations.
- Do not claim device validation unless it happened.
- Do not describe mock cards as live services.
- Do not describe planned widgets or icon packs as delivered.

## 9. Tagging and publishing

Only after explicit authorization:

1. Commit the release preparation.
2. Create a signed or annotated tag according to the project policy.
3. Push the tag only when authorized.
4. Create the GitHub release manually.
5. Attach the signed APK and checksum.
6. Include release notes and known limitations.

No automated release publishing exists in this repository.

## 10. Rollback

If a release has a critical defect:

- Stop distribution.
- Document the defect and affected versions.
- Prepare a patch release with an incremented `versionCode`.
- Do not rewrite a published tag or replace a published artifact silently.
- Communicate the issue and mitigation.

## 11. Post-release

- Update the status ledger.
- Record the release commit and artifact checksum.
- Close completed roadmap items.
- File follow-up issues for known limitations.
- Keep the previous release artifact available for comparison.
