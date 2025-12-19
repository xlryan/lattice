---
level: 3
file_id: plan_04
parent: plan_02
status: pending
created: 2025-12-19 14:15
estimated_time: 150分钟
---

# 任务：容器化编排与网络联通

## 任务概述
编写 Python 镜像的 `Dockerfile`，并将其加入根目录的 `docker-compose.yml` 中，确保 Java 容器可以通过 `http://python-engine:8000` 访问。

---

## 依赖关系
- **前置任务**：`plan_03` 已完成基础代码。

---

## 执行步骤

### 步骤 1：编写 Dockerfile
- **操作**：使用 `python:3.11-slim` 作为基座，安装构建工具（gcc等，用于解析库），COPY 源码。
- **注意**：使用多阶段构建或清理 `.pyc` 文件以减小镜像体积。

### 步骤 2：更新 docker-compose.yml
- **操作**：添加 `python-worker` 服务项，设置网络别名为 `python-engine`。
- **输入**：根目录 `docker-compose.yml`。

### 步骤 3：网络连通性测试
- **操作**：启动后，进入 `lattice-application` 容器尝试 `ping python-engine`。

---

## 可视化辅助
```
┌──────────────┐      ┌──────────────┐
│ Java App 容器 │ ---> │ Python 容器   │
│ (lattice-app)│      │ (python-work)│
└──────┬───────┘      └──────┬───────┘
       │ 网络别名:      │ 监听端口:
       ▼ python-engine ▼ 8000
```

---

## 验收标准
- [ ] `docker-compose ps` 显示 `python-worker` 状态为 Up。
- [ ] Java 容器内可成功 `curl http://python-engine:8000/api/v1/health`。
