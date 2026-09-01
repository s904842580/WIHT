# 后端目录

后端是 WAHT 的当前主线。

## 目录规划

```text
backend/
  waht-java/       # [NOW] Spring Boot 3 主后端
  waht-go/         # [LATER] Go 辅助服务
  ai-service/      # [NOW] Python AI 学习助手服务
```

## 当前规则

- `waht-java` 继续负责核心业务和 Agent Gateway。
- `waht-go` 只留说明，不写代码。
- `ai-service` 负责模型调用、工具编排、会话和草稿审批，不直连 Java 业务表。
- 每个 Java 文件尽量写清楚注释，方便学习复盘。
