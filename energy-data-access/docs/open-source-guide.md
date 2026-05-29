# 开源说明

## 项目名称

Energy Data Access

## 开源定位

本项目面向中国工业能源数据场景，提供一个可运行、可改造、可二次开发的数据接入、清洗和聚合服务。项目重点不是做完整商业平台，而是沉淀一套清晰的工程实现和技术方案，方便个人开发者、系统集成商、工业软件团队快速搭建原型或试点系统。

## 适合人群

- 做能源管理、工业互联网、智慧园区、楼宇自控相关系统的开发者。
- 需要接入电表、水表、气表、热表数据的项目团队。
- 希望学习 Kafka、ClickHouse、Redis 在工业时序数据场景中组合使用的开发者。
- 需要一个轻量数采平台侧服务作为二次开发基础的团队。

## 开源协议

项目使用 MIT License。你可以自由使用、复制、修改、合并、发布、分发和商用，但需要保留版权声明和许可证文本。

## 贡献方式

欢迎提交 Issue 和 Pull Request。建议优先从以下方向贡献：

- 完善文档和安装说明。
- 增加更多测试用例。
- 补充 TDengine 清洗和聚合表适配。
- 增加认证鉴权和租户级权限控制。
- 增加监控指标和运行面板。
- 优化大规模多租户场景下的 Kafka partition、ClickHouse 表结构和查询性能。

## 分支建议

- `main`：稳定可运行版本。
- `main` 不允许直接推送，所有变更通过 Pull Request 合并。
- 功能开发建议从 `main` 拉出 feature 分支。
- 外部贡献者建议 fork 仓库后提交 Pull Request。
- PR 中请说明变更内容、验证方式和兼容性影响。
- 仓库维护者应在 GitHub 和 Gitee 均开启 `main` 分支保护，要求 Pull Request 审查后才能合并。

## 提交规范建议

推荐使用简洁的英文或中文提交说明：

```text
feat: add tdengine aggregate storage
fix: handle redis latest write failure
docs: update quick start guide
test: add cleaning anomaly tests
```

## 生产使用提醒

当前项目仍是开源基础版本，生产落地前建议至少补充：

- 接口认证和租户权限校验。
- Kafka、ClickHouse、Redis 的高可用部署。
- 数据库账号密码和敏感配置管理。
- 监控告警。
- 数据备份和恢复策略。
- 压测和容量规划。
