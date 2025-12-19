---
level: 3
file_id: plan_09
parent: plan_08
status: pending
created: 2025-12-19 14:40
estimated_time: 240分钟
---

# 任务：Reranker 模型服务实现

## 任务概述
在 Python 引擎中加载重排序模型并提供预测接口。

---

## 执行步骤
1.  **模型选型**：在 `requirements.txt` 中添加 `sentence-transformers`。
2.  **异步加载**：在 FastAPI 启动钩子中预加载模型至内存。
3.  **预测逻辑**：实现 `/api/v1/rerank` 接口，接收 `{query, docs}`。

---

## 验收标准
- [ ] 接口能返回 0-1 之间的相关性得分。
- [ ] 显存/内存占用在预设范围内。
