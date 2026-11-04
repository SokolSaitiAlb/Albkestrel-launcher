# AGENTS.md

Canonical engineering instructions for AI coding agents working on Albkestrel Launcher.

Every agent, regardless of model or vendor, must follow this file. `GEMINI.md` is only an entry
point that points back here.

## 0. Prime directive

> AI can propose. Policies decide. System authorizes. User controls.

An agent may investigate, propose, implement, test, and report. It may not decide that a destructive
action is authorized, silently expand scope, or claim success without evidence.

## 1. Inspect before modifying

Before editing:

1. Confirm the repository root.
2. Confirm the branch and HEAD.
3. Inspect `git status --short`.
4. Inspect the files relevant to the task, not just prior reports.
5. Identify existing uncommitted work and preserve it.
6. Identify unrelated untracked files and exclude them.

If the expected branch or repository state differs, stop and report the discrepancy.

## 2. Never assume previous AI output is correct

- Treat prior summaries, reports, and generated code as unverified.
- Re-read the actual source.
- Re-run the commands needed to support a claim.
- Check whether a supposedly implemented feature has a real Android integration or only a UI
  placeholder.
- Do not repeat a prior claim simply because it appears in a summary.

## 3. Preserve the existing architecture

- Respect the existing UI → ViewModel → domain repository → data implementation layering.
- Preserve Android `Intent` construction in the data layer.
- Preserve pure policy in the domain layer.
- Do not introduce a DI framework, navigation framework, or network stack without a justified ADR.
- Do not rewrite working code to match personal preference.
- If a structural change is necessary, write or update an ADR under `docs/decisions/` and obtain
  explicit approval before implementing it.

## 4. Kotlin and Jetpack Compose conventions

- Use Kotlin 2.2.x conventions and four-space indentation.
- Prefer immutable data, `val`, and explicit types on public APIs.
- Keep composables focused on rendering and callbacks.
- Hoist state; do not bury business logic in composables.
- Use `collectAsStateWithLifecycle` at the boundary.
- Use stable lazy-list keys.
- Add `testTag` values only where they support a test.
- Do not perform blocking Android queries during composition. Record existing violations as
  performance tasks instead of expanding them.
- Avoid wildcard imports.
- Handle expected Android failures with safe return values; do not crash the launcher.
- Do not swallow exceptions without a documented reason.

## 5. Material 3 and Living Flow design

- Use Material 3 components and the existing `AlbkestrelTheme`.
- Reuse `LuminousSurface`, `LivingAppIcon`, `FolderIcon`, and existing color/type tokens.
- Living Flow principles: luminous translucent surfaces, fluid spring motion, flexible haptic
  feedback.
- Motion must explain state; it must not block interaction.
- The Reduced Motion setting must materially reduce or remove non-essential animation.
- Respect haptics settings.
- Do not add decorative motion that ignores the user's accessibility preferences.
- Maintain contrast on translucent surfaces.
- Verify layouts at different font scales and screen widths.
- Provide meaningful content descriptions and semantic actions.

## 6. Repository and Room migration safety

- Every schema change needs a version bump, an explicit migration, exported schema output, and a
  migration test.
- Never use destructive fallback for a released database.
- Never reset or recreate a user's workspace to fix a migration.
- Multi-table writes must be transactional.
- Bootstrap operations must be idempotent.
- Preserve intentionally empty layouts; do not reseed after the user clears content.
- Distinguish launcher-reference removal from package uninstall.
- When in doubt, preserve data and report the limitation.

## 7. Public Android APIs first

- Use documented, public Android APIs.
- Prefer `PackageManager`, `LauncherApps`, `Settings`, Room, and DataStore as already integrated.
- Guard newer APIs by `Build.VERSION.SDK_INT`.
- Provide graceful fallback for older devices.
- Do not rely on hidden APIs, reflection into non-public framework internals, or vendor-specific
  behavior without explicit approval.

## 8. No root or system privileges

- Do not add root, shell, `su`, system-app signing, device-admin, or privileged permission
  requirements.
