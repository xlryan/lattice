
# 🕸️ Lattice (晶格)

> **The engineering approach to life management.**
> 用数据治理的思维，重构你的人生。

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3+-green.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-0.8.1-brightgreen.svg)](https://spring.io/projects/spring-ai)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)

---

## 📖 The Story / 初衷

**[English]**

**Why do we manage corporate assets with such rigorous engineering standards, while our own lives—our careers, hobbies, and memories—are scattered in chaotic text notes?**

Existing note-taking apps are too "light"; they lack structure.
Existing RAG tools are too "naive"; they lack governance.
Finance apps rarely talk to your knowledge base, so budgets stay siloed.

So I built **Lattice**. It's not just a resume generator or a diary. It applies **Data Governance** principles to personal life. It treats your career milestones, DIY projects, creative sparks, and even Firefly III transaction logs as **Structured Nodes**, connected by **Vectors**, governed by **Spring AI**, and executed through Tool Calling.

**[中文]**

**为什么我们不用管理企业核心资产的方式，来管理我们自己的人生？**

现在的笔记软件（Notion/Obsidian）太“轻”了，它们只存文本。
现在的 RAG（AI 问答）太“傻”了，它们不懂数据的结构。
传统记账 App 虽然擅长记账，但与知识库割裂，无法被 AI 统一协作。

所以我开发了 Lattice (晶格)。它不仅仅是一个简历生成器或日记本，它是**你人生的 Docker 容器**。我用我最擅长的**数据治理（Data Governance）**技术，结合最新的 **Spring AI**，打造了这个系统。它将你的职业经历、DIY 装修灵感、音乐创作、健康数据、Firefly III 账本，变成一个个标准化的“晶格 (Node)”，让 AI 真正成为你的第二大脑与数字管家。

---

## 🚀 Key Features / 核心特性

### 1. Structured Chaos (结构化人生)
- **Concept:** Don't just dump text. Lattice uses **PostgreSQL JSONB** to store dynamic attributes (e.g., DIY material costs, Music chords) alongside **pgvector** for semantic understanding.
- **Value:** Accurate retrieval. When you ask about "costs," it looks at the numbers; when you ask about "ideas," it looks at the vectors.
- **混合检索:** 结合 JSONB 的精确过滤与 Vector 的语义搜索，拒绝 AI 幻觉。

### 2. Spring AI Native (Java 生态)
- **Stack:** Built with **Java 21** and **Spring Boot 3.3**.
- **Why:** Leveraging the robustness of the Java ecosystem. No Python dependency hell.
- **Spring AI:** 利用 Java 强大的类型安全和企业级特性，构建更稳定的 AI 应用。

### 3. Privacy First (隐私至上)
- **Deployment:** Designed for **Self-Hosted** environments (NAS, Home Server).
- **Data:** Your data lives in your Docker container, not in a SaaS cloud.
- **私有化:** 你的数据完全由你掌控，适合部署在群晖、威联通或个人服务器上。

### 4. Agentic Workflow (智能体工作流)
- **Career Agent:** Parses your git commits into STAR-format resume bullets.
- **DIY Agent:** Calculates material lists for your aluminum profile projects.
- **Interview Bot:** Acts as a strict interviewer based on your *actual* project history.

### 5. Finance Action Layer (财务执行层)
- **Tool Calling:** Spring AI Function Call connects to **Firefly III**, so “记一笔刚才的 18.5 元咖啡” becomes an executable instruction.
- **RAG + Action:** Retrieve historical CSV/PDF statements (RAG) and write new expenses or alerts (Action) without context switching.
- **中文说明:** 通过 Firefly III API，Lattice 既能“读”账本，也能“写”账本，真正成为你的数字 CFO。

---

## 🏗️ Architecture / 架构

```mermaid
graph TD
    User[User (Ant Design Pro)] -->|Rest API| Web[Spring Boot Core]
    
    subgraph "Lattice Core (Docker)"
        Web -->|Orchestration| Agent[Spring AI Agent]
        Web -->|CRUD| DB[(PostgreSQL)]
        
        Agent -->|Vector Search| DB
        Agent -->|Metadata Filter| DB
        
        DB -- pgvector --> VectorIndex
        DB -- JSONB --> MetaIndex
    end
    
    subgraph "Model Layer"
        Agent -->|OpenAI Protocol| LLM[DeepSeek / Ollama / GPT-4]
    end
    Agent -->|Tool Calling| Firefly[(Firefly III API)]
````

### Tech Stack

* **Backend:** Java 21, Spring Boot 3.3+, Spring AI
* **Database:** PostgreSQL 16 (with `pgvector` extension)
* **Frontend:** Ant Design Pro (React / TypeScript)
* **Containerization:** Docker & Docker Compose
* **Finance Tooling:** Firefly III (self-hosted) + Spring AI Function Callback

-----

## ⚡ Quick Start / 快速开始

### Prerequisites

* Docker & Docker Compose installed.
* An API Key (DeepSeek recommended for cost/performance) OR local Ollama running.
* (Optional) A running **Firefly III** instance + API token for the Finance Action Layer.

### Installation

1.  **Clone the repo**

    ```bash
    git clone [https://github.com/xlryan/lattice.git](https://github.com/xlryan/lattice.git)
    cd lattice
    ```

2.  **Configure Environment**
    Copy the example config and add your API key / Firefly token.

    ```bash
    cp .env.example .env
    # Edit .env and set SPRING_AI_OPENAI_API_KEY=sk-xxxxxx
    # Optionally set FIREFLY_API_TOKEN=personal-access-token
    ```

3.  **Start up**

    ```bash
    docker-compose up -d
    ```

4.  **Access**

  * **Dashboard:** `http://localhost:8000` (Default user: admin/lattice)
  * **API Docs:** `http://localhost:8080/swagger-ui.html`

-----

## 🗺️ Roadmap & Model / 规划与模式

Lattice follows the **Open Core** model.
Lattice 遵循“开放核心”模式。

| Feature | Community Edition (OSS) | Pro Edition (Planned) |
| :--- | :---: | :---: |
| **License** | **AGPL v3** | Commercial |
| **Core RAG Engine** | ✅ | ✅ |
| **PostgreSQL + Vector** | ✅ | ✅ |
| **Self-Hosted (Docker)** | ✅ | ✅ |
| **Mock Interview Agent** | Basic | Advanced (Voice Mode) |
| **Firefly Finance Tooling** | ✅ (Manual token) | ✅ (Auto budgeting & alerts) |
| **Multi-Device Sync** | ❌ (Local only) | ✅ (Cloud Sync) |
| **Advanced Fine-tuning** | ❌ | ✅ |

-----

## 🤝 Contributing / 贡献

We welcome contributions\! Please read our [Contributing Guide](https://www.google.com/search?q=CONTRIBUTING.md) first.
Focus areas:

* New "Domain Parsers" (e.g., parsing Fitness App exports).
* Frontend Components for Ant Design Pro.

## 📄 License

This project is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.

* ✅ Free to use for personal/private use.
* ✅ Free to use for internal business use.
* ⚠️ If you modify the code and provide it as a service to others, you MUST open source your changes.

-----

<p align="center">
Made with ❤️ & 🍺 by a Java Developer in Chongqing.
</p>

````
