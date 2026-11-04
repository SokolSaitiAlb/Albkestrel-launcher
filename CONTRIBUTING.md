# Contributing

Thank you for considering a contribution. Albkestrel Launcher is an early-stage Android project.
Accuracy, scope discipline, and evidence matter more than volume.

## Before you start

1. Read [AGENTS.md](AGENTS.md) if you are using an AI coding agent.
2. Read [ARCHITECTURE.md](ARCHITECTURE.md) and [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md).
3. Inspect the current branch, HEAD, and working tree before editing.
4. Do not overwrite, reset, stash, or discard existing work.
5. Do not start a future phase while the current phase is uncommitted or unaccepted.

## Branch strategy

- `main` is the public branch and must not receive unreviewed direct commits.
- `migration-orca` is the current development branch for Phase 1.1/1.2 work.
- Create a topic branch from the agreed base for new work:

```text
feat/<short-topic>
fix/<short-topic>
docs/<short-topic>
chore/<short-topic>
```

- Do not switch branches automatically in an agent workflow.
- Do not force-push shared branches.
- Do not merge, rebase, or cherry-pick without explicit authorization.
- Keep unrelated agent/tooling directories, such as an untracked `skills/` folder, out of commits.

## Commit conventions

Use Conventional Commits:

```text
<type>(<scope>): <imperative summary>
```

Common types:

- `feat` — user-visible capability.
- `fix` — defect correction.
- `docs` — documentation only.
- `test` — tests only.
- `refactor` — behavior-preserving restructuring.
- `perf` — performance improvement.
- `build` — Gradle, wrapper, or dependency changes.
- `chore` — repository maintenance.

Examples:

```text
feat(context-menu): add anchored long-press actions
fix(workspace): preserve intentionally empty layouts on restart
docs: add architecture decision records
test(context-menu): cover system package uninstall guard
```

Rules:

- Keep the subject imperative and under roughly 72 characters.
- Explain **why** in the body when the change is not obvious.
- Do not mix unrelated changes.
- Do not claim a feature is complete in the commit message if device validation is pending.
- Never include secrets, keys, `local.properties`, `.env`, or build outputs.

## Pull request process

Before opening a pull request:

