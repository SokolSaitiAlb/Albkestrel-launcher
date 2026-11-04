# Roadmap

Milestone-based roadmap for Albkestrel Launcher. Status values are deliberately narrow:

- **Complete** — committed and supported by tests or a documented build.
- **Implemented, uncommitted** — code exists in the working tree but is not durable.
- **Partial** — some layers exist, but the user-facing scope is incomplete.
- **Planned** — approved direction, not implemented.
- **Proposed** — architecture decision not yet accepted.
- **Blocked** — depends on an unavailable Android capability or external decision.

Snapshot: 2026-11-04, `migration-orca`.

## Milestone summary

| Phase | Objective | Scope | Dependencies | Acceptance criteria | Status |
| --- | --- | --- | --- | --- | --- |
| 1.0 | Persistent launcher foundation | HOME activity, Compose shell, Room workspace, DataStore settings, dock, folders, drawer, mock cards | Android SDK 36, JDK 17 | HOME intent works; layout survives restart; settings persist; debug build succeeds | Complete in `migration-orca` history |
| 1.1 | Real installed-app integration | PackageManager discovery, real icons/labels, search, Room v3 metadata and migrations | Phase 1.0 | Launcher lists real launchable apps, excludes itself, launches them, survives package changes | Complete at `8747b09` |
| 1.2 | Long-press app context menu | Anchored menu, app info, remove, uninstall, shortcuts, haptics, dismissal, policy guards | Phase 1.1 | Menu opens from every app surface; only supported actions appear; system uninstall blocked; tests pass | Complete at `157e446`; owner-reported Pixel 8a validation 12/12 |
| 1.3 | Universal Widget System | Real `AppWidgetHost`, picker, binding, persistence, resize, lifecycle | Phase 1.2 accepted; ADR-003 accepted | A real third-party widget can be added, persisted, restored, resized, and removed without fake UI | Planned; ADR-003 proposed |
| 1.4 | Icon Appearance Engine | Icon packs, per-app overrides, adaptive shapes, themed icons, bounded caching | Phase 1.3 or explicit resequencing | At least one real icon pack works; fallback is deterministic; tests cover parsing and fallback | Planned |
| 2.0 | Workspace editing and gestures | Drag-and-drop placement, add/remove picker, folder creation, page management | Stable grid model; accessibility review | User can arrange apps without corrupting persistence or overlapping items | Planned |
| 2.1 | Search and shortcut expansion | Search ranking, shortcut surfacing, query history (local only) | Phase 1.2/1.4 | Search remains local, deterministic, and covered by tests | Planned |
| 2.2 | Work profile and multi-user | Separate profile-aware app lists and workspaces | Android profile APIs and product decision | Profiles are separated and never leak app data across users | Planned |
| 3.0 | AlbkestrelOS integration | Launcher integration points for a future OS image | Separate platform program | Documented, stable interfaces only; no root assumptions | Proposed |
| Release 1.0 | First distributable build | GPL compliance and notices, signing, cleanup, CI, device matrix, release notes | All above for chosen scope | Signed artifact, green CI, documented device validation, verified GPL-3.0-or-later compliance | Planned |

Licensing is settled as **GPL-3.0-or-later**. Optional future monetization, such as Google Play
Billing, is not a release requirement and must preserve the GPL rights of users and contributors.

## Phase 1.2 acceptance checklist

Code-level scope is committed as `157e446` and covered by unit tests. The project owner reports
12/12 physical scenarios passed on a Google Pixel 8a. The milestone is not closed until it is
independently re-validated where coverage is still thin.

- [x] Long press on home, dock, drawer, and folder icons.
- [x] Anchored menu with edge clamping and scroll bounds.
- [x] App info through Android application details.
- [x] Remove from launcher without uninstalling.
- [x] Official uninstall flow for eligible user apps.
- [x] System-app uninstall blocked in policy and repository.
- [x] Pause and Widgets omitted while unavailable.
- [x] API 25+ shortcut discovery and launch with host-permission guard.
- [x] Dismissal by outside tap, back, and actions.
- [x] Stale-menu cleanup after package refresh.
- [x] Explicit click and long-click accessibility semantics.
- [x] Unit tests for policy, repository, ViewModel, and icon semantics.
- [x] Owner-reported Pixel 8a acceptance: 12/12 scenarios.
- [x] Commit on `migration-orca` as `157e446`.
- [ ] Independent physical-device acceptance and broader device matrix.
- [ ] Instrumentation coverage for the menu surface.

## Phase 1.3 exit criteria

Phase 1.3 must not be marked complete unless all of the following are true:

1. A real `AppWidgetHost` is created with an explicit lifecycle and stable host id.
2. Widget binding uses the platform-supported permission flow; no root or system-only APIs.
3. At least one real third-party widget can be added, rendered, persisted, restored after process
   death, resized, and removed.
4. Provider removal and bind failures are handled without crashing or leaving orphaned database
   rows.
5. The **Widgets** context-menu action appears only when the host can service it.
6. Unit tests cover state/persistence logic and a physical device validates rendering.

## Phase 1.4 exit criteria

1. At least one real icon pack format is parsed and validated.
2. Per-app overrides persist and can be reset.
3. Missing icons fall back to the real `PackageManager` icon or a documented vector fallback.
4. Caching is bounded and does not leak `Drawable` or `Bitmap` references.
5. The Settings surface exposes only options that actually work.

## Sequencing rules

- Phase 1.2 is committed as `157e446`; do not start Phase 1.3 or 1.4 until its remaining acceptance
  items and ADR-003 are resolved.
- Do not merge Phase 1.2 into `main` without explicit authorization and a reviewed commit.
- Do not hide incomplete work behind fake UI. If an Android capability is unavailable, omit the
  action and document why.
- Architecture changes require an ADR under `docs/decisions/`.
- Every phase closes with tests, a build, a status update, and exact file/test reporting.
