# 本地运行说明

本文档记录本地开发环境规则。

## 当前前提

- 操作系统：Windows。
- IDE：优先 IDEA。
- Java：JDK 17。
- Maven 本地仓库：`G:\maven\repository`。
- 项目目录：`E:\projects\WAHT`。

## Maven 规则

后续生成 Java 后端时，需要在项目中保留：

```text
.mvn/maven.config
```

内容：

```text
-Dmaven.repo.local=G:/maven/repository
```

目的：

- Maven 依赖不下载到 C 盘。
- IDEA 和命令行使用同一套 Maven 仓库。

## IDEA 规则

打开项目时优先打开：

```text
E:\projects\WAHT
```

如果后续只想调后端，可以打开：

```text
E:\projects\WAHT\backend\waht-java
```

但推荐打开根目录，方便同时查看文档、SQL 和后端。

## 数据库规则

第一阶段优先使用本机已有服务：

- MySQL。
- Redis。
- MinIO。

当前不强制使用 Docker。

## 启动顺序

后续后端生成后，推荐顺序：

1. 确认 MySQL 可用。
2. 确认 Maven 仓库指向 G 盘。
3. IDEA 导入 Maven 项目。
4. 启动 Spring Boot。
5. 访问健康检查接口。

具体命令等后端代码生成后再补充。
