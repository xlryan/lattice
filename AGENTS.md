# Repository Guidelines

## Project Structure & Module Organization
`lattice-oss/` hosts the Maven modules (`lattice-core`, `lattice-ingestion`, `lattice-retrieval`, `lattice-agent`, `lattice-application`), and the last module is the Spring Boot entry point with most integration tests under `src/test/java`. The Ant Design Pro client lives in `web-react/`, optional Python helpers sit in `lattice-python-engine/` (installed via `environment.yml`), and supporting assets stay in `docker-compose.yml`, `monitoring/`, `sql/`, `docs/`, and `scripts/`.

## Build, Test, and Development Commands
- `./scripts/deploy.sh` builds images and runs `docker compose up -d` for the entire stack.
- `cd lattice-oss && mvn clean verify` compiles every module, executes JUnit/Testcontainers suites, and validates Flyway migrations.
- `cd lattice-oss && mvn -pl lattice-application spring-boot:run -Dspring.profiles.active=local` starts the backend on `:8080` using your exported env vars.
- `cd web-react && npm install && npm run dev -- --host 0.0.0.0` launches the Vite dev server; run `npm run build` before submitting UI changes.
- `cd lattice-python-engine && conda env update -f environment.yml` prepares the optional agent helpers before using their notebooks or CLI tools.

## Coding Style & Naming Conventions
- Java: target Java 21, 4-space indentation, and the usual Spring layering (`domain`, `repository`, `service`, `application`). Keep entities in PascalCase (for example `CareerNode`), match Flyway scripts with snake_case tables/columns, prefer Lombok builders, and return DTOs declared in `application.*`; add bilingual comments only when business terms need translation.
- TypeScript: PascalCase component files go in `components/` and `pages/`, camelCase hooks/state stay in `contexts/`, and strongly typed fetchers live in `services/*.ts` alongside `requestConfig`. Stick to Vite/TSConfig defaults and format before committing with `npm run build` or your editor’s formatter.

## Testing Guidelines
Extend `AbstractIntegrationTest` for repository or service coverage so Testcontainers bootstraps PostgreSQL + pgvector (Docker must be running). Unit tests live under the module’s `src/test/java`, follow the `ClassNameTest` and `methodUnderTest_context_expected` naming, and should cover new Flyway migrations or agent prompt tweaks. Frontend PRs must pass `npm run build`, and add browser tests under `web-react/tests/` whenever you ship a notable user flow.

## Commit & Pull Request Guidelines
Favor Conventional Commits (`feat:`, `fix:`, `chore:`) plus the touched area (`core`, `web`, `agent`) so reviewers can skim quickly, and keep each commit focused. Pull requests should state the problem, outline the fix, link issues, include screenshots or curl snippets when UI/API changes occur, list new env vars (for example `DEEPSEEK_API_KEY`, `FIREFLY_API_TOKEN`), and cite the validation you ran (`mvn clean verify`, `npm run build`, `deploy.sh`).
