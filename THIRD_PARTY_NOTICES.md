# Third-Party Notices and Asset Audit

Albkestrel Launcher is licensed under **GPL-3.0-or-later**; see [LICENSE](LICENSE). That license
covers original project code and documentation. It does not relicense third-party software, fonts,
icons, images, or other assets. Every third-party item keeps its own license and must be audited
individually.

This file records the current audit state. It is not a substitute for the license text shipped with
a dependency or asset.

## Project authorship

Repository history associates all commits to date with the GitHub account `SokolSaitiAlb` (display
name `Alb Kestrel`). No separate legal entity, legal name, or copyright assignment is documented in
this repository. Copyright in a contribution remains with its author.

The Phase 1.2 change set is committed on `migration-orca` as `157e446` under the repository's
configured author identity and is treated as the same owner's work. It is owner-reported physically
validated on a Google Pixel 8a, but that does not affect authorship or licensing. No third-party
copyright notices or SPDX headers were found in the launcher source during this audit. If
third-party source is added later, record its origin and license here.

## Dependencies

The build and version catalog reference the following dependency families. Some are currently
commented out or unused. Each remains under its upstream license. The authoritative license text is
the one distributed with the resolved artifact or its source repository; this table does not replace
it.

| Dependency family | Used for | Known upstream license | Audit status |
| --- | --- | --- | --- |
| Kotlin standard library and Kotlin Coroutines | Language runtime and async | Apache-2.0 | Compatible with GPLv3; verify resolved artifacts and notices |
| AndroidX / Jetpack Compose / Lifecycle / Navigation / Activity | UI, lifecycle, navigation | Apache-2.0 | Compatible with GPLv3; verify resolved artifacts and notices |
| AndroidX Room and DataStore | Local persistence and preferences | Apache-2.0 | Compatible with GPLv3; verify resolved artifacts and notices |
| Coil | Image loading | Apache-2.0 | Currently commented out; verify if enabled |
| Retrofit, OkHttp, Moshi | Networking and serialization | Apache-2.0 | Currently unused by launcher code; verify or remove |
| Firebase AI, Firebase AppCheck, Firebase BOM, Google Services plugin, Secrets plugin, Credential Manager / Google ID | Unused scaffolding | Removed; not shipped | Removed from the build graph and version catalog in Phase 1.2.1. No Firebase/GMS/Play Core component or `INTERNET` permission remains in the merged manifest. |
| Google Play services Location, CameraX, Accompanist Permissions | Optional/unused integrations | Apache-2.0 or Google terms depending on artifact | Verify or remove |
| JUnit 4 | Test-only | Eclipse Public License 1.0 | Test-only and not shipped; do not redistribute as part of the app |
| Robolectric, AndroidX Test, Espresso | Test-only | Apache-2.0 | Test-only; not shipped in the APK |

No project-wide audit of transitive dependencies has been completed. The table records the
license family, not a legal conclusion for every resolved artifact. Before distributing a binary:

- Generate or review a per-dependency license report from the resolved Gradle graph.
- Keep required notices and license texts with the distribution.
- Confirm that every dependency is compatible with GPL-3.0-or-later distribution.
- Remove unused dependencies rather than auditing code the launcher does not use.

### GPL compatibility notes

- Apache-2.0 dependencies are generally compatible with GPLv3 distribution when their notices and
  license text are preserved. They are not relicensed under the GPL.
- JUnit 4 is EPL-1.0 and is test-only; it is not part of the launcher APK.
- Firebase and Google SDK artifacts were unused scaffolding and have been removed from the build
  graph in Phase 1.2.1. No Google-terms artifact is shipped.
- The GPL covers original project code and documentation only. It does not override any third-party
  license, font license, asset provenance, or trademark right.

## Fonts

### Plus Jakarta Sans

- File: `app/src/main/res/font/plus_jakarta_sans.ttf`
- SHA-256: `3632e5fe3fbece3917f7347ba91fbb6fa06d120af55b387f1e2067c170f92faf`
- Copyright: 2020 The Plus Jakarta Sans Project Authors
  (<https://github.com/tokotype/PlusJakartaSans>)
- License: SIL Open Font License 1.1
- License text: [`licenses/PlusJakartaSans-OFL.txt`](licenses/PlusJakartaSans-OFL.txt)
- Status: third-party asset, retained under the SIL OFL. It is **not** relicensed under the GPL.

Distributions that include the font must also include the OFL license text and preserve the font's
copyright and license notices. Modified versions of the font must follow the OFL conditions. The
full OFL 1.1 text and the upstream copyright header are committed in
`licenses/PlusJakartaSans-OFL.txt`; ship that file with any APK that bundles the font.

## Bundled images and artwork

| File | SHA-256 | Status |
| --- | --- | --- |
| `app/src/main/res/drawable/albkestrel_launcher_icon.jpg` | `c5f0e13cd0720814c5c0bd723906745de7a0dc8294d2f7a7b859a5f9656cb185` | Provenance and license not recorded. **Unresolved — release blocker.** |
| `app/src/main/res/drawable/albkestrel_wallpaper.jpg` | `f6236195e9646c635be01cb3b147eb7a1ae93ba777d9618620a6b4ea164b07c1` | Provenance and license not recorded. **Unresolved — release blocker.** |
| `app/src/main/res/mipmap-*/ic_launcher*.png` | e.g. mdpi `4e3c38ac…a1734` | Derived from the launcher icon above; **unresolved** until the source artwork is cleared. |
| `app/src/main/res/drawable/ic_launcher_background.xml` | `83385540…a9b9` | Android Studio template artwork; confirm the template license before release. |
| `app/src/main/res/drawable/ic_launcher_foreground.xml` | `9bf5c10e…778a` | Android Studio template artwork; confirm the template license before release. |

The repository does not record who created these images, where they came from, or under what terms
they may be redistributed. They must not be described as project-owned or automatically relicensed
under the GPL. Resolve their provenance, or replace them with clearly licensed artwork, before
publishing a release.

System application icons rendered at runtime come from installed packages through Android's
`PackageManager`. They are displayed by the platform and are not redistributed by this repository.

## Trademarks

GPL-3.0-or-later does not grant trademark rights. The names "Albkestrel", "Albkestrel Launcher", and
"AlbkestrelOS" identify this project, but this repository makes no trademark registration or
ownership claim over them. Android is a trademark of Google LLC. Other product and company names
belong to their respective owners.

## Asset audit checklist

- [ ] Establish the source and license for both bundled JPEGs and any derived launcher mipmaps.
- [x] Include the SIL OFL text and Plus Jakarta Sans notices in distributions that contain the font
      (`licenses/PlusJakartaSans-OFL.txt`).
- [ ] Generate a per-dependency license report before release.
- [x] Remove unused Firebase/AppCheck and other unused dependencies.
- [ ] Confirm test-only dependencies are excluded from shipped artifacts.
- [ ] Record every new third-party dependency, font, icon, image, or asset here before merging it.
