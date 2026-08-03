# AGENTS.md

Branch naming rules, developer workflows, and agent guidance for the `dicechess-bot-java` repository.

## Branch Naming Conventions

Branch name pattern: `<type>/<short-description>`, optionally `<type>/<id>-<short-description>` to link an issue.

Allowed prefixes:
- Issue-driven: `task` (work items), `feat` (features), `bug` (fixes) — typically carry an `<id>`.
- Issueless: `refactor`, `chore`, `docs`, `ci`, `test`, `perf` — no issue required.

Examples: `bug/42-fix-native-access-warning`, `feat/add-onnx-evaluator`, `chore/bump-deps`.

## Agent Rules (AI Assistance)
- Issue-driven work (`task`/`feat`/`bug`) starts from an issue; the branch carries its `<id>` and the PR links it with `Closes #<id>`. Issueless work (`refactor`/`chore`/`docs`/`ci`/`test`/`perf`) needs no issue. Name the branch per the pattern above.
- Always run `mise run format` on any modified code and ensure `mise run check` passes successfully locally before proposing a PR.
- Releases are human-triggered via GitHub Actions: `gh workflow run release.yaml -f bump=patch|minor|major`. Propose and assist, never execute releases directly.
- Human retains the ultimate authority to review, approve, and merge the PR.
- **GitHub CLI Authentication**: On macOS, credentials are saved in the Keychain. When executing `gh` commands, explicitly set the token to an empty string (e.g., `GH_TOKEN="" gh issue create ...`) to avoid authentication errors.

## Developer Workflows
- **Core Runner**: Use `mise run <task>` from the root of the repository for all development tasks.
- **Local Validation**: `mise run check` compiles code, runs unit & integration tests, and builds the shaded JAR.
- **Code Formatting**: `mise run format` applies standard Maven/Java formatting.
- **Local Service Control**:
  - `mise run build`: Compiles and packages the application via Maven.
  - `mise run test`: Runs all JUnit 5 unit and integration tests.
  - `mise run run`: Launches the bot server locally on port 8080.
  - `mise run docker:build`: Builds the local Docker image `dicechess-bot-java:latest`.
  - `mise run docker:up`: Starts the container stack via `docker-compose.yaml`.

## Approved GitHub Labels

Use ONLY these labels when generating `gh` commands:

* **Shared core** (identical across all Dice Chess repositories):
  * **bug** — Something isn't working.
  * **enhancement** — New feature or request.
  * **refactoring** — Code restructuring without behavioral changes.
  * **documentation** — Improvements or additions to documentation.
  * **testing** — Adding unit or integration tests.
  * **performance** — Strategy optimizations and speedups.
  * **ci-cd** — GitHub Actions, build scripts, or mise configuration.
  * **dependencies** — Dependency updates (applied by Dependabot).

* **Domains** (this repository only):
  * **bot-engine** — ONNX strategy, move generation, and Scala interop.
  * **infrastructure** — Docker, Koyeb, and container runtime.
