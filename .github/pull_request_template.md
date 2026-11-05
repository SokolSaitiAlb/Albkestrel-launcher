## Summary

Describe the change and why it is needed. Keep this focused on one phase or concern.

## Scope

- Phase / milestone:
- Related issue:
- Branch:
- Base commit:

## Changed files

List the exact files created, modified, or deleted. Do not summarize as "various changes".

## Behavior changes

- User-visible changes:
- Internal changes:
- Data or schema changes:

## Validation

Commands actually run:

```text
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
git diff --check
```

Results:

- Unit tests:
- Build:
- Lint:
- Whitespace:
- Artifact path and SHA-256, if produced:

## Device validation

- Device model:
- Android version / API level:
- Home-role selected: yes / no
- Scenarios tested:
- Not tested:

If no physical device was used, state that clearly. Robolectric and unit tests are not device
validation.

## Status honesty

Check every statement that applies:

- [ ] I did not describe planned work as implemented.
- [ ] I did not describe mock surfaces as live services.
- [ ] I did not claim device validation that did not happen.
- [ ] I labelled partially implemented behavior as partial.
- [ ] I updated README, TODO, ROADMAP, CHANGELOG, and docs/PROJECT_STATUS.md where status changed.
- [ ] I added or updated tests for business logic.
- [ ] I did not weaken or delete a test to make the change pass.

## Safety

- [ ] No secrets, signing material, `.env`, or `local.properties` are included.
- [ ] No unrelated files are included (including any untracked `skills/` directory).
- [ ] No destructive Git action was performed without authorization.
- [ ] `main` was not modified without explicit authorization.
- [ ] No new network, analytics, telemetry, or cloud dependency was added without approval.
- [ ] Public Android APIs only; no root or system-only behavior.
- [ ] Room schema changes include a migration, exported schema, and migration test.
- [ ] Third-party code and assets are GPL-compatible, attributed, and recorded in
      `THIRD_PARTY_NOTICES.md`.

## Known limitations and follow-up

List remaining limitations, unverified layers, and follow-up tasks.
