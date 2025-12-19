# 拖拽即用提示词工具包

该目录集中整理了仓库中最常用、质量最高的提示词，按照 **coding** 与 **user** 两类模块化打包，方便直接整体拖拽到其他项目或会话中复用。所有文件都保留了 YAML 元信息，便于追溯原始来源与后续同步更新。

## 使用方式
- 直接拖拽 `prompt-kits/drag-ready/` 目录或其中的子目录到新项目；
- 若需在本仓更新原始提示词，可先编辑 `i18n/zh/prompts/**` 中的对应文件，再运行同样的复制流程；
- 需要少量内容时，可在 README 中查找对应文件及来源路径，避免重复搜索。

## 内容总览

### Coding 提示词
| 文件 | 作用 | 原始路径 |
| --- | --- | --- |
| `prompt-kits/drag-ready/coding/01-project-context-doc.md` | 生成结构化项目上下文文档，确保跨会话共享一致的背景信息 | `i18n/zh/prompts/coding_prompts/(1,1)_#_📘_项目上下文文档生成_·_工程化_Prompt（专业优化版）.md` |
| `prompt-kits/drag-ready/coding/02-meta-rd-navigator.md` | 智能需求理解与研发导航引擎，从任意输入中抽取概念、路径与行动建议 | `i18n/zh/prompts/coding_prompts/智能需求理解与研发导航引擎.md` |
| `prompt-kits/drag-ready/coding/03-system-architect.md` | 引导 AI 先做系统架构与模块规划，再进入编码 | `i18n/zh/prompts/coding_prompts/系统架构.md` |
| `prompt-kits/drag-ready/coding/04-principal-software-architect.md` | 启动首席软件架构师角色，输出权衡后的架构决策 | `i18n/zh/prompts/coding_prompts/architecture-design.md` |
| `prompt-kits/drag-ready/coding/05-ai-planning-engine.md` | 仅输出层级化计划文档（含可视化建议），适合大任务拆解 | `i18n/zh/prompts/coding_prompts/plan提示词.md` |
| `prompt-kits/drag-ready/coding/06-project-structure-standard.md` | 统一项目目录规范，指导 AI 生成或重构标准化结构 | `i18n/zh/prompts/coding_prompts/标准项目目录结构.md` |
| `prompt-kits/drag-ready/coding/07-senior-code-reviewer.md` | 资深代码审查员角色，逐项输出问题、示例与优先级 | `i18n/zh/prompts/coding_prompts/code-review.md` |
| `prompt-kits/drag-ready/coding/08-debug-expert.md` | 系统化调试专家，围绕假设验证定位问题根因 | `i18n/zh/prompts/coding_prompts/debug-expert.md` |
| `prompt-kits/drag-ready/coding/09-process-standardization.md` | 将任意输入转化为规范流程文档，输出五段式结构 | `i18n/zh/prompts/coding_prompts/标准化流程.md` |
| `prompt-kits/drag-ready/coding/10-front-end-design-guardian.md` | 从“最糟糕的用户”视角设计前端体验与交互文案 | `i18n/zh/prompts/coding_prompts/前端设计.md` |

### User 提示词
| 文件 | 作用 | 原始路径 |
| --- | --- | --- |
| `prompt-kits/drag-ready/user/01-project-variables-and-tools.md` | 在 `AGENTS.md`/`CLAUDE.md` 统一维护变量、工具与调用路径 | `i18n/zh/prompts/user_prompts/项目变量与工具统一维护.md` |
| `prompt-kits/drag-ready/user/02-ascii-diagram.md` | 输出严格对齐的 ASCII 流程/架构图 | `i18n/zh/prompts/user_prompts/ASCII图生成.md` |
| `prompt-kits/drag-ready/user/03-data-pipeline.md` | 把任意输入转换成箭头式数据管道描述 | `i18n/zh/prompts/user_prompts/数据管道.md` |

## 维护建议
1. 需要添加新提示词时，先在 README 的表格补充一行，并更新复制脚本；
2. 同名提示词若有版本差异，可在文件名前添加 `v2`、`expert` 等标签；
3. 完成文档类修改后，记得运行 `make lint`，保持 Markdown 规范；
4. 若迁移到其他仓库，请保留 YAML Front Matter 以记录来源和可追溯信息。
