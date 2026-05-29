# 微服务开发规范

本文档用于后续新增微服务时快速对齐当前仓库约定，避免每次都重新阅读全部目录。新增服务前优先读本文档；需要看样例时，再按文中给出的少量文件定位。

## 1. 当前工程基线

### 1.1 工程形态

- 根工程是 Maven 聚合工程，父 POM 位于 `pom.xml`，统一管理 Java 版本、Spring Boot 版本和 Maven 插件版本。
- 当前运行时基线：
  - Java 17
  - Spring Boot 3.3.5
  - MySQL 8
  - Flyway
  - Docker 部署
- 已有模块：
  - `platform-basic-service`：平台管理和基础信息服务，当前最完整的分层样例。
  - `log-service`：登录日志和操作日志服务，当前具备启动类、配置、Flyway 脚本和接口测试期望，可作为新服务最小骨架参考。
  - `deploy/energy-platform`：部署脚本、Nginx 转发、启动停止脚本和冒烟测试。

### 1.2 架构约束

- 一个微服务拥有一个独立 MySQL 数据库。
- 服务之间只能通过 API 通信，不能跨服务直接查表或复用对方数据库连接。
- 共享能力优先通过 HTTP API、网关路由、公共约定复制实现；只有当重复代码足够稳定且跨多个服务反复出现时，再考虑提取公共模块。
- 新服务必须明确自己的业务边界、数据库边界、接口前缀、部署端口、健康检查和冒烟测试入口。

## 2. 新增微服务的最小阅读路径

新增微服务时，不需要全量阅读现有代码。按下面顺序读即可：

1. 根 POM：`pom.xml`
   - 看 `<modules>`、Java 版本、Spring Boot 版本、插件管理。
2. 完整服务样例：`platform-basic-service/pom.xml`
   - 看子模块依赖、Spring Boot repackage 配置。
3. 完整配置样例：`platform-basic-service/src/main/resources/application.yml`
   - 看端口、应用名、数据源、Flyway、Jackson、Actuator、服务间调用配置。
4. API 和通用响应样例：
   - `platform-basic-service/src/main/java/com/getech/energy/platformbasic/common/ApiResponse.java`
   - `platform-basic-service/src/main/java/com/getech/energy/platformbasic/common/GlobalExceptionHandler.java`
   - `platform-basic-service/src/main/java/com/getech/energy/platformbasic/common/TraceFilter.java`
5. 分层和租户隔离样例：
   - `platform-basic-service/src/main/java/com/getech/energy/platformbasic/catalog/PlatformController.java`
   - `platform-basic-service/src/main/java/com/getech/energy/platformbasic/catalog/BasicController.java`
   - `platform-basic-service/src/main/java/com/getech/energy/platformbasic/catalog/ResourceService.java`
   - `platform-basic-service/src/main/java/com/getech/energy/platformbasic/catalog/MutationService.java`
6. 数据库迁移样例：
   - `platform-basic-service/src/main/resources/db/migration/V1__create_platform_basic_schema.sql`
   - `log-service/src/main/resources/db/migration/V1__create_log_schema.sql`
7. 测试样例：
   - `platform-basic-service/src/test/java/com/getech/energy/platformbasic/PlatformBasicApiTest.java`
   - `log-service/src/test/java/com/getech/energy/logservice/LogServiceApiTest.java`
8. 部署接入样例：
   - `deploy/energy-platform/scripts/start.sh`
   - `deploy/energy-platform/scripts/init-mysql.sh`
   - `deploy/energy-platform/scripts/smoke-test.sh`
   - `deploy/energy-platform/nginx/default.conf`

## 3. 模块脚手架规范

### 3.1 命名

- Maven 模块名使用 kebab-case，并以 `-service` 结尾，例如 `metering-service`、`alarm-service`。
- `artifactId` 与模块目录名保持一致。
- `spring.application.name` 与模块名保持一致。
- Java 包名使用 `com.getech.energy.<serviceName>`，其中 `<serviceName>` 使用连续小写单词，例如：
  - `platform-basic-service` -> `com.getech.energy.platformbasic`
  - `log-service` -> `com.getech.energy.logservice`

### 3.2 根 POM 接入

新增模块后，必须在根 `pom.xml` 中注册：

```xml
<modules>
    <module>platform-basic-service</module>
    <module>log-service</module>
    <module>your-new-service</module>
</modules>
```

根 POM 负责统一版本。子模块不要重复声明 Spring Boot 版本、Maven compiler 版本、Surefire 版本。

### 3.3 子模块 POM

