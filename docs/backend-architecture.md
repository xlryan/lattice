# Lattice 项目后端架构设计文档

> 版本：v0.1（Java 21 / Spring Boot 3.3 / Spring AI M2）

## 1. Project Directory Structure（Maven 多模块骨架）

```
lattice
├── lattice-application                 # Monolith 启动器，聚合所有模块
│   └── src/main/java/com/lattice/app
│       ├── LatticeApplication.java     // SpringBootApplication 入口
│       ├── config                      // 全局配置（OpenAPI、CORS 等）
│       └── web                         // 跨模块 REST 适配器
├── lattice-core                        # Core：领域模型 + 基础设施
│   └── src/main/java/com/lattice/core
│       ├── config                      // 数据源、pgvector、自定义 Jackson
│       ├── domain                      // LatticeNode 聚合、值对象
│       ├── repository                  // Spring Data + 自定义 Vector 查询
│       └── service                     // 通用领域服务（如打分、规范化）
├── lattice-ingestion                   # Ingestion：入库与清洗管线
│   └── src/main/java/com/lattice/ingest
│       ├── controller                  // /api/ingest REST 入口
│       ├── service                     // IngestionService 实现
│       ├── mapper                      // AI 输出 -> JSONB 结构映射
│       └── workflow                    // Prompt 链路、回退策略
├── lattice-retrieval                   # Retrieval：混合检索策略
│   └── src/main/java/com/lattice/retrieval
│       ├── controller                  // /api/search API
│       ├── api                         // SearchRequest/Response DTO
│       ├── filter                      // FilterExpression 语法树
│       └── service                     // RetrievalService 与向量算分
└── lattice-agent                       # Agent：AI 应用与工具链
    └── src/main/java/com/lattice/agent
        ├── prompt                      // few-shot、JsonSchema 定义
        ├── client                      // Spring AI（DeepSeek/Ollama）
        └── task                        // RAG Workflow、工具插件
```

> 说明：多模块仍然由 `lattice-application` 统一打包运行，模块之间通过 Spring Bean 导出接口，不共享实现细节。

## 2. Advanced Database Schema（PostgreSQL 16 + pgvector）

### 2.1 `lattice_nodes` DDL

```sql
create table if not exists lattice_nodes (
    id            uuid primary key,
    domain        text not null check (domain in ('CAREER','DIY','MUSIC','LIFE','FINANCE')),
    title         text not null,
    content       text not null,
    properties    jsonb not null,
    embedding     vector(1536),                     -- pgvector 保存语义向量
    tags          text[] default '{}',
    created_at    timestamptz default now(),
    updated_at    timestamptz default now()
);

-- JSONB 通用 GIN，加速 key 任意组合查询
create index if not exists idx_nodes_props_gin on lattice_nodes using gin (properties);

-- 热点路径索引：成本字段需要排序/过滤
create index if not exists idx_nodes_props_cost on lattice_nodes ((properties->>'cost'));

-- 嵌套数组元素索引：materials[0].name，便于 DIY 材料关键字过滤
create index if not exists idx_nodes_props_material on lattice_nodes ((properties #>> '{materials,0,name}'));

-- domain + tags 普通 B-Tree，减少向量扫描候选集
create index if not exists idx_nodes_domain on lattice_nodes (domain, tags);

-- pgvector Ivfflat：lists/probes 后续视数据量调优
create index if not exists idx_nodes_embedding
    on lattice_nodes
    using ivfflat (embedding vector_cosine_ops)
    with (lists = 100);
```

> 查询策略：先使用 `domain/tags/JSONB` 过滤缩小候选集，再用 `<=>` 计算向量距离，最终采用加权评分（0.7 语义 + 0.3 结构）。

### 2.2 JSONB 样例

**CAREER**
```json
{
  "pattern": "STAR",
  "situation": "Legacy billing API lacked audit trail",
  "task": "Design event-sourcing module",
  "action": [
    "建模事件 Schema",
    "落地 Debezium CDC 管线"
  ],
  "result": "审计响应时间减少 70%",
  "skills": ["Spring Boot", "Kafka", "PostgreSQL logical decoding"]
}
```

**DIY**
```json
{
  "materials": [
    {"name": "2020 aluminum extrusion", "length_mm": 600},
    {"name": "T-slot connector", "count": 12}
  ],
  "cost": 420.5,
  "tools": ["miter saw", "torque wrench"],
  "notes": "Frame for 3D printer enclosure",
  "constraints": {"temperature_ceiling": 45}
}
```

