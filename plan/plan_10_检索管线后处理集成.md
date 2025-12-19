---
level: 3
file_id: plan_10
parent: plan_08
status: pending
created: 2025-12-19 14:45
estimated_time: 120分钟
---

# 任务：检索管线后处理集成

## 任务概述
在 Java 端的 `RetrievalService` 中引入重排序拦截器。

---

## 执行步骤
1.  **扩展检索链路**：在向量搜索返回后，判断是否开启 Rerank。
2.  **调用 Python 接口**：将粗排结果发送至 Python 引擎。
3.  **重新排序**：根据 Python 返回的 score 重新对 List 进行 Sort。

---

## 验收标准
- [ ] 后端日志显示检索链路中成功触发了 `ExternalRerankProcessor`。
- [ ] 前端展示的结果与 PG 原始向量排序不完全一致（证明 Rerank 生效）。
