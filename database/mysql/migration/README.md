# migration

数据库迁移脚本目录。

规则：

- 每次改表都新增迁移文件。
- 不直接修改已经执行过的旧 SQL。
- 文件名建议：`20260626_001_add_note_table.sql`。

## 当前脚本

- `20260711_001_repair_note_demo_utf8.sql`：修复早期本地导入时损坏的中文笔记演示数据。
- `20260830_001_add_agent_note_draft_request.sql`：增加 Agent 审批创建笔记的幂等记录表。
