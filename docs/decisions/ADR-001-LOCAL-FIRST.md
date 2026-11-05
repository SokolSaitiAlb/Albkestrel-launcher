# ADR-001: Local-First Data Architecture

- **Status:** Accepted (retrospective record of implemented behavior; no separate owner sign-off is
  recorded)
- **Date:** 2026-11-04
- **Deciders:** Existing implementation and repository audit; project-owner ratification is not
  separately recorded
- **Supersedes:** None

## Context

Albkestrel Launcher stores a user's home-screen layout, folders, dock, and preferences. A launcher
sees a meaningful amount of personal context: which apps are installed, how they are grouped, and
what the user looks at first. Sending that data to a server would create privacy obligations,
network failure modes, account requirements, and a backend the project does not have.

The project is an early-stage Android launcher with no backend, no authentication, and no release.
The initial implementation already uses Room and DataStore, both on-device. That choice was implicit
in the code; this ADR makes it explicit so future features do not drift into cloud defaults.

## Decision

The launcher is **local-first by default**.

1. Workspace layout, folders, dock contents, and preferences are stored on the device using Room and
   DataStore.
2. No launcher feature requires an account, network connection, or cloud service.
3. No analytics, telemetry, advertising, or crash-reporting SDK is part of launcher behavior.
4. Installed-app metadata is read from the Android platform and held in memory; it is not uploaded.
5. Any future feature that would leave the device requires:
   - an explicit product decision,
   - an ADR,
   - a privacy review,
   - visible user consent,
   - documentation of what is sent, why, and how to disable it.
6. Mock surfaces must be labelled as mock so they are not mistaken for live data collection.
7. The launcher must remain usable offline.

Android OS-level backup is a platform behavior, not a launcher-managed cloud feature. The project
must still make its backup policy explicit rather than relying on sample templates.

## Consequences

### Positive

- No backend cost or operational burden.
- No account or sign-in friction.
- Strong privacy posture that matches the product vision.
- Works offline and on isolated devices.
- Smaller dependency and permission surface.
- Simpler threat model.

### Negative

- No cross-device sync unless the user relies on Android backup or a future explicit export.
- No remote crash diagnostics; bugs must be reported by users.
- Restoring a layout after device loss depends on the OS backup policy.
- Some product ideas (shared themes, cloud icon packs, remote widget catalogs) are constrained.

### Neutral / follow-up

- Backup and data-extraction policy defined in the uncommitted Phase 1.2.1 work; see
  [../BACKUP_POLICY.md](../BACKUP_POLICY.md).
- Provide explicit local export/import if cross-device portability becomes a requirement.
- Unused Firebase/AppCheck dependencies removed in the uncommitted Phase 1.2.1 work, consistent with
  this decision.
- Record any future network addition in a new ADR.

## Alternatives considered

### Cloud-synced layout from day one

Rejected. It adds authentication, a backend, data-protection obligations, and failure modes before
the launcher has a stable release.

### Analytics for feature usage

Rejected. The launcher does not need usage telemetry to function, and collecting it would violate the
product's privacy premise.

### Anonymous crash reporting

Deferred, not adopted. It could be reconsidered with explicit consent and a documented data policy,
but it is not part of the current architecture.

### Rely on Android backup only

Accepted as the current platform default. The backup policy is now explicit in the uncommitted
Phase 1.2.1 work: the Room database, DataStore settings, and legacy SharedPreferences are included;
disposable caches are excluded; cloud backup requires encryption; device-to-device transfer carries
the same application data. See [../BACKUP_POLICY.md](../BACKUP_POLICY.md).

## References

- [ARCHITECTURE.md](../../ARCHITECTURE.md)
- [SECURITY.md](../../SECURITY.md)
- [MASTER_PLAN.md](../../MASTER_PLAN.md)
- [ADR-002](ADR-002-LAUNCHER-ARCHITECTURE.md)
