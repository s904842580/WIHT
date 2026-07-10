# 数据库设计草稿

当前版本：V0.1  
当前范围：第一阶段 MySQL 表设计，不生成 SQL。

## 设计原则

- 表结构先服务当前模块，不提前做复杂权限、工作流、分库分表。
- 表名、字段名使用 `snake_case`。
- 主键统一使用 `BIGINT`。
- 时间字段统一使用 `created_at`、`updated_at`。
- 关联字段使用 `xxx_id`，第一阶段先按逻辑外键处理。
- 文章、项目、素材都保留状态字段，方便后续做后台管理。
- 素材表只记录元数据，不直接保证素材可公开。

## 通用字段约定

| 字段 | 建议类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |
| `status` | `VARCHAR(20)` | 业务状态，按表定义取值 |
| `sort_order` | `INT` | 排序值，越小越靠前 |

## 表关系概览

```text
waht_user
  ├─ waht_note.created_by
  └─ waht_note.updated_by

waht_note_category
  └─ waht_note.category_id

waht_note
  └─ waht_note_tag_rel
       └─ waht_note_tag

waht_project
  ├─ waht_project_link
  └─ waht_project_tech_rel
       └─ waht_tech_stack

waht_asset
  ├─ waht_note.cover_asset_id
  └─ waht_project.cover_asset_id
```

## 用户表：waht_user

用途：保存平台用户基础信息，为后续登录、后台管理预留。

| 字段 | 建议类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `username` | `VARCHAR(50)` | 用户名，唯一 |
| `password` | `VARCHAR(100)` | 密码 |
| `nickname` | `VARCHAR(50)` | 昵称 |
| `avatar_url` | `VARCHAR(500)` | 头像地址 |
| `role` | `VARCHAR(20)` | 角色：`ADMIN`、`USER` |
| `status` | `VARCHAR(20)` | 状态：`ACTIVE`、`DISABLED` |
| `last_login_at` | `DATETIME` | 最后登录时间 |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

建议索引：

- 唯一索引：`username`
- 普通索引：`status`

## 笔记分类表：waht_note_category

用途：保存学习笔记、游戏科普等内容分类。

| 字段 | 建议类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `name` | `VARCHAR(50)` | 分类名称 |
| `slug` | `VARCHAR(80)` | 分类访问标识，唯一 |
| `description` | `VARCHAR(300)` | 分类说明 |
| `sort_order` | `INT` | 排序值 |
| `status` | `VARCHAR(20)` | 状态：`ACTIVE`、`HIDDEN` |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

建议索引：

- 唯一索引：`slug`
- 普通索引：`status`、`sort_order`

## 学习笔记表：waht_note

用途：保存学习笔记文章。游戏科普第一阶段也先作为笔记分类承载，不单独建表。

| 字段 | 建议类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `title` | `VARCHAR(120)` | 标题 |
| `slug` | `VARCHAR(150)` | 访问标识，唯一 |
| `summary` | `VARCHAR(500)` | 摘要 |
| `content` | `LONGTEXT` | 正文 |
| `category_id` | `BIGINT` | 分类 ID |
| `cover_asset_id` | `BIGINT` | 封面素材 ID，可为空 |
| `status` | `VARCHAR(20)` | 状态：`DRAFT`、`PUBLISHED`、`HIDDEN` |
| `view_count` | `BIGINT` | 浏览次数 |
| `published_at` | `DATETIME` | 发布时间 |
| `created_by` | `BIGINT` | 创建用户 ID |
| `updated_by` | `BIGINT` | 更新用户 ID |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

建议索引：

- 唯一索引：`slug`
- 普通索引：`category_id`
- 普通索引：`status`、`published_at`

## 笔记标签表：waht_note_tag

用途：保存笔记标签，例如 `Java`、`Spring Boot`、`MySQL`、`游戏系统设计`。

| 字段 | 建议类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `name` | `VARCHAR(50)` | 标签名称 |
| `slug` | `VARCHAR(80)` | 标签访问标识，唯一 |
| `color` | `VARCHAR(20)` | 标签颜色，可为空 |
| `status` | `VARCHAR(20)` | 状态：`ACTIVE`、`HIDDEN` |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

建议索引：

- 唯一索引：`slug`
- 普通索引：`status`

## 笔记标签关联表：waht_note_tag_rel

用途：保存笔记和标签的多对多关系。

| 字段 | 建议类型 | 说明 |
| --- | --- | --- |
| `note_id` | `BIGINT` | 笔记 ID |
| `tag_id` | `BIGINT` | 标签 ID |
| `created_at` | `DATETIME` | 创建时间 |

建议索引：

- 联合唯一索引：`note_id`、`tag_id`
- 普通索引：`tag_id`

