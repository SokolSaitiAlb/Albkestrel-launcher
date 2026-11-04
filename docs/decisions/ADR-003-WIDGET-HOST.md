# ADR-003: Third-Party Widget Host

- **Status:** Proposed — not accepted, not implemented
- **Date:** 2026-11-04
- **Deciders:** Project owner
- **Supersedes:** None
- **Related:** [WIDGET_ARCHITECTURE.md](../WIDGET_ARCHITECTURE.md)

## Context

The launcher has a reserved `WIDGET_SLOT` item type and an empty render branch, plus a context-menu
**Widgets** action that is hidden because `isWidgetSupportAvailable()` returns `false`.

Users reasonably expect a launcher to host Android app widgets, and the roadmap lists Phase 1.3 as
the Universal Widget System. However, widget hosting is a platform permission and lifecycle problem,
not a UI problem:

- A normal app does not hold `BIND_APPWIDGET`.
- `AppWidgetHost` requires a stable host id and careful lifecycle management.
- Binding may require user approval and provider configuration.
- Widgets are remote views owned by another process.
- Provider removal, process death, resize, and orientation changes all have to be handled without
  losing data or leaking views.

There is a real risk of shipping a convincing but fake picker, or enabling a menu action before it
can work. This ADR proposes the implementation boundary and asks for an explicit decision before
Phase 1.3 starts.

## Decision (proposed)

Implement third-party widget hosting using only public Android APIs:

1. Use **`AppWidgetHost` / `AppWidgetHostView`**. Do not reimplement provider remote views.
2. Persist a stable host id across process restarts.
3. Persist widget bindings in Room with provider identity and grid placement.
4. Use the platform-supported bind/pin flow. Do not assume `BIND_APPWIDGET` is granted.
5. Handle bind denial, configuration cancellation, provider removal, and `createView` failure
   without fake UI.
6. Participate in the same collision and placement rules as apps and folders.
7. Enable the context-menu **Widgets** action only after the host, picker, persistence, restore,
   resize, and removal paths pass device validation.
8. Keep the feature local and permission-minimal. No network access, no root, no system privileges.

### Scope boundary

In scope:

- Home-screen widgets on the launcher workspace.
- Provider discovery and a launcher-owned picker.
- Binding, rendering, persistence, restore, resize, and removal.
- Provider change and package removal handling.

Out of scope:

- Keyguard widgets.
- Hosting widgets without user approval.
- Privileged/system-only widget APIs.
- Remote widget catalogs or network-provided widgets.
- Replacing the system launcher's picker outside this app.

## Consequences

### Positive

- Meets a core launcher expectation with a supported platform mechanism.
- Reuses the existing workspace coordinate and persistence model.
- Keeps the **Widgets** action honest.
- Remote views remain owned and rendered by Android, not reimplemented.

### Negative

- Significant lifecycle and permission complexity.
- Bind behavior varies by OEM and Android version.
- Remote views can be fragile, slow, or crash-prone; the host must isolate failures.
- Adds a new Room schema and migration.
- Requires physical-device validation, which the project has not yet performed for any feature.

### Risks

| Risk | Mitigation |
| --- | --- |
| Fake picker that cannot bind | Gate the picker on a real bind attempt; show denial honestly |
| Orphaned widget ids | Reconcile stored ids on startup; release unused ids |
| Process-death loss | Persist host id and bindings; restore in a defined lifecycle |
| Provider uninstalled | Listen for package changes and clean rows |
| Memory leaks | Release host views with the host lifecycle; avoid activity context retention |
| Collision corruption | Reuse workspace collision checks; reject invalid spans |
| Scope creep into system privileges | Reject privileged APIs; require a new ADR |

## Alternatives considered

### Keep only in-app Compose cards

Rejected as a substitute. The existing weather/media/wellbeing cards are in-app mock surfaces. They
are not third-party widgets and must not be labelled as widget support.

### Use `requestPinAppWidget` only

Considered as a partial path. It can let a user pin a widget without the launcher holding
`BIND_APPWIDGET`, but it is not a general in-launcher picker and depends on provider support and the
launcher role. It could be an early increment, but not the whole system.

### Assume `BIND_APPWIDGET` / system privileges

Rejected. It would make the launcher non-installable on normal devices and violate the project's
public-API rule.

### Reimplement widget UI in Compose

Rejected. It would not be real widget hosting and would misrepresent third-party app behavior.

### Defer widgets indefinitely

Acceptable fallback. The **Widgets** action stays hidden and the roadmap keeps Phase 1.3 as planned.
This is better than shipping a non-functional picker.

## Decision requested

The project owner must explicitly accept or reject this ADR before Phase 1.3 implementation begins.
Acceptance means:

- the public-API-only boundary is approved,
- the Room schema addition is approved,
- the device-validation requirement is accepted,
- the **Widgets** action remains hidden until the exit criteria are met.

Until then, this document is a proposal only.

## References

- [WIDGET_ARCHITECTURE.md](../WIDGET_ARCHITECTURE.md)
- [ROADMAP.md](../../ROADMAP.md)
- [TODO.md](../../TODO.md)
- [ADR-001](ADR-001-LOCAL-FIRST.md)
- [ADR-002](ADR-002-LAUNCHER-ARCHITECTURE.md)