- Do not silently uninstall packages.
- Do not bypass Android's confirmation UI.
- Do not add an accessibility service or notification listener merely to obtain data.
- Do not draw overlays over other apps.
- Any integration requiring platform privileges needs a separate approved architecture decision.

## 9. Local-first and privacy-first

- Keep workspace layouts, preferences, and app metadata on the device.
- Do not add network calls, cloud sync, analytics, telemetry, advertising, or account requirements
  without explicit product approval and a privacy review.
- Do not send package lists, usage data, or device identifiers off the device.
- Mock data must be visibly labelled as mock.
- Do not imply a live service when the implementation uses constants.
- Respect Android backup behavior and document any change to it.

## 10. No mandatory cloud, analytics, or telemetry

- The launcher must work offline.
- No mandatory sign-in.
- No Firebase, analytics, crash-reporting, or advertising SDK may be added to launcher code without
  explicit approval.
- Existing unused Firebase/AppCheck dependencies are a cleanup item, not a pattern to extend.
- Logging must not contain personal data, package inventories, or secrets.

## 11. Never introduce unnecessary dependencies

- Prefer the Android framework and existing libraries.
- Justify every new dependency by capability, maintenance cost, binary size, and permissions.
- Update `gradle/libs.versions.toml` rather than hard-coding versions.
- Do not add a library for a single trivial helper.
- Do not add a dependency that duplicates an existing one.
- Remove unused dependencies when the task explicitly permits build cleanup.

## 12. Unit tests for business logic

- Add or update unit tests for pure policy, search, mapping, ViewModel state, and repository logic.
- Use fakes or in-memory Room rather than real user data.
- Use coroutine test dispatchers for suspending code.
- Use Robolectric only where Android framework behavior is required.
- A bug fix should include a regression test when practical.
- Do not weaken or delete a test to make a change pass.

## 13. Android instrumentation tests where appropriate

- Add instrumentation tests for Compose surfaces and Android integrations when the task scope
  includes them.
- The repository currently has no `app/src/androidTest` suite; do not claim instrumentation coverage
  that does not exist.
- Widget hosting, shortcuts, uninstall flows, HOME behavior, and permission flows require
  device/emulator validation.

## 14. Physical-device validation for Android system integrations

- Unit tests cannot prove HOME-role behavior, package-change broadcasts, uninstall confirmation,
  shortcut-host permission, widget rendering, haptics, or OEM-specific popup behavior.
- State the exact device/API level used.
- If no device is available, mark the feature "implemented but not physically verified".
- Never claim device validation from a Robolectric test.

## 15. Run relevant Gradle tests and builds

