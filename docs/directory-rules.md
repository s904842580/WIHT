# 目录存放规则

本文档规定 WAHT 项目中文件应该放在哪里。

## 总原则

- 代码、文档、素材、部署配置分开。
- 当前能用的目录正常维护。
- 后续技术目录只放 README，占位即可。
- 官方或网络素材只能本地使用，不进入公开发布链路。

## 后端目录

```text
backend/
  waht-java/       # [NOW] Spring Boot 3 主后端
  waht-go/         # [LATER] Go 辅助服务
  ai-service/      # [LATER] AI 助手服务
```

规则：

- `waht-java/` 是主业务后端，优先保证可运行、可维护。
- `waht-go/` 只在 Java 主体稳定后加入，用于排行榜、任务 Worker、实时通知等边界模块。
- `ai-service/` 等笔记、素材、游戏科普有内容后再做。

## 前端目录

```text
frontend/
  waht-web/        # [NOW] 用户前台和个人写作台
  waht-admin/      # [LATER] 管理后台
```

规则：

- `waht-web/` 已承载公开内容、登录注册和个人笔记写作台。
- `waht-admin/` 等后端内容管理接口稳定后再做。

## 数据库目录

```text
database/
  mysql/
    init/          # 初始建表脚本
    migration/     # 版本迁移脚本
  redis/           # Redis key 设计说明
```

规则：

- 表结构先写 SQL，再写实体。
- 每次改表都在 `migration/` 留记录。
- Redis key 必须写用途、过期时间、数据结构。

## 素材目录

```text
assets/
  official-reference/   # [LOCAL] 官方素材，本地参考
  fan-reference/        # [LOCAL] 网络同人参考
  generated/            # [NEXT] AI 生成素材
  original/             # [NEXT] 自己原创素材
  public-safe/          # [LATER] 可公开使用素材
```

规则：

- 当前可以先收集官方和网络素材。
- 不要把官方素材用于公开站点或商用展示。
- 后期公开部署只允许使用 `public-safe/`。

## 部署目录

```text
infra/
  local/          # [NOW] 本地启动说明索引
  nginx/          # [LATER] Nginx 配置
  docker/         # [LATER] Docker 配置
  k8s/            # [LATER] k3s/Kubernetes 学习
```

规则：

- 你的电脑暂时不稳定跑 Docker，当前不生成 Docker 配置。
- 先保证 IDEA 能启动 Java 后端。
- k8s 只做学习记录，不进入当前开发。
