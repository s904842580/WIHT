# AI 学习助手架构

## 当前目标

第一版只实现作者学习助手，不做流式响应、多 Agent、向量数据库、Redis、消息队列或 LangGraph。

可用能力：

- 创建和查询学习会话。
- 基于当前用户的笔记搜索、读取并回答问题。
- 查询可用分类与标签。
- 生成结构化笔记草稿。
- 用户批准、修改后批准或拒绝草稿。
- 只有批准后才由 Java 写入 `waht_note`，状态固定为 `DRAFT`。

## 服务边界

```text
React 作者工作台
        |
        | 浏览器登录 JWT
        v
Spring Boot Agent Gateway
        |
        | X-WAHT-Service-Token
        v
FastAPI Agent Service ------> OpenAI Agents SDK / 模型
        |
        | 2 分钟 delegation JWT + 服务令牌
        v
Spring Boot 内部笔记工具 ------> waht MySQL

FastAPI Agent Service ------> waht_ai MySQL
```

Java 仍是用户、鉴权、笔记和项目的业务所有者。Python 不直连 `waht`，只保存自己的会话、运行、工具摘要和审批状态。

## 授权规则

- 普通登录 JWT 的 `token_use` 为 `user`，只用于浏览器访问 Java。
- Agent delegation JWT 的 `token_use` 为 `agent_delegation`，`aud` 为 `waht-agent`。
- `note:read` 只允许搜索、读取笔记和读取分类标签。
- `note:create-draft` 只允许审批后创建草稿。
- delegation 默认两分钟失效，不写入数据库和日志。
- Python 调用 Java 内部工具时还必须携带相同的 `X-WAHT-Service-Token`。

## 数据归属

`waht`：

- 用户、登录状态和角色。
- 笔记、分类、标签。
- `waht_agent_note_draft_request`，保存审批 ID 到笔记 ID 的幂等映射。

`waht_ai`：

- `agent_conversation`：会话。
- `agent_message`：用户和助手消息。
- `agent_run`：单次模型运行、状态和 token 用量。
- `agent_tool_call`：脱敏后的工具参数与结果摘要。
- `agent_approval`：待审批草稿和最终处理结果。

## 接口

浏览器访问 Java：

```text
GET  /api/agent/conversations
POST /api/agent/conversations
GET  /api/agent/conversations/{conversationId}
POST /api/agent/conversations/{conversationId}/messages
POST /api/agent/approvals/{approvalId}
```

Java 访问 Python：

```text
GET  /internal/v1/conversations
POST /internal/v1/conversations
GET  /internal/v1/conversations/{conversationId}
POST /internal/v1/conversations/{conversationId}/messages
POST /internal/v1/approvals/{approvalId}
```

Python 访问 Java 工具：

```text
POST /api/internal/agent-tools/notes/search
GET  /api/internal/agent-tools/notes/{noteId}
GET  /api/internal/agent-tools/note-metadata
POST /api/internal/agent-tools/note-drafts
```

## 后续演进

满足以下条件后再引入新组件：

- 笔记达到足够规模且关键词搜索质量不足时，再评估 embedding 和向量检索。
- 出现长任务、重试和削峰需求时，再引入消息队列。
- 出现跨任务长期记忆需求时，再设计可删除、可查看、可纠正的 Memory。
- 单 Agent 的工具选择和状态机无法维护时，再评估 LangGraph 或多 Agent。
- 流式响应应在同步链路稳定、错误与审批语义明确后单独实现。
