# 开发规则

## 基本原则

- 每次围绕一条可验证的业务闭环推进，避免同时展开无关模块。
- 先写规则和设计，再写代码。
- 后端结构由 WAHT 自己维护，不强制参考其他项目。
- 每个 Java 文件尽量写注释，尤其是类职责、字段含义、方法目的。
- 当前阶段优先保证 IDEA、前端开发服务器和 MySQL 联调稳定，不优先考虑 Docker、k8s、Go、AI。

## 后端规则

后端主线使用 Spring Boot 3。

推荐演进方式：

```text
第一阶段：Spring Boot 3 单体后端
第二阶段：按业务模块拆包
第三阶段：稳定后再考虑 Go 辅助服务
第四阶段：再考虑 AI 服务和云原生部署
```

当前后端不要求参考 `jinxi-platform`。

可以借鉴通用工程思想：

- 分层清晰。
- 命名清晰。
- 统一响应。
- 统一异常。
- SQL 和实体对应明确。
- 接口注释和业务注释完整。

但 WAHT 的包名、模块名、目录结构以自身规划为准。

当前统一约定：

- Controller 响应使用 `BaseResponse<T>`。
- Service 的可预期业务错误使用 `ServiceException`。
- 简单单表 CRUD 优先使用 MyBatis-Plus `BaseMapper`。
- 复合主键和原子更新使用明确的 Mapper SQL。
- TypeScript 禁止 `any`，接口数据必须声明具体类型。

## 注释规则

必须写注释的位置：

- 类的职责。
- DTO 字段含义。
- Entity 字段含义。
- Controller 接口用途。
- Service 方法业务含义。
- Redis key 设计。
- SQL 字段注释。

不需要写废话注释：

- 不写“获取 name”这种无信息注释。
- 不重复解释 Java 语法。

## 每日开发规则

每天结束后补一篇开发日志：

```text
docs/day-log/yyyy-MM-dd-day-n.md
```

内容包括：

- 今天做了什么。
- 为什么这么做。
- 遇到的问题。
- 明天准备做什么。

## 暂缓规则

以下内容当前不做：

- Docker 配置。
- k8s 配置。
- Go 服务代码。
- AI 服务代码。
- GitHub Actions。

原因是当前目标是先把登录、内容写作和公开展示主线跑稳。
