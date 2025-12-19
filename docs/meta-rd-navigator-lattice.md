# 🚀 Lattice (晶格) · 智能需求理解与研发导航分析

---

### 🧭 一、需求理解与意图识别
*   **显性需求（表面目标）**：构建一个基于 Spring AI、pgvector 和 React 的多模态生活管理系统，实现数据的节点化存储与智能检索。
*   **隐性需求（潜在动机）**：
    *   **非线性知识整合**：将碎片化的财务、职业、兴趣数据通过“语义链接”转化为可推导的资产。
    *   **低摩擦决策辅助**：通过 Agent 自动处理繁琐的入库（Ingestion）与查询，降低用户管理生活的认知负载。
    *   **数据主权与演进**：利用本地化向量库（pgvector）确保隐私，并允许系统随用户生活数据的增加而自发产生洞察。
*   **背后意图**：实现**“第二大脑”的工程化落地**。不仅是存储，而是通过 RAG 架构将“记录”升级为“可交互的认知智能体”。

---

### 🧩 二、关键词 · 概念 · 基础与隐性知识
*   **核心关键词**：
    *   **JSONB + pgvector**：在 PostgreSQL 中兼顾半结构化数据的灵活性与高维向量的相似度计算（Hybrid Search）。
    *   **RAG (Retrieval-Augmented Generation)**：通过检索用户历史数据（财务、日记）作为 LLM 上下文，解决模型幻觉并提供个性化建议。
    *   **Agentic Workflow**：不仅是问答，而是通过 Tool Calling 执行具体的“入库”、“分析”、“通知”任务。
*   **学科归属与理论背景**：
    *   **数据治理（Data Governance）**：应用企业级的元数据管理与质量控制策略于个人数据。
    *   **认知负荷理论**：通过自动化与智能化，将人类从“记住数据”中解放，转向“利用数据决策”。
*   **隐性知识与要点**：
    *   **结构化混沌（Structured Chaos）**：生活数据本质是乱序的。关键不在于强行定义 Schema，而在于**“晚绑定”**——在查询时通过语义发现关联，而非在存储时通过外键硬连接。
    *   **向量漂移**：随时间推移，用户生活偏好会变化，向量索引需要具备时序敏感性。

---

### 🧱 三、技术路径 · 开源项目 · 参考资料
*   **技术路径**：
    1.  **统一模型层**：利用 Java 21 的 `Loom`（虚拟线程）处理高并发的外部 API（DeepSeek, Firefly III）调用。
    2.  **存储引擎**：深化 `pgvector` 应用，引入 HNSW 索引优化大规模节点检索。
    3.  **代理编排**：从简单的 Prompt 转向 **Multi-Agent 系统**（例如：财务 Agent 协作职业 Agent 生成“职业转换财务影响报告”）。
*   **相关开源项目与工具**：
    *   [Spring AI](https://github.com/spring-projects/spring-ai)：作为后端骨架，统一处理不同 LLM 厂商的接入。
    *   [Firefly III](https://github.com/firefly-iii/firefly-iii)：已集成的财务底座。
    *   [Ant Design Pro](https://pro.ant.design/)：现有的前端框架，可扩展出复杂的图谱可视化组件（如 G6 或 ECharts Tree）。
*   **参考资料**：
    *   *“Building a Second Brain”* by Tiago Forte（思维方法论）。
    *   *“Vector Database Strategy in PostgreSQL”* (PostgreSQL 官方博客)。

---

### 🧠 四、专家范式 · 高层洞见与建议
*   **专家范式**：**解耦收集 (Capture) 与 整理 (Organize)**。
    *   不要让用户在输入时担心格式。系统应通过 `lattice-ingestion` 模块实现“盲收”，通过 LLM 在后台异步进行“语义打标”与“关系推理”。
*   **隐性经验**：
    *   **不要过度设计 Schema**：JSONB 配合 GIN 索引是处理生活多样性的良药。
    *   **反馈闭环**：在 `LatticeChat` 中增加“反馈点赞/踩”，这是微调本地 Embedding 模型的最宝贵数据。
*   **高层洞见**：
    *   Lattice 的终极价值不是“自动化”，而是**“自省能力”**。一个能指出你“消费习惯与长期职业目标冲突”的系统，远比一个简单的记账软件强大。
*   **下一步行动策略**：
    1.  **强化 `lattice-agent` 的工具链**：实现直接通过对话修改 PostgreSQL 中的 JSONB 数据（安全受控）。
    2.  **引入时间轴视图**：在前端 React 中将节点按语义流随时间线展开。
    3.  **打通 Python 引擎**：利用 Python 社区丰富的 `scikit-learn` 或 `PyTorch` 资源，进行后端 Java 难以胜任的长文本聚类分析。

---
*分析时间: 2025年12月19日*
*分析引擎: Meta R&D Navigator*