新服务默认依赖：

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-jdbc`
- `spring-boot-starter-actuator`
- `flyway-mysql`
- `mysql-connector-j`，scope 为 `runtime`
- `h2`，scope 为 `test`
- `spring-boot-starter-test`，scope 为 `test`

如果服务需要本地认证、密码编码或 Spring Security 过滤链，再加入 `spring-boot-starter-security`。不要因为现有服务用了 Security 就默认加到所有服务。

子模块必须配置 Spring Boot repackage：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 3.4 推荐目录结构

```text
your-new-service/
  pom.xml
  src/main/java/com/getech/energy/yourservice/
    YourServiceApplication.java
    common/
    config/
    <business-domain>/
    auth/        # 只有服务自己处理认证时需要
    logging/     # 只有服务主动调用日志服务时需要
  src/main/resources/
    application.yml
    db/migration/
      V1__create_your_service_schema.sql
  src/test/java/com/getech/energy/yourservice/
    YourServiceApiTest.java
  src/test/resources/
    application-test.yml
```

## 4. 配置规范

### 4.1 `application.yml`

每个服务必须包含：

- `server.port`：通过环境变量覆盖，默认端口不能与已有服务冲突。
- `spring.application.name`：等于模块名。
- `spring.datasource`：通过服务专属环境变量覆盖。
- `spring.flyway`：启用并使用 `classpath:db/migration`。
- `spring.jackson.time-zone`：使用 `Asia/Shanghai`。
- `spring.jackson.date-format`：使用 `yyyy-MM-dd HH:mm:ss`。
- `management.endpoints.web.exposure.include`：至少暴露 `health,info`。

示例：

```yaml
server:
  port: ${SERVER_PORT:8092}

