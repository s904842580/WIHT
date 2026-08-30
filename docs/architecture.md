# 整体架构

## 当前架构判断

当前阶段采用“Java 单体主后端 + 预留扩展目录”的方式。

原因：

- 你现在 Java 更熟，先把主业务做稳定。
- Go、AI、云原生还在学习期，不应提前进入核心链路。
- 单体项目更适合当前进度，后续可以按模块拆分。

## 第一阶段架构

```text
浏览器 / IDEA 调试
        |
        v
Spring Boot 3 后端
        |
        +-- MySQL：用户、笔记、项目、素材元数据
        +-- Redis：缓存、验证码、排行榜草稿
        +-- MinIO：图片、素材、项目封面
```

第一阶段只要求：

- IDEA 能启动后端。
- 接口结构清晰。
- 数据库表设计可维护。
- 代码注释充分，便于后续复盘。

## 当前前后端架构

```text
React + TypeScript + Vite 前台
        |
        v
Spring Boot 3 API
        |
        +-- MySQL
        +-- Redis
        +-- MinIO
```

前端同时承载公开页面和轻量作者工作台：

- 首页
- 学习笔记
- 项目展示
- 游戏科普
- 素材展示
- 登录、注册
- 我的笔记与 Markdown 编辑器

认证请求链路：

```text
浏览器 Authorization: Bearer token
        |
        v
JwtAuthInterceptor：校验签名和有效期
        |
        v
waht_user：再次确认用户存在且状态为 ACTIVE
        |
        v
@LoginUser CurrentUser：注入 Controller 参数
```

笔记写作链路：

```text
作者工作台 -> /api/my/notes -> NoteService -> MyBatis-Plus -> MySQL
公开博客   -> /api/notes    -> PUBLISHED 状态 + 分页与组合筛选
```

## 第三阶段架构

加入 Go：

```text
Spring Boot 3 主业务
        |
        +-- Go 服务：排行榜、任务 Worker、实时通知
```

Go 不负责主业务。
Go 用来学习高并发、轻量服务和工具型服务。

## 第四阶段架构

加入 AI 和云原生：

- AI 助手：游戏开发知识问答、学习笔记问答。
- Docker：本地环境统一。
- k3s：学习 Kubernetes。
- Prometheus/Grafana：监控学习。

这些都不是当前阶段目标。
