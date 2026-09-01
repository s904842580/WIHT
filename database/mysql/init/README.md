# init

初始建表脚本目录。

规则：

- 只放从 0 初始化数据库需要的 SQL。
- 文件名建议：`001_init_user.sql`。
- SQL 必须写字段注释。

## 当前脚本

- `001_init_schema.sql`：初始化表结构。
- `002_init_admin_user.sql`：初始化本地管理员。
- `003_init_note_demo_data.sql`：初始化学习笔记演示数据。
- `004_init_project_demo_data.sql`：初始化项目展示演示数据。
- `005_init_agent_schema.sql`：初始化 Python Agent 独立数据库 `waht_ai` 的会话、运行、工具与审批表。
