# ADR-002: Launcher Layering and State Ownership

- **Status:** Accepted (retrospective record of implemented behavior; no separate owner sign-off is
  recorded)
- **Date:** 2026-11-04
- **Deciders:** Existing implementation and repository audit; project-owner ratification is not
  separately recorded
- **Supersedes:** None

## Context

The launcher codebase is a single Android module with Compose UI, an `AndroidViewModel`, domain
repository interfaces, and Android-backed data implementations. That structure emerged during
Phases 1.0–1.2. Without a recorded decision, future work could:

- move `PackageManager`, Room, or DataStore calls into composables,
- split state across multiple competing holders,
- introduce a DI or navigation framework for a small app,
- duplicate action policy between UI and data layers,
- make destructive actions depend only on UI guards.

The code already demonstrates a workable direction. This ADR records it so changes are deliberate.

## Decision

Adopt and preserve the following layering:

```text
Compose UI
    ↓ events / state
LauncherViewModel (single UI state owner)
    ↓
Domain repository interfaces + pure policy
    ↓
Data implementations (PackageManager, Room, DataStore)
```

### Rules

1. **UI renders and emits events.** Composables do not call `PackageManager`, Room, or DataStore
   directly, and do not construct Android `Intent`s.
2. **One UI state owner.** `LauncherViewModel` owns `LauncherUiState`, overlay state, search state,
   page state, haptics, and action dispatch.
3. **Domain owns policy.** Pure decisions such as search filtering and context-menu action
   availability live in `domain/` and are unit-testable without Android.
4. **Data owns Android integration.** `PackageManager`, `LauncherApps`, `Settings` intents, Room,
   and DataStore stay in `data/`.
5. **Manual dependency composition.** `AlbkestrelApplication` constructs repositories lazily. A DI
   framework is not justified at the current size.
6. **Defense in depth for destructive actions.** Both the domain policy and the data repository
   enforce system-app uninstall blocking; the UI is not the only guard.
7. **Overlay state is a sealed type.** Drawer, search, settings, folder, prototype info, and context
   menu are mutually exclusive states of one sealed interface.
8. **Platform failure degrades safely.** Missing intents, packages, icons, or shortcut permission
   return empty/false values rather than crashing.
9. **New architectural patterns require an ADR.** This includes navigation, DI, network, background
   services, and multi-module splits.

### Rationale for a single ViewModel

At the current scope, one ViewModel keeps cross-cutting state coherent: a package refresh can dismiss
a stale context menu, a Home intent can collapse any overlay, and settings changes flow into the same
UI state. Splitting it now would add coordination cost without a demonstrated benefit.

The ViewModel is also a known scaling risk. When it grows beyond a manageable size, split by feature
with explicit state boundaries and record the change here rather than adding ad-hoc state holders.

## Consequences

### Positive

- Clear test seams: pure policy and repository contracts.
- Android APIs remain replaceable and mockable.
- Destructive actions are guarded in more than one layer.
- No framework overhead for a small team.
- Overlay behavior stays consistent.

### Negative

- `LauncherViewModel` is large and will need refactoring as features grow.
- Manual composition becomes repetitive if repositories multiply.
- A single state object can cause broad recomposition if not observed carefully.
- No navigation library means overlay transitions are hand-managed.

### Follow-up

- Move shortcut discovery off the composition path.
- Reduce unnecessary workspace recomposition.
- Split the ViewModel when Phase 2 introduces workspace editing and search ranking.
- Add instrumentation tests at the UI boundary.

## Alternatives considered

### Compose-first architecture with state in composables

Rejected. It would put platform queries and action policy in the UI layer and make destructive-action
guards untestable.

### Full MVVM with one ViewModel per screen

Rejected for now. Overlay coordination and package-refresh behavior span screens. Revisit when
screens become independently navigable.

### Hilt or another DI framework

Rejected for the current size. Manual composition in the `Application` is explicit and adequate.
Revisit if the module count or repository graph grows materially.

### Navigation Compose

Rejected for now. The launcher is a single activity with mutually exclusive overlays, not a
destinations graph.

### Multi-module architecture

Deferred. The app is small enough for one module. A split should follow measured build times and
ownership boundaries, not speculation.

## References

- [ARCHITECTURE.md](../../ARCHITECTURE.md)
- [PROJECT_STRUCTURE.md](../../PROJECT_STRUCTURE.md)
- [APP_CONTEXT_MENU.md](../APP_CONTEXT_MENU.md)
- [AGENTS.md](../../AGENTS.md)
