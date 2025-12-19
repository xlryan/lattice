# 📘 Lattice (晶格) · 项目上下文文档 (V1.0)

## 2.1 项目概要（Project Overview）
- **项目名称**: Lattice (晶格)
- **项目背景**: 基于“人生管理工程化”理念，将个人生活数据（职业、财务、爱好）视为结构化图谱节点。
- **目标与目的**: 应用数据治理原则管理动态属性，利用 Spring AI 与向量嵌入（Vector Embeddings）实现智能检索与代理工作流。
- **要解决的问题**: 解决个人生活数据零散、非结构化、难以进行深层语义分析与自动化处理的痛点。
- **整体愿景**: 实现“结构化的混沌（Structured Chaos）”，通过 PostgreSQL JSONB 与 pgvector 兼顾灵活性与智能语义理解。

## 2.2 范围定义（Scope Definition）
- **当前范围**: 
    - 后端：Java/Spring Boot 多模块架构（核心、采集、检索、代理、应用）。
    - 前端：React 19 + Ant Design Pro 的管理后台与交互界面。
    - 基础设施：Docker 容器化部署、PostgreSQL + pgvector。
- **非本次范围**: `lattice-python-engine`（目前处于非激活或开发中状态，未包含在 `docker-compose.yml` 中）。
- **约束条件**: 依赖特定外部 API（DeepSeek, Firefly III, ntfy），需 Java 21 环境。

## 2.3 关键实体与关系（Key Entities & Relationships）
- **核心实体**:
    - `Node`: 通用数据节点，存储于 JSONB，具备语义向量。
    - `Agent`: 执行特定任务的 AI 代理。
    - `Embedding`: 节点的向量表示。
- **实体职责**:
    - `lattice-core`: 定义领域模型与数据库迁移（Flyway）。
    - `lattice-agent`: 负责 Prompt 管理与工具调用逻辑。
- **实体关系描述**: 以图（Graph）结构组织，节点间通过语义或显式链接关联。

## 2.4 功能模块拆解（Functional Decomposition）
- **模块列表**: `lattice-ingestion`, `lattice-retrieval`, `lattice-agent`, `web-react`
- **模块详情**:
    - **lattice-agent**:
        - 输入: 用户指令、上下文数据。
        - 输出: 结构化响应、工具调用指令。
        - 核心逻辑: 结合 RAG 的 Agentic Workflow。
    - **lattice-ingestion**:
        - 核心逻辑: 解析不同来源的数据并转化为 JSONB 格式入库。
- **典型用户场景**: 通过 `LatticeChat` 进行语义搜索，通过 `CareerIngest` 导入职业数据，查看 `WealthDashboard` 进行财务分析。

## 2.5 技术方向与关键决策（Technical Direction & Decisions）
- **客户端**: React 19, TypeScript, Vite 6, Ant Design Pro。
- **服务端**: Java 21, Spring Boot 3.3.4, Spring AI 1.1.2。
- **模型或算法层**: DeepSeek LLM, RAG 架构, pgvector 向量检索。
- **数据流与架构**: 
    - 存储：PostgreSQL (Relational + JSONB + Vector)。
    - 部署：Docker & Docker Compose 一键启动。
- **已做技术决策**: 
    - 使用 Maven 多模块管理后端。
    - 使用 Flyway 管理数据库变更。
    - 采用 JWT 进行身份验证。

## 2.6 交互、风格与输出约定（Interaction & Style Conventions）
- **AI 输出风格**: 结构清晰、层级明确、工程化表达、优先考虑代码鲁棒性。
- **表达规范**: 统一使用 Markdown；复杂逻辑使用 Java/TypeScript 代码片段；变更需符合现有项目 Lint 规范。
- **格式要求**: 严谨、模块化。
- **用户特殊偏好**: 遵循 `GEMINI.md` 定义的架构规范与命名约定。

## 2.7 当前进展总结（Current Status）
- **已确认事实**: 
    - 项目骨架已建立，支持 Docker 部署。
    - 具备基础的职业（Career）与财务（Finance）功能模块。
    - 后端已集成 Spring AI 与 pgvector。
- **未解决问题**: Python 引擎的集成状态、具体业务节点的图谱演变算法。

## 2.8 后续计划与风险（Next Steps & Risks）
- **待讨论主题**: 
    - Agent 工具集的具体扩展计划。
    - 复杂 RAG 链路合作。
- **潜在风险与不确定性**: DeepSeek API 的稳定性、JSONB schema 演变的维护成本。
- **推荐的后续初始化 Prompt**: `“基于当前 Lattice 项目架构，请分析 lattice-agent 模块中 Tool Calling 的实现机制并提出优化建议。”`

---
*文档生成时间: 2025年12月19日*
*状态: 初始同步完成*
