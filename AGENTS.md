# Repository Guidelines

## Project Structure & Module Organization
`lattice-oss/` contains the Maven modules (`lattice-core`, `lattice-ingestion`, `lattice-retrieval`, `lattice-agent`, `lattice-application`), with the last module exposing Spring Boot entrypoints and integration tests in `src/test/java`. `web-react/` ships the Ant Design Pro client, `lattice-python-engine/` provides optional helpers via `environment.yml`, and infra or docs live under `docker-compose.yml`, `monitoring/`, `sql/`, `docs/`, and `prompt-kits/drag-ready/`. Treat `docs/PROJECT_CONTEXT.md` as the canonical guardrail and refresh it whenever scope changes.

## Build, Test, and Development Commands
- `./scripts/deploy.sh`: build images and run Docker Compose for the stack.
- `cd lattice-oss && mvn clean verify`: compile modules, run JUnit/Testcontainers, check Flyway.
- `cd lattice-oss && mvn -pl lattice-application spring-boot:run -Dspring.profiles.active=local`: launch the backend on `:8080`.
- `cd web-react && npm install && npm run dev -- --host 0.0.0.0`: start the Vite dev server; close with `npm run build`.
- `cd lattice-python-engine && conda env update -f environment.yml`: provision the Python helper toolchain.

## Coding Style & Naming Conventions
Java targets Java 21, 4-space indentation, and clear Spring layering (`domain`, `repository`, `service`, `application`); entities stay PascalCase, Flyway tables use snake_case, Lombok builders and DTOs under `application.*` are preferred. TypeScript components are PascalCase under `components/` or `pages/`, hooks reside in `contexts/`, API clients in `services/*.ts`, and camelCase state plus project formatting (or `npm run build`) keeps diffs clean.

## Testing Guidelines
Extend `AbstractIntegrationTest` for repository/service coverage to trigger PostgreSQL + pgvector Testcontainers, and keep unit tests under each module’s `src/test/java` using `ClassNameTest` with `methodUnderTest_context_expected` test names. Frontend work must pass `npm run build`; significant flows should gain browser specs in `web-react/tests/` and backend changes should document validation commands in PRs.

## Commit & Pull Request Guidelines
Use Conventional Commits with scopes (`feat:core`, `fix:web`, `chore:agent`) and keep each diff focused. PRs need a problem statement, linked issues, screenshots or curl snippets for UI/API changes, any new env vars (for example `DEEPSEEK_API_KEY`, `FIREFLY_API_TOKEN`), and the validation commands you executed (`mvn clean verify`, `npm run build`, `./scripts/deploy.sh`).

## Prompt-Kit Context Workflow
Always preload the drag-ready prompt kits so AI assistants follow them verbatim:
- `01-project-context-doc.md`: "生成完整、结构化、可迁移的项目上下文文档；不得虚构事实。"
- `02-meta-rd-navigator.md`: "自动识别关键词→结构→方案→行动，输出结构化结果。"
- `03-system-architect.md`: "先完成系统架构、模块边界、测试策略，再允许编码。"
- `04-principal-software-architect.md`: "第一性原理思考、权衡利弊、如需用 Mermaid 输出架构图。"
- `05-ai-planning-engine.md`: "只生成层级化计划文档，覆盖需求→计划→可视化序列。"
- `06-project-structure-standard.md`: "所有 AI 产物遵守标准目录与命名，不得污染根目录。"
- `07-senior-code-reviewer.md`: "分点阐述、解释原因、标注优先级，保持建设性审查。"
- `08-debug-expert.md`: "拒绝猜测，先收集事实，再二分验证假设。"
- `09-process-standardization.md`: "输出包含目的/范围/注意/工具/流程的 Markdown 编号文档。"
- `10-front-end-design-guardian.md`: "从最糟糕用户出发，三步以内交互、即时反馈、温柔文案。"
