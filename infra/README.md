# 基础设施目录

基础设施目录用于后续部署。

## 当前结论

当前阶段不生成 Docker、Nginx、k8s 配置。

原因：

- 你的电脑可能无法稳定运行 Docker。
- 当前首要目标是 IDEA 能跑 Java 后端。
- 云原生是后续升级方向。

## 目录

```text
infra/
  local/
  nginx/
  docker/
  k8s/
```
