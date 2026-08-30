# WAHT

WAHT 是一个用于记录学习笔记、展示个人项目和持续练习全栈开发的个人平台。

## 当前能力

- Spring Boot 3 + MyBatis-Plus + MySQL 后端。
- React + TypeScript + Vite 前端。
- 用户注册、登录、JWT 鉴权、登录态恢复和私有缓存隔离。
- 学习笔记公开分页、关键词与分类标签筛选、Markdown 详情和浏览次数。
- 作者工作台：笔记 CRUD、草稿发布、Markdown 工具栏、全屏预览和本地恢复。
- 笔记分类、标签和项目展示公开查询。
- 统一 `BaseResponse` 响应和 `ServiceException` 异常处理。
- 可配置 CORS、请求编号、环境预检和一键构建验证。

## 目录说明

```text
backend/waht-java/       Spring Boot 主后端
frontend/waht-web/       React 用户前台和作者工作台
database/mysql/          MySQL 初始化与迁移脚本
docs/                    架构、开发规则和进度文档
assets/                  素材及素材使用规则
```

## 本地入口

后端默认地址：

```text
http://localhost:8080
```

前端默认地址：

```text
http://localhost:5173
```

常用页面：

```text
/login                   登录
/register                注册
/notes                   公开笔记
/workspace/notes         我的笔记
/workspace/notes/new     新建笔记
/projects                项目展示
```

详细启动步骤见 `docs/runbook-local.md`，模块进度见 `docs/module-plan.md`，真实结构、依赖、数据库和文档基线见 `docs/project-inventory.md`。

首次运行建议执行：

```powershell
.\scripts\check-local.ps1
.\scripts\verify-project.ps1
```

## 当前边界

- 保持 Java 单体后端，不提前拆微服务。
- 暂不接入 Go、AI、Docker 和 Kubernetes。
- JWT 第一阶段存储在浏览器 `localStorage`，正式公网部署前再评估 HttpOnly Cookie。
- 版权不明确的素材只用于本地学习，不进入公开发布目录。