Default verification commands:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
git diff --check
```

Additional commands when relevant:

```bash
./gradlew :app:lintDebug
./gradlew :app:assembleRelease
./gradlew :app:connectedDebugAndroidTest
```

Use JDK 17. Do not fabricate wrapper files, test results, or build outputs. If a command cannot run,
report the exact error and the environment limitation.

## 16. Report exact changed files and test results

Every completion report must include:

- Repository path.
- Branch and HEAD.
- Git status before and after.
- Exact files created, modified, and deleted.
- Exact commands run.
- Test/build results, including counts and failures.
- Artifact paths and checksums when an APK is produced.
- What was verified automatically.
- What still requires physical-device validation.
- Known limitations and follow-up work.
- Confirmation that unrelated files were excluded.

Do not summarize a large diff as "various changes".

## 17. Never claim success without evidence

- Do not say "done", "fixed", "working", or "complete" unless the evidence supports it.
- Distinguish:
  - Implemented.
  - Implemented but not physically verified.
  - Partially implemented.
  - Planned.
  - Not implemented.
  - Blocked by Android platform limitations.
- If a test was not run, say so.
- If a build was not run, say so.
- If only a subset of tests ran, say so.
- If a feature is uncommitted, say so.
- If `main` was not touched, say so.

## 18. Never commit, push, merge, or rebase without authorization

- Do not run `git commit`, `git push`, `git merge`, `git rebase`, or `git cherry-pick` without
  explicit authorization.
- Do not run `git reset`, `git clean`, `git checkout`, or `git stash` when doing so could discard
  work.
- Do not switch branches automatically.
- Do not modify `main` unless explicitly instructed.
- Do not stage unrelated files.
- Do not include untracked agent/tooling folders such as `skills/` in application commits.
- Leave changes uncommitted when the task asks for implementation without commit authorization.

## Repository-specific guardrails

### Current state

- Active worktree: `/home/alb/orca/workspaces/Albkestrel-launcher/main`.
- Branch: `migration-orca`.
- HEAD: `4b0f066` at the time of writing.
- Public `main`: `24932c3`.
- Phase 1.2 is committed as `157e446`; the GPL license and notices are committed as `4b0f066`.
- The project owner reports 12/12 physical scenarios passed on a Google Pixel 8a, including
  TalkBack click and long-press. That report is not a substitute for an independent re-run.
- Phase 1.3 (widgets) and Phase 1.4 (icon engine) are planned, not implemented.
- The GitHub Actions workflow is added but has not yet run on GitHub.
- The untracked `skills/` directory is external agent content and is not part of the app.

### Scope

- Do not start Phase 1.3 (widgets) or Phase 1.4 (icon engine) while Phase 1.2 acceptance items
  remain unresolved.
- Do not present pause or widget actions as available. No supported public pause API or real
  `AppWidgetHost` exists.
- Do not replace mock cards with fake "live" behavior.
- The project is licensed under GPL-3.0-or-later. Do not add or change licensing terms without
  explicit authorization.
- Documentation-only tasks must not modify application source.

### Licensing

- `LICENSE` contains the complete GNU GPL v3 text; the project SPDX identifier is
  `GPL-3.0-or-later`.
- Original project code and documentation are GPL-3.0-or-later. Third-party dependencies, fonts,
  icons, images, and other assets retain their own licenses.
- Do not copy third-party code or assets unless the license is compatible, attribution is preserved,
  and the item is recorded in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
- Do not relicense third-party work or claim ownership of it.
- Do not claim that the GPL grants trademark rights over the project name or logo.
- Google Play Billing is an optional future monetization feature. Paid distribution and subscriptions
  are permitted, but they must not reduce GPL rights.
- Do not remove or weaken third-party license notices to simplify a change.

### Android boundaries

- `minSdk 24`, `targetSdk 36`, `compileSdk 36`.
- Shortcuts require API 25+ and shortcut-host permission.
- Dynamic color requires API 31+.
- The HOME role is selected by the user.
- System apps cannot be uninstalled by the launcher.
- Uninstall must use `ACTION_DELETE`.
- App details must use `ACTION_APPLICATION_DETAILS_SETTINGS`.

### Known issues to preserve or fix explicitly

- Shortcut discovery runs during context-menu composition.
- The icon cache is unbounded.
- Unused Firebase/AppCheck dependencies remain in the build.
- Backup rules are sample templates.
- No instrumentation suite exists.
- The GitHub Actions workflow has not yet run on GitHub.
- Physical validation is owner-reported; landscape/wide layouts, API 25 fallback, and some
  accessibility traversal paths still need independent coverage.

Do not silently "fix" these outside the requested scope. Report them.

## Definition of done

A task is done only when:

1. The requested scope is implemented or documented.
2. Existing work is preserved.
3. Relevant tests/builds pass, or limitations are reported.
4. Documentation and status files are updated when behavior or status changes.
5. Third-party notices are updated when a dependency or asset changes.
6. The final report includes exact files and evidence.
7. No unauthorized Git action occurred.
8. Remaining limitations are explicit.

## Final reporting template

```text
Repository:
Branch:
HEAD:
Git status before:
Git status after:

Created:
Modified:
Deleted:

Validation:
- command: result

Artifacts:
- path: size, SHA-256

Verified automatically:
Pending physical-device validation:
Known limitations:
Git authorization status:
```