- [ ] Branch is based on the agreed target.
- [ ] The change is limited to one phase or concern.
- [ ] Application source is unchanged for documentation-only work.
- [ ] Unit tests pass.
- [ ] Debug assembly passes.
- [ ] `git diff --check` passes.
- [ ] Physical-device validation is recorded for Android integrations, or explicitly marked pending.
- [ ] Documentation and status files are updated.
- [ ] No unrelated files are staged.
- [ ] No secrets or signing material are present.
- [ ] Third-party code and assets are GPL-compatible, attributed, and recorded in
      [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

The pull request description should use the repository template and include:

- Summary.
- Exact changed files.
- Tests and commands run, with results.
- Device/API coverage.
- Known limitations and follow-up work.
- Confirmation that `main` was not modified without authorization.

## Code style

### Kotlin

- Follow the official Kotlin coding conventions.
- Use four-space indentation.
- Prefer immutable data and expression-bodied functions where clear.
- Keep domain logic out of composables.
- Name booleans as questions or states (`isInstalled`, `hasPermission`).
- Avoid wildcard imports.
- Handle expected platform failures without crashing; return a safe result and document it.
- Do not swallow exceptions silently unless the failure is genuinely non-actionable and the reason
  is documented.

### Jetpack Compose

- Hoist state above reusable composables.
- Keep composables focused on rendering and event callbacks.
- Use `collectAsStateWithLifecycle` at the activity/root boundary.
- Use stable keys in lazy lists.
- Add `testTag` values for surfaces that need UI tests.
- Do not perform blocking Android queries during composition. If existing code does, record it as a
  performance task rather than expanding the pattern.
- Provide content descriptions for meaningful controls.
- Respect reduced-motion and haptic settings.
- Verify layouts at different font scales and screen widths.

### Architecture

- UI → ViewModel → domain repository interface → data implementation.
- Do not call `PackageManager`, Room, or DataStore directly from composables.
- Keep Android `Intent` construction in the data layer.
- Pure policy belongs in the domain layer and must be unit-testable.
- New architecture patterns require an ADR under `docs/decisions/`.
- Prefer public Android APIs. Do not add root, shell, hidden-API, or system-only behavior.

### Persistence

- Every Room schema change needs a version bump, migration, exported schema, and migration test.
- Never use destructive fallback for a released database.
- Preserve user data through bootstrap and migration.
- Keep write operations transactional when they touch multiple tables.
- Remove operations must state whether they affect launcher references or installed packages.

## Testing requirements

- Business logic: unit tests.
- Repository behavior: unit tests with fakes or in-memory Room.
- ViewModel state transitions: coroutine tests.
- Compose surfaces: instrumentation or Compose UI tests once the suite exists.
- Android integrations: physical-device validation for permissions, intents, shortcuts, widgets,
  haptics, and HOME behavior.
- Build: `./gradlew :app:testDebugUnitTest` and `./gradlew :app:assembleDebug`.

Do not report a test as passing unless it was run and the output was observed. State skipped tests
and unverified layers explicitly.

## Documentation expectations

- Update [README.md](README.md) when user-visible behavior changes.
- Update [TODO.md](TODO.md) and [ROADMAP.md](ROADMAP.md) when status changes.
- Update [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) after each phase.
- Add an entry to [CHANGELOG.md](CHANGELOG.md) under `Unreleased`.
- Add or update an ADR for architectural decisions.
- Keep links valid and Mermaid diagrams syntactically correct.
- Never document planned features as implemented.

## AI-assisted contribution policy

AI assistance is allowed, but the contributor remains responsible for every line.

- AI output is a proposal, not evidence.
- Inspect the repository before accepting generated code.
- Do not assume a previous AI report is correct.
- Run the relevant tests and builds yourself.
- Check that the changed files match the stated scope.
- Disclose material AI assistance in the pull request when it affected design or implementation.
- Do not let an agent commit, push, merge, rebase, reset, or switch branches without explicit
  authorization.
- Do not let an agent add telemetry, cloud services, or unnecessary dependencies.
- Do not accept fabricated APIs, permissions, test results, or device claims.

All agents must follow [AGENTS.md](AGENTS.md).

## Scope discipline

- Phase 1.2 is committed as `157e446`; remaining acceptance work is tracked in [TODO.md](TODO.md).
- Phase 1.3 (widgets) and Phase 1.4 (icon engine) must not be implemented opportunistically.
- Unavailable platform capabilities must be omitted, not simulated.
- If a requirement cannot be met with a public Android API, document the limitation and stop.

## License

Albkestrel Launcher is licensed under **GPL-3.0-or-later**. The SPDX identifier for the project is
`GPL-3.0-or-later`. By intentionally submitting a contribution, you agree to license it under the
same terms and confirm that you have the right to do so. No copyright assignment or contributor
license agreement is currently required; copyright in your contribution remains with you.

Rules:

- Do not submit code, documentation, or assets that you cannot license under GPL-3.0-or-later.
- Do not copy third-party code without its license, attribution, and a compatibility review.
  Apache-2.0, MIT, and other permissive licenses are not automatically interchangeable with the GPL;
  verify the specific compatibility and notice obligations before adding the code.
- Do not relicense third-party code, fonts, icons, images, or other assets. They keep their own
  licenses even when distributed with this GPL project.
- Record every new third-party dependency or asset in
  [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
- Do not add fonts, images, or artwork unless their license permits redistribution and the required
  notices can be included.
- Paid distribution or subscriptions do not change the license terms for contributions.

See [LICENSE](LICENSE) for the full license text and
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the current third-party audit.
