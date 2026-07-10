# 素材使用规则

## 核心规则

素材必须区分来源和可公开状态。

当前阶段允许使用网络和官方素材做本地学习，但不能提交 GitHub，不能公开部署，不能商用。

## 目录规则

```text
assets/
  official-reference/   # 官方素材，仅本地
  fan-reference/        # 网络同人参考，仅本地
  generated/            # AI 生成素材，默认不提交
  original/             # 自己原创素材
  public-safe/          # 可公开素材
```

## GitHub 提交规则

不提交：

- `assets/official-reference/` 中的素材文件。
- `assets/fan-reference/` 中的素材文件。
- 未审核的 `assets/generated/` 素材文件。

可以提交：

- 各目录的 `README.md`。
- `assets/original/` 中你自己创作的素材。
- `assets/public-safe/` 中确认可公开的素材。

## 官方素材规则

官方素材只能用于：

- 本地页面练习。
- 视觉参考。
- 临摹学习。
- 私人项目预览。

不能用于：

- 公开部署。
- 商业用途。
- GitHub 公开仓库素材提交。
- 对外宣传图。

## 网络同人素材规则

网络同人素材只能用于参考。

如果未来要公开使用，必须确认：

- 作者是谁。
- 是否允许转载。
- 是否允许二次创作。
- 是否允许商用。
- 是否需要署名。

## public-safe 判定

素材进入 `public-safe/` 前必须满足至少一项：

- 自己原创。
- 已获得授权。
- 使用明确开源许可。
- 来自可商用素材库，且符合许可。

不确定就不要放进 `public-safe/`。
