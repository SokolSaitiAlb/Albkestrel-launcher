# TODO

GitHub-compatible task list for Albkestrel Launcher. A task is checked only when repository
evidence supports it: source code, tests, a build record, or a committed document. Uncommitted work
is called out explicitly.

Status snapshot: 2026-11-04, branch `migration-orca`. Phase 1.2 is committed as `157e446` and the
GPL-3.0-or-later license and notices as `4b0f066`. The project owner reports 12/12 physical
scenarios passed on a Google Pixel 8a.

## Critical bugs and blockers

- [ ] Remove unused Firebase/AppCheck dependencies, BOMs, plugins, and `.env` plumbing before any
      release. The launcher code does not use them, but dependency manifest merging can add
      permissions and attack surface.
- [ ] Verify the merged manifest after dependency cleanup. The source manifest declares only
      `VIBRATE`; confirm the packaged APK does not request network or other unused permissions.
- [x] Restore explicit click semantics in `LivingAppIcon`. Touch works through `pointerInput`;
      both click and long-click are now declared through semantics, and a focused Compose semantics
      test guards the contract.
- [ ] Define an intentional backup policy. `android:allowBackup="true"` is set while
      `backup_rules.xml` and `data_extraction_rules.xml` are still sample templates.
- [ ] Establish the provenance and license of `albkestrel_launcher_icon.jpg`,
      `albkestrel_wallpaper.jpg`, and the launcher mipmaps derived from them. Record the result in
      `THIRD_PARTY_NOTICES.md` or replace the artwork.
- [x] Commit the reviewed Phase 1.2 implementation on `migration-orca` as `157e446`.

## Phase 1.2 acceptance

### Implemented and covered by tests

- [x] Long-press wiring on home screen app icons.
- [x] Long-press wiring on dock icons.
- [x] Long-press wiring on app-drawer icons.
- [x] Long-press wiring on folder app icons.
- [x] Anchored context-menu popup clamped to screen bounds.
- [x] Scrollable menu with a bounded height for small screens.
- [x] App-info action opens Android application details.
- [x] App-info action dismisses the launcher menu.
- [x] Remove-from-launcher action deletes only workspace references.
- [x] Uninstall action uses the official Android delete flow.
- [x] System packages never expose or execute uninstall.
- [x] Repository-level defense blocks system-package uninstall.
- [x] Pause action hidden while no supported public API exists.
- [x] Widgets action hidden while no real widget host exists.
- [x] Shortcut discovery guarded to API 25+ and host permission.
- [x] Shortcut execution guarded to API 25+, installed app, and non-blank id.
- [x] Outside-tap dismissal.
- [x] Back-press dismissal.
- [x] Action-selection dismissal where appropriate.
- [x] Stale menu dismissal when the package disappears during refresh.
- [x] Accessibility `onClick` and `onLongClick` semantics for launch and menu actions.
- [x] Focused Compose semantics test for click and long-click actions.
- [x] Focused unit tests for policy, repository actions, shortcuts, and ViewModel behavior.

### Owner-reported physical validation

The project owner reported 12/12 passed on a Google Pixel 8a. This was not independently re-run by
the documentation/audit pass.

- [x] App launch by tap.
- [x] Long-press on Home, App Drawer, Dock, and inside folders.
- [x] App Info.
- [x] Remove from Home.
- [x] Official Android uninstall flow.
- [x] Shortcut discovery and execution.
- [x] Context-menu screen bounds and scrolling.
- [x] Haptics and animations.
- [x] TalkBack click and long-press accessibility.

### Pending acceptance

- [x] Commit Phase 1.2 on `migration-orca` without touching `main` (`157e446`).
- [ ] Validate popup placement in landscape and on wide/foldable screens.
- [ ] Validate uninstall cancellation and post-uninstall refresh edge cases.
- [ ] Validate uninstall is absent for system apps on a real device.
- [ ] Validate Android 7.0/7.1 fallback behavior where shortcuts are unavailable.
- [ ] Validate TalkBack row traversal and focus order in the menu.
- [ ] Validate haptics with haptics disabled.
- [ ] Add broader Compose UI/instrumentation coverage for the menu surface.

## Phase 1.3 — Universal Widget System

Planned. Phase 1.2 is committed as `157e446`; do not start until its remaining acceptance items are
resolved.

- [ ] Write and accept an ADR for the widget host lifecycle before implementation.
- [ ] Add a real `AppWidgetHost` with a stable host id.
- [ ] Request `BIND_APPWIDGET` through the platform-supported flow, without root or system
      privileges.
