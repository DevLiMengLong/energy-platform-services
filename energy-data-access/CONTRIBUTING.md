# 贡献指南

感谢你关注 Energy Data Access。

## 本地开发

```bash
docker compose -f deploy/docker-compose.dev.yml up -d
mvn test
```

## 分支与合并规则

- `main` 是稳定主分支，不接受直接推送。
- 外部贡献者请 fork 后提交 Pull Request。
- 仓库协作者请从 `main` 拉出功能分支，例如 `feature/cleaning-rule`、`fix/kafka-retry`。
- 所有代码变更必须通过 Pull Request 合并到 `main`。
- Pull Request 合并前至少需要完成一次代码审查，并通过基础测试。
- 紧急修复也应走 Pull Request 流程，避免直接修改 `main`。

提交 PR 前请至少运行：

```bash
mvn test
```

## 代码风格

- Java 版本保持 Java 8 兼容。
- 尽量保持模块边界清晰：接入、协议、存储、清洗、聚合、查询分别放在对应 package。
- 不要在代码、配置和文档中提交真实客户信息、公司信息、密钥、内网地址。
- 新增配置项时，同步更新 `README.md` 或 `docs/technical-solution.md`。

## Issue 建议

提交问题时建议包含：

- 使用的版本或 commit。
- 运行环境。
- 复现步骤。
- 期望结果和实际结果。
- 相关日志。
