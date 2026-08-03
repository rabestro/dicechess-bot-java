# AGENTS.md

Branch naming rules, quality gates, and agent instructions for `dicechess-bot-java`.

## Branch Naming Conventions

Branch name pattern: `<type>/<short-description>`, optionally `<type>/<id>-<short-description>`.

Allowed prefixes:
- Issue-driven: `task`, `feat`, `bug`
- Issueless: `refactor`, `chore`, `docs`, `ci`, `test`, `perf`

Example: `feat/onnx-evaluator-java`, `bug/fix-turn-context-clock`.

## Developer Workflows

- **Core Runner**: Use `mise run <task>` from the repository root.
- **Tasks**:
  - `mise run compile`: Compiles Java sources (`mvn test-compile`).
  - `mise run test`: Runs JUnit 5 test suite (`mvn test`).
  - `mise run check`: Full quality gate (`mvn clean test package`).
  - `mise run run`: Executes the bot application locally (`java -jar target/dicechess-bot-java-0.1.0-SNAPSHOT.jar`).

## Quality Gates — Definition of Done

- `mise run check` passes clean before any PR is proposed.
- All unit and integration tests pass.
- Code style follows standard Java 21 formatting conventions.

## Security & Boundaries

- Never commit secrets or signing keys (`DICECHESS_WEBHOOK_SECRET`, `BOT_TOKEN`).
- Do not commit large model binaries if untracked; keep models under `models/` gitignored if required.