- [ ] Implement the widget picker for providers that support the launcher.
- [ ] Bind, inflate, and render widget views through supported Android APIs.
- [ ] Persist widget identity, page, grid position, and span in Room.
- [ ] Handle widget resize, orientation changes, and launcher restart.
- [ ] Handle provider removal, disabled providers, and bind failures gracefully.
- [ ] Refresh widgets when package or provider changes occur.
- [ ] Add widget-specific tests and physical-device validation.
- [ ] Keep the context-menu **Widgets** action hidden until the host is genuinely usable.

## Phase 1.4 — Icon Appearance Engine

Planned.

- [ ] Define icon-pack discovery and format support.
- [ ] Add per-app icon overrides.
- [ ] Add adaptive/legacy icon shape handling.
- [ ] Add themed icon support where the Android version permits it.
- [ ] Preserve a deterministic fallback to the real `PackageManager` icon.
- [ ] Cache rendered icons with a bounded strategy.
- [ ] Add icon-engine unit tests and visual validation.
- [ ] Do not claim icon-pack support until at least one real pack is validated.

## Performance

- [ ] Move shortcut discovery out of composition; it currently queries `LauncherApps`
      synchronously when the context menu is composed.
- [ ] Measure cold-start time to first home frame.
- [ ] Bound the icon cache and measure memory on low-memory devices.
- [ ] Confirm package queries stay off the main thread.
- [ ] Profile app-drawer scrolling with a large installed-app set.
- [ ] Avoid unnecessary recomposition of the full workspace when only overlay state changes.
- [ ] Review the one-second clock tick for battery impact.

## Accessibility

- [x] Restore explicit click semantics in `LivingAppIcon`.
- [x] Add click and long-click semantics for the shared app icon, with a focused semantics test.
- [x] Owner-reported TalkBack click and long-press check on a Google Pixel 8a.
- [ ] Add content descriptions or semantic roles for other custom clickable surfaces that use raw
      `pointerInput`.
- [ ] Verify minimum touch targets for all icon, menu, and close controls.
- [ ] Verify contrast for secondary text on translucent surfaces.
- [ ] Verify reduced-motion mode disables or shortens all non-essential motion.
- [ ] Test TalkBack traversal of home, drawer, folder, settings, and every context-menu row.
- [ ] Test switch access and keyboard/D-pad navigation.
- [ ] Support system font scaling without clipping.
- [ ] Respect RTL layout.

## Testing

- [ ] Create `app/src/androidTest` with instrumentation tests.
- [x] Add a Robolectric/Compose semantics test for `LivingAppIcon` click and long-click actions.
- [ ] Add Compose UI tests for context-menu actions and dismissal.
- [ ] Add instrumentation tests for Room migrations on device.
- [ ] Add a device matrix for API 24, 25, 31, and the current target.
- [ ] Add a test for package removal while the launcher is visible.
- [ ] Add a release-build smoke test.
- [x] Add the Android CI workflow for unit tests and debug assembly (not yet verified on GitHub).
- [x] Configure debug APK and test-report artifact retention in the workflow.
- [ ] Verify the workflow on GitHub before claiming CI is green.

## Documentation

- [x] Root README with accurate feature status.
- [x] TODO checklist.
- [x] Milestone roadmap.
- [x] Long-term master plan.
- [x] Architecture documentation.
- [x] Project structure documentation.
- [x] Contribution guidelines.
- [x] Security and privacy policy.
- [x] Changelog.
- [x] AI agent instructions and Gemini entry point.
- [x] Project status ledger.
- [x] Development setup, build/test, release, design-system, context-menu, widget, and icon docs.
- [x] Architecture decision records for local-first, launcher architecture, and proposed widget host.
- [x] GitHub issue templates, pull-request template, and Android CI workflow.
- [x] Add the GPL-3.0-or-later `LICENSE` and update licensing documentation.
- [x] Add the third-party dependency and asset audit record.
- [ ] Add screenshots after physical-device validation.
- [ ] Keep the status ledger current after each phase.

## Release preparation

- [x] Add the GPL-3.0-or-later `LICENSE` and project licensing declarations.
- [ ] Complete the third-party dependency license audit and include required notices.
- [ ] Resolve bundled image provenance before distributing the artwork.
- [ ] Remove unused dependencies and verify the merged manifest.
- [ ] Define versioning and bump `versionCode`/`versionName`.
- [ ] Create and protect release signing material outside the repository.
- [ ] Configure release signing through environment variables or a local keystore.
- [ ] Finalize backup and data-extraction rules.
- [ ] Run the full unit suite and a release build.
- [ ] Run physical-device acceptance for the release scope.
- [ ] Verify CI on the release commit.
- [ ] Write release notes from the changelog.
- [ ] Tag the release only after explicit authorization.
- [ ] Publish the artifact manually; the CI workflow does not publish releases.