## 项目展示表：waht_project

用途：保存项目展示信息。

| 字段 | 建议类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `name` | `VARCHAR(100)` | 项目名称 |
| `slug` | `VARCHAR(120)` | 访问标识，唯一 |
| `summary` | `VARCHAR(500)` | 项目摘要 |
| `description` | `LONGTEXT` | 项目详情 |
| `cover_asset_id` | `BIGINT` | 封面素材 ID，可为空 |
| `status` | `VARCHAR(20)` | 状态：`DRAFT`、`PUBLISHED`、`HIDDEN` |
| `sort_order` | `INT` | 排序值 |
| `started_at` | `DATE` | 项目开始日期 |
| `ended_at` | `DATE` | 项目结束日期，可为空 |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

建议索引：

- 唯一索引：`slug`
- 普通索引：`status`、`sort_order`

## 项目链接表：waht_project_link

用途：保存项目仓库、演示、文档等链接。

| 字段 | 建议类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `project_id` | `BIGINT` | 项目 ID |
| `link_type` | `VARCHAR(20)` | 链接类型：`REPO`、`DEMO`、`DOC`、`VIDEO`、`OTHER` |
| `title` | `VARCHAR(80)` | 链接标题 |
| `url` | `VARCHAR(500)` | 链接地址 |
| `sort_order` | `INT` | 排序值 |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

建议索引：

- 普通索引：`project_id`
- 普通索引：`link_type`

## 技术栈表：waht_tech_stack

用途：保存项目使用的语言、框架、数据库和工具。

| 字段 | 建议类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `name` | `VARCHAR(50)` | 技术名称 |
| `slug` | `VARCHAR(80)` | 技术访问标识，唯一 |
| `tech_type` | `VARCHAR(30)` | 类型：`LANGUAGE`、`FRAMEWORK`、`DATABASE`、`TOOL`、`OTHER` |
| `icon_url` | `VARCHAR(500)` | 图标地址，可为空 |
| `sort_order` | `INT` | 排序值 |
| `status` | `VARCHAR(20)` | 状态：`ACTIVE`、`HIDDEN` |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

建议索引：

- 唯一索引：`slug`
- 普通索引：`tech_type`
- 普通索引：`status`

## 项目技术栈关联表：waht_project_tech_rel

用途：保存项目和技术栈的多对多关系。

| 字段 | 建议类型 | 说明 |
| --- | --- | --- |
| `project_id` | `BIGINT` | 项目 ID |
| `tech_id` | `BIGINT` | 技术栈 ID |
| `created_at` | `DATETIME` | 创建时间 |

建议索引：

- 联合唯一索引：`project_id`、`tech_id`
- 普通索引：`tech_id`

## 素材元数据表：waht_asset

用途：记录素材元数据。素材文件本体可以先放在本地目录或后续 MinIO。

| 字段 | 建议类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `name` | `VARCHAR(120)` | 素材名称 |
| `asset_type` | `VARCHAR(30)` | 类型：`IMAGE`、`MODEL`、`AUDIO`、`VIDEO`、`OTHER` |
| `storage_path` | `VARCHAR(500)` | 存储路径 |
| `thumbnail_path` | `VARCHAR(500)` | 缩略图路径，可为空 |
| `file_ext` | `VARCHAR(20)` | 文件扩展名 |
| `file_size` | `BIGINT` | 文件大小，单位 byte |
| `source_type` | `VARCHAR(30)` | 来源：`ORIGINAL`、`OFFICIAL`、`NETWORK`、`OPEN_SOURCE`、`UNKNOWN` |
| `source_url` | `VARCHAR(500)` | 来源地址，可为空 |
| `source_note` | `VARCHAR(500)` | 来源说明 |
| `license_note` | `VARCHAR(500)` | 授权说明 |
| `public_status` | `VARCHAR(30)` | 公开状态：`LOCAL_ONLY`、`REVIEWING`、`PUBLIC_SAFE` |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

建议索引：

- 普通索引：`asset_type`
- 普通索引：`source_type`
- 普通索引：`public_status`

## 第一阶段建表顺序

1. `waht_user`
2. `waht_note_category`
3. `waht_note_tag`
4. `waht_asset`
5. `waht_note`
6. `waht_note_tag_rel`
7. `waht_project`
8. `waht_project_link`
9. `waht_tech_stack`
10. `waht_project_tech_rel`

## 暂不建表

- 权限菜单表：第一阶段只用 `role` 字段。
- 操作日志表：等后台管理成型后再加。
- 评论表：当前没有互动需求。
- 游戏科普专表：先用 `waht_note_category` 区分。
- 文件分片、上传任务表：当前素材文件不由后端完整管理。
