# GEMINI.md

This file is the Gemini/Antigravity entry point for Albkestrel Launcher. It is intentionally short.

## Canonical instructions

Read and follow [AGENTS.md](AGENTS.md). It is the single canonical engineering instruction set for
all AI agents. If this file and `AGENTS.md` ever conflict, `AGENTS.md` wins.

## Project in one paragraph

Albkestrel Launcher is a local-first Android home screen built with Kotlin and Jetpack Compose. The
active development branch is `migration-orca`; public `main` currently contains only the initial
scaffold. Phase 1.2 (long-press app context menu) is committed as `157e446`, and the GPL license and
third-party notices are committed as `4b0f066`. The project owner reports 12/12 physical scenarios
passed on a Google Pixel 8a. Phase 1.3 (widgets) and Phase 1.4 (icon engine) are planned, not
implemented. GitHub Actions run #4 (`41804893723`) succeeded on 2026-11-04 for the committed
baseline at `63cb0c5`; the uncommitted Phase 1.2.1 working-tree changes have not been validated by
CI.

## Non-negotiables

- Inspect the repository before modifying it.
- Never assume a previous AI report is correct.
- Preserve existing uncommitted work.
- Do not modify `main`, switch branches, reset, clean, rebase, merge, or cherry-pick without explicit
  authorization.
- Never commit or push without explicit authorization.
- Exclude unrelated untracked content such as `skills/`.
- Use public Android APIs only; no root or system privileges.
- Keep the launcher local-first: no analytics, telemetry, mandatory cloud, or new network calls.
- Do not add unnecessary dependencies.
- Keep the project licensed under GPL-3.0-or-later. Do not add incompatible code or assets, and keep
  third-party notices current.
- Do not fake unavailable platform capabilities. Pause and widget actions stay hidden.
- Add tests for business logic and run the relevant Gradle checks.
- Never claim a feature works without evidence.
- Report exact changed files, commands, results, and remaining limitations.

## Default commands

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
git diff --check
```

Use JDK 17 and Android SDK Platform 36.

## Key documents

- [AGENTS.md](AGENTS.md) — canonical rules.
- [LICENSE](LICENSE) — GPL-3.0-or-later license text.
- [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) — dependency and asset license audit.
- [ARCHITECTURE.md](ARCHITECTURE.md) — actual architecture.
- [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) — current verified state.
- [TODO.md](TODO.md) — actionable tasks.
- [CONTRIBUTING.md](CONTRIBUTING.md) — branch, commit, and PR policy.
- [SECURITY.md](SECURITY.md) — privacy and security boundaries.

Engineering principle: **AI can propose. Policies decide. System authorizes. User controls.**
