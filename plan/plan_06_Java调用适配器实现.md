---
level: 3
file_id: plan_06
parent: plan_05
status: pending
created: 2025-12-19 14:25
estimated_time: 180分钟
---

# 任务：Java 调用适配器实现

## 任务概述
在 `lattice-ingestion` 模块中实现 `PythonEngineClient`，使用 Spring 提供的 `RestClient` 与 Python 引擎进行高效通讯。

---

## 执行步骤

### 步骤 1：定义 DTO 协议
- **操作**：在 Java 端创建 `PythonParseRequest` 和 `PythonParseResponse`。
- **字段**：`rawContent` (String/Base64), `fileType` (String)。

### 步骤 2：RestClient 配置
- **操作**：配置 `PythonEngineProperties`，读取 `LATTICE_PYTHON_ENGINE_URL` 环境变量。

### 步骤 3：容错机制
- **操作**：使用 `@Retry` (Resilience4j) 装饰调用方法，处理 Python 引擎冷启动或超时。

---

## 验收标准
- [ ] 编写集成测试，Mock Python 引擎返回，验证 Java 端解析响应的逻辑正确。
- [ ] 异常情况下（如 Python 挂掉）有友好的日志记录。
