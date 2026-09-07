# WAHT AI Agent Service

ai-service 是 WAHT 的独立 Python 学习助手服务。它负责模型调用、Agent 工具编排、会话记录和草稿审批，不直接访问 Java 核心业务表。

## 技术栈

Python 3.11+、FastAPI、Pydantic、HTTPX、OpenAI Agents SDK、SQLAlchemy 和 pytest。

## 本地启动

1. 进入 backend/ai-service。
2. 执行 python -m venv .venv。
3. 执行 .venv\Scripts\python.exe -m pip install -e ".[dev]"。
4. 设置 OPENAI_API_KEY、WAHT_AGENT_DB_URL 等环境变量。
5. 执行 `.venv\Scripts\python.exe src\waht_agent\main.py`，或者在 PyCharm 中直接运行 `main.py`。

`main.py` 会通过 Uvicorn 启动服务，并读取 `WAHT_AGENT_HOST` 和 `WAHT_AGENT_PORT`；默认监听 `127.0.0.1:8000`。需要 Uvicorn 自动重载时，仍可使用标准命令：

```powershell
.\.venv\Scripts\python.exe -m uvicorn waht_agent.main:app --host 127.0.0.1 --port 8000 --reload
```

MySQL 首次执行 `database/mysql/init/005_init_agent_schema.sql`。Java 的 `WAHT_AGENT_SERVICE_TOKEN` 必须和本服务相同。

没有设置 OPENAI_API_KEY 时，健康检查和会话查询仍可使用，但发送消息会返回明确的 503，不会伪造模型回答。

## 服务边界

- Java 通过 X-WAHT-Service-Token 调用本服务。
- Python 使用 Java 签发的短期 delegation token 调用笔记工具。
- delegation token 不写入数据库或日志。
- 创建笔记先生成审批请求，批准后才调用 Java 创建 DRAFT。
- 测试使用 SQLite 临时数据库和 Fake Agent Runner，不消耗模型配额。

## 测试

```powershell
cd E:\projects\WAHT\backend\ai-service
.\.venv\Scripts\python.exe -m pytest -q
```

当前是同步 REST 版本，不提供 SSE 或 WebSocket 流式输出。
