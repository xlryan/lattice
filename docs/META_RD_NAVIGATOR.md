# 🚀 智能需求理解与研发导航引擎（Meta R&D Navigator）

> 依据 `prompt-kits/drag-ready/drag-ready/coding/02-meta-rd-navigator.md` 生成。

## 🧭 一、需求理解与意图识别
- **显性需求**：将 Lattice 仓库的背景、目标、模块与流程通过 Meta R&D Navigator 梳理成 AI 可复用的高层认知框架。
- **隐性需求**：把 README、AGENTS、Prompt Kits 等散落信息转化为统一的“概念 → 架构 → 行动”蓝图，确保智能体决策与任务拆解共享上下文。
- **背后意图**：搭建跨任务的 Cognitive Map，帮助所有助手按照“理解 → 规划 → 执行 → 迭代”范式行动。

## 🧩 二、关键词 · 概念 · 基础与隐性知识
- **核心关键词**：数据治理、pgvector、Spring AI Agent、混合检索、Firefly III Tool Calling、Ant Design Pro、Docker Compose、Prompt Kits。
- **学科/理论背景**：
  - 信息架构与数据治理：将人生资产标准化，结合 JSONB + Vector。
  - 软件工程分层：Maven 多模块、领域/服务/应用分层。
  - 智能体工作流：LLM、函数调用、工具协同、反馈闭环。
  - DevOps 可观测性：Docker、Testcontainers、ntfy、monitoring。
- **隐性知识要点**：
  - Spring AI 工具调用需要 DTO 约束，Firefly API 凭证管理关键。
  - RAG 精度依赖 JSONB 过滤 + pgvector 嵌入；Prompt 治理同等重要。
  - 前后端需共享语义，AGENTS.md/PROJECT_CONTEXT.md 负责对齐语言。
- **概念关联**：Prompt Kit 定义认知入口 → 后端 Agent/数据库/外部工具执行 → 前端与通知层反馈，形成闭环。

## 🧱 三、技术路径 · 开源项目 · 参考资料
- **技术路径**：
  1. 需求 → 概念图谱：使用 `02-meta-rd-navigator` 抽取概念、痛点与意图。
  2. 规划 → 执行：`05-ai-planning-engine` 拆解任务，`03-system-architect` 与 `06-project-structure-standard` 约束模块设计。
  3. 研发 → 验证：Spring Boot + Testcontainers 后端、Ant Design Pro 前端、Docker Compose 部署。
  4. 反馈 → 治理：`07-senior-code-reviewer`、`08-debug-expert` 审核/调试并回写 `docs/PROJECT_CONTEXT.md`。
- **相关项目/工具**：Spring Boot 3.3、Spring AI、PostgreSQL 16 + pgvector、Firefly III、ntfy、Ant Design Pro、Docker Compose、Testcontainers、DeepSeek/Ollama/GPT-4。
- **参考资料**：Spring AI docs、pgvector README、Firefly III API、Ant Design Pro 指南、Testcontainers Manual、Prompt Kits README。

## 🧠 四、专家范式 · 高层洞见与建议
- **思维模型**：`Context → Concept Lattice → System Architecture → Execution Loop`，先固化上下文，再用导航 Prompt 抽象需求，借系统架构 Prompt 设计分层，最后用调试/评审 Prompt 维持质量。
- **隐性经验**：
  - Prompt 治理与代码治理同等重要，定义“共享真相”可减少幻觉。
  - 改动需映射到 `PROJECT_CONTEXT.md` 与测试/部署脚本，否则多模块 + Docker 容易失配。
  - 财务/通知工具属于行动层，计划阶段就要考虑密钥、容错、可观测性。
- **高层洞见**：Lattice 既是产品也是方法论，必须在每次需求拆解时复用一致抽象层次，才能维持长期可维护性。
- **下一步建议**：
  1. 所有新任务先运行 `02-meta-rd-navigator` + `05-ai-planning-engine`，并把输出附在 issue/PR。
  2. 将关键概念图谱可视化（Mermaid/ASCII）放入 `docs/`，帮助新成员理解。
  3. 模块级变更完成后更新 `docs/PROJECT_CONTEXT.md` 与 `AGENTS.md`，确保 Prompt 指南与实现同步。
