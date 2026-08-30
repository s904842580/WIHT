# MySQL

MySQL 是第一阶段主数据库。

## 目录

```text
mysql/
  init/          # 初始建表脚本
  migration/     # 后续迁移脚本
```

## 已有表

- 用户表。
- 学习笔记表。
- 项目展示表。
- 素材元数据表。

初始化表结构位于 `init/001_init_schema.sql`，演示用户、笔记和项目数据位于后续 init 脚本。

## 执行规则

- 新环境按文件编号依次执行 `init/`。
- 已有环境只执行尚未执行的 `migration/`。
- MySQL CLI 必须使用 `--default-character-set=utf8mb4`，避免中文内容损坏。
- 迁移脚本执行后不再回改，后续变更新增文件。

具体命令见 `docs/runbook-local.md`。
