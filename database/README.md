# 数据库目录

数据库目录用于保存 SQL、表结构说明和 Redis key 设计。

## 目录规划

```text
database/
  mysql/
    init/
    migration/
  redis/
```

## 当前规则

- 先写 SQL，再写实体。
- SQL 要有注释。
- 表名、字段名保持清晰，不提前复杂化。