spring:
  application:
    name: your-new-service
  datasource:
    url: ${YOUR_DB_URL:jdbc:mysql://127.0.0.1:3306/energy_your_service?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true}
    username: ${YOUR_DB_USERNAME:energy_your_user}
    password: ${YOUR_DB_PASSWORD:energy_your_pass}
    driver-class-name: com.mysql.cj.jdbc.Driver
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  jackson:
    time-zone: Asia/Shanghai
    date-format: yyyy-MM-dd HH:mm:ss

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### 4.2 测试配置

测试 profile 使用 H2 的 MySQL 兼容模式，并保持 Flyway 开启：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:energy_your_service;MODE=MySQL;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver
  flyway:
    enabled: true
```

## 5. API 规范

### 5.1 路径前缀

- 平台管理接口使用 `/api/platform/**`。
- 基础信息接口使用 `/api/basic/**`。
- 日志接口使用 `/api/logs/**`。
- 新服务必须定义清晰的独立前缀，例如 `/api/alarm/**`、`/api/metering/**`。
- 对外路径必须同步接入 Nginx 转发和冒烟测试。

### 5.2 响应体

新服务统一使用包含 `traceId` 的响应结构：

```java
public record ApiResponse<T>(String code, String message, String traceId, T data) {
    public static <T> ApiResponse<T> ok(T data, String traceId) {
        return new ApiResponse<>("SUCCESS", "OK", traceId, data);
    }

    public static <T> ApiResponse<T> fail(String code, String message, String traceId) {
        return new ApiResponse<>(code, message, traceId, null);
    }
}
```

分页结构沿用：

```java
public record PageResult(long total, int page, int size, List<Map<String, Object>> rows) {
}
```

如果业务响应结构稳定，优先定义明确的 response record；只有通用资源列表、动态列或原型快速验证场景才使用 `Map<String, Object>`。

### 5.3 错误码

- 成功码固定为 `SUCCESS`。
- 业务错误使用稳定的大写下划线编码，例如 `AUTH_FAILED`、`TENANT_REQUIRED`、`RESOURCE_NOT_FOUND`。
- 参数错误统一映射为 `VALIDATION_ERROR`。
- 未预期异常统一映射为 `SYSTEM_ERROR`。
- Controller 不直接拼异常响应，统一通过 `GlobalExceptionHandler` 输出。

### 5.4 Controller 约定

- Controller 只做协议层工作：接收参数、校验、获取当前用户、调用 Service、包装响应、记录必要操作日志。
- 复杂 SQL、事务、状态流转、租户隔离判断放到 Service。
- 写接口的 request body 必须加 `@Valid @RequestBody`。
- 请求对象使用 record，并用 `jakarta.validation` 声明必填、长度、格式、数值边界。
- 字段对外使用 camelCase；SQL 查询中通过 `AS camelCase` 做别名。

### 5.5 HTTP 方法

- `GET`：查询、详情、树、列表。
- `POST`：新增、复杂动作、批量保存、导入。
- `PUT`：完整更新或明确的编辑动作。
- `DELETE`：只有确实需要物理删除时使用；业务停用、启用优先使用动作接口。
- 状态类动作要能从接口名或 `actionCode` 看出语义，例如 `disableTenant`、`enableUsers`。

## 6. 数据库规范

### 6.1 Flyway

- 每个服务只维护自己模块下的 `src/main/resources/db/migration`。
- 命名格式：`V数字__描述.sql`，例如 `V1__create_alarm_schema.sql`。
- 已经部署共享环境的迁移脚本不要回改；新增变更通过新的版本脚本追加。
- 测试环境也走同一套迁移脚本，避免测试表结构和生产表结构漂移。

### 6.2 表命名

- 表名使用服务或业务前缀：
  - 基础信息：`basic_*`
  - 日志：`log_*`
  - 新服务示例：`alarm_*`、`metering_*`
- 字段使用 snake_case。
- 主键默认 `id BIGINT PRIMARY KEY AUTO_INCREMENT`。
- 多租户业务表必须包含 `tenant_id BIGINT NOT NULL`，并为高频查询建立包含 `tenant_id` 的索引。
- 常见业务字段：
  - `status VARCHAR(32) NOT NULL DEFAULT 'ENABLED'`
  - `deleted TINYINT NOT NULL DEFAULT 0`
  - `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`

### 6.3 租户隔离

- 租户域数据查询必须带 `tenant_id` 条件。
- 平台域数据可以不带 `tenant_id`，但必须有平台管理员权限校验。
- 通用查询只能使用白名单字段做过滤和排序，不能把前端传入字段名直接拼进 SQL。
- 使用 `JdbcClient` 时必须使用命名参数绑定，禁止拼接用户输入值。

## 7. 认证、权限和上下文

### 7.1 当前认证模式

`platform-basic-service` 当前通过 `AuthInterceptor` 保护 `/api/**`，排除登录、公开接口和 Actuator：

- 支持 `Authorization: Bearer <token>`。
- 兼容 `X-Auth-Token`。
- token 解析后写入 `AuthContext`。
- 请求结束必须清理 ThreadLocal 上下文。

新增服务如果需要保护业务接口，应复用这一套行为约定。后续如果多个服务都需要同样认证逻辑，再考虑抽取公共认证模块或网关鉴权。

### 7.2 权限边界

- 平台管理员接口必须显式校验 `roleType == PLATFORM_ADMIN`。
- 租户接口必须要求当前用户存在 `tenantId`。
- 不要只依赖前端隐藏按钮做权限控制；后端必须做最终权限判断。

## 8. 链路追踪和日志

### 8.1 TraceId

所有服务都应有 `TraceFilter`：

- 入站请求优先读取 `X-Trace-Id`。
- 缺失时生成 UUID。
- 写入 request attribute。
- 响应头返回 `X-Trace-Id`。
- API 响应体包含同一个 `traceId`。

### 8.2 操作日志

业务服务调用日志服务时，使用 HTTP API，不写日志库表。

登录日志接口：

- `POST /api/logs/login`
- 关键字段：`traceId`、`tenantId`、`userId`、`account`、`loginStatus`、`failureReason`、`clientIp`、`userAgent`

操作日志接口：

- `POST /api/logs/operation`
- 关键字段：`traceId`、`tenantId`、`userId`、`account`、`subsystemCode`、`moduleCode`、`actionCode`、`actionName`、`resourceType`、`resourceId`、`requestMethod`、`requestUri`、`clientIp`、`success`、`message`

非关键日志写入失败不应阻断主业务，当前 `LogClient` 采用 warn 后吞掉异常的方式。若某类审计日志属于强一致要求，必须在需求或设计阶段明确，不要沿用非阻断策略。

## 9. 代码分层规范

### 9.1 Controller

- 只处理 HTTP 协议、参数校验、认证上下文、调用 Service 和包装响应。
- 不承载复杂业务流程。
- 不直接处理事务。
- 除极少数只读详情接口外，不建议直接写 SQL。

### 9.2 Service

- 承载业务规则、数据访问、租户校验、状态流转。
- 涉及多条写入或先删后插的逻辑必须加 `@Transactional`。
- 捕获数据库唯一键冲突后转换为稳定业务错误码。
- 状态默认值、编码生成、业务幂等逻辑放在 Service 层。

### 9.3 通用查询 Registry

现有 `ResourceRegistry` + `ResourceDefinition` + `ResourceService` 适合大量结构相似的管理列表：

- `ResourceRegistry` 白名单声明资源、表、字段、固定条件、搜索列、过滤列、租户列和排序。
- `ResourceService` 统一处理分页、关键字、字段过滤和租户条件。
- 适合 CRUD 管理页快速接入。
- 不适合复杂聚合、跨上下文流程或需要强类型返回的核心业务接口。

新增服务可以复制这个模式，但必须保持过滤字段白名单，不允许前端自由传 SQL 字段。

### 9.4 DTO/VO

- 请求对象命名为 `XxxRequest`。
- 响应对象命名为 `XxxResponse`。
- 简单数据结构优先使用 Java record。
- 对外响应不要泄露密码、密钥、内部状态、审计字段或无关统计字段。

## 10. 测试规范

### 10.1 模块测试

每个服务至少有一个 API 集成测试：

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class YourServiceApiTest {
}
```

测试应覆盖：

- 启动上下文。
- Flyway 迁移能在 H2 MySQL 模式下执行。
- 核心查询接口返回 `SUCCESS`。
- 关键写接口能写入并能再次查询或断言数据库状态。
- 多租户服务必须覆盖租户隔离。
- 权限敏感接口必须覆盖无权限或错误角色场景。

### 10.2 本地验证命令

只改单个服务时，优先运行：

```bash
mvn -pl your-new-service test
```

改了父 POM、部署脚本、公共约定或跨服务交互时，运行：

```bash
mvn test
```

打包前不要用 `-DskipTests` 代替验证。`-DskipTests package` 只能在已经完成测试后用于生成部署包。

### 10.3 冒烟测试

部署脚本中的 `smoke-test.sh` 至少要验证：

- 网关首页或静态资源可访问。
- 新服务 Actuator health 为 `UP`。
- 如服务需要认证，验证登录或带 token 调用核心接口。
- 如服务有独立数据库，验证核心表可访问或核心数据已生成。

## 11. 部署接入规范

新增微服务后，必须同步修改 `deploy/energy-platform`：

1. `deploy.sh`
   - 复制新服务 jar 到发布目录。
2. `scripts/init-mysql.sh`
   - 创建新服务数据库、用户、密码环境变量和授权。
3. `scripts/start.sh`
   - 增加新服务容器。
   - 指定独立端口。
   - 传入数据库连接环境变量。
   - 如依赖其他服务，使用 Docker network 内部服务名访问。
4. `scripts/stop.sh`
   - 停止并清理新服务容器和可能残留的本地进程。
5. `nginx/default.conf`
   - 增加 `/api/<prefix>/` 和 `/api/<prefix>/actuator/` 转发。
6. `scripts/smoke-test.sh`
   - 增加新服务健康检查和关键接口检查。
7. `deploy/energy-platform/README.md`
   - 更新端口、数据库、运行组件说明。

端口分配必须避开已有端口：

- `8090`：`platform-basic-service`
- `8091`：`log-service`
- 新服务从 `8092` 起按需递增。

## 12. 新增微服务交付清单

新增服务提交前逐项检查：

- [ ] 根 `pom.xml` 已注册新模块。
- [ ] 子模块 POM 继承父工程，依赖最小化，配置了 repackage。
- [ ] 启动类包名位于服务根包。
- [ ] `application.yml` 使用服务专属端口、应用名、数据库环境变量和 Flyway。
- [ ] `application-test.yml` 使用 H2 MySQL 模式并启用 Flyway。
- [ ] 数据库脚本位于服务自己的 `db/migration`，表名前缀清晰。
- [ ] 租户域表和查询都带 `tenant_id`。
- [ ] API 前缀唯一，并已规划 Nginx 转发。
- [ ] 响应体、错误码、traceId 和异常处理与现有约定一致。
- [ ] Controller 薄，事务和业务规则在 Service。
- [ ] 写接口 request body 使用 `@Valid` 和明确 request record。
- [ ] 服务间调用使用 HTTP API，并配置超时。
- [ ] 关键业务动作写操作日志；日志失败策略已按业务要求明确。
- [ ] API 集成测试覆盖核心读写、权限和租户隔离。
- [ ] 本地已运行 `mvn -pl <module> test`。
- [ ] 部署脚本、Nginx、冒烟测试和部署 README 已同步更新。

## 13. 当前样例与适用场景

| 样例 | 适合参考的内容 |
| --- | --- |
| `platform-basic-service` | 完整 Spring Boot 服务结构、认证拦截、traceId、统一响应、异常处理、租户隔离、通用列表、写操作事务、操作日志、MockMvc 集成测试 |
| `log-service` | 最小服务骨架、独立数据库、日志表结构、日志接口测试契约 |
| `deploy/energy-platform` | 多服务 jar 发布、Docker 启停、MySQL 初始化、Nginx API 转发、发布后冒烟测试 |

如果新增服务只做单一领域能力，优先复制 `platform-basic-service` 的工程骨架和通用约定，但不要复制与平台基础信息强绑定的 catalog 代码。服务边界要先按业务能力划清，再决定是否引入通用列表 Registry、认证拦截和日志客户端。