**MUSIC**
```json
{
  "key": "Eb Major",
  "tempo_bpm": 96,
  "motif": "descending minor third + tritone resolution",
  "chords": ["Ebmaj7", "Gm9", "Abmaj7", "F7#11"],
  "mood": ["melancholic", "lush"],
  "hardware": {"synth": "Prophet Rev2", "daw": "Logic"}
}
```

**FINANCE**
```json
{
  "booked_at": "2024-11-25T09:30:00+08:00",
  "amount": 188.5,
  "currency": "CNY",
  "category": "餐饮",
  "source_account": "招商银行信用卡",
  "notes": "罗森早餐与咖啡，自动同步自 Firefly III",
  "tags": ["finance", "budget:food", "alert:overspend"]
}
```

## 3. Core Service Layer Interfaces

### 3.1 IngestionService（文本 -> Domain -> JSON -> PG）

```java
package com.lattice.ingest.service;

import java.util.UUID;

public interface IngestionService {
    /**
     * @param request 文本原件 + 可选标题
     * @return 新建节点的 UUID
     */
    UUID ingest(TextIngestionRequest request);
}

public record TextIngestionRequest(String rawText, String titleHint) {}
```

**流程**
1. `ClassificationClient`（Spring AI + DeepSeek）对 `rawText` 做 zero-shot 分类，输出 Domain + 置信度。
2. `MetadataExtractionWorkflow` 触发 JsonSchema Prompt，产出结构化元数据；`MetadataMapper` 负责类型校验与默认值填充。
3. `EmbeddingClient` 生成 1536 维向量；`NormalizationService` 归一化成本等字段。
4. `LatticeNodeRepository` 事务化写入 PG，失败回滚并记录至 `ingestion_audit`。

### 3.2 RetrievalService（FilterExpression + Vector Search）

```java
package com.lattice.retrieval.service;

public interface RetrievalService {
    SearchResponse search(SearchRequest request);
}

public record SearchRequest(
        String query,
        Domain domain,
        FilterExpression filter,
        int topK,
        Double minSimilarity
) {}

public sealed interface FilterExpression
        permits FilterEquals, FilterRange, FilterAnd, FilterOr {
    /**
     * @return SQL 片段以及对应参数，用于组合 JSONB 过滤
     */
    SqlClause toSqlClause();
}

public record SqlClause(String sql, Map<String, Object> params) {}
```

**检索步骤**
- `VectorProvider`：对 `query` 生成查询向量，支持缓存以降低延迟。
- `FilterCompiler`：将 `FilterExpression` 转为 `jsonb_path_query` 或 `properties ->> 'key'` 语句。
- `HybridSearchExecutor`：执行语句 `SELECT *, 0.7*(1 - embedding <=> :vector) + 0.3*metadata_score AS score ... ORDER BY score DESC LIMIT :topK`。
- `SearchResponse`：返回节点概述 + Top-N 高亮字段，为 Agent 模块提供上下文。

## 4. Tech Challenges & Solutions

### 4.1 数据隐私
- **字段级加密**：对 `content`、`properties -> 'situation'` 等敏感字段使用 pgcrypto + Spring AttributeConverter，避免明文落盘。
- **最小化外发**：Agent 层引入 Prompt 拦截器，过滤包含 PII 的 key；对所有 AI 请求打上 domain tag 以便追溯。
- **访问分级**：在 RetrievalService 前置 ABAC（domain + tag + requester role），生活（LIFE）类节点默认仅本人可检索。

### 4.2 混合检索性能
- **候选集裁剪**：强制所有查询至少包含 `domain`，并将高频 JSONB 字段抽取为 `generated column`（如 cost_num），配合 B-Tree 使用。
- **pgvector 调优**：按数据量调整 `lists`（≈ `rows/1000`），查询时设置 `ivfflat.probes`；定期 `ANALYZE` 以更新统计信息。
- **缓存策略**：对热门查询存储向量与 FilterClause，结合 Redis LFU；并在启动阶段执行 Warm-up Query，提升 buffer 命中率。

> 以上设计确保 Lattice 既具备结构化治理能力，又能通过 RAG 能力形成“第二大脑”的知识网络。
