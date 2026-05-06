# sandbox-common-postgresql

> PostgreSQL 数据库基础设施封装，ShardingSphere 5.5.1 读写分离 + 分库分表 + 字段加密一站式配置

基于 Spring Boot 3.x 的 PostgreSQL 公共组件，整合 Druid 连接池、MyBatis-Plus、ShardingSphere 5.5.1，提供开箱即用的多数据源、读写分离、分库分表、字段加密以及一组 PostgreSQL 特有的 MyBatis TypeHandler。

---

## 🎯 核心功能

### 1. 多数据源 + 读写分离

- 默认两主两从架构（`ds0` / `ds1` + `ds0slave0` / `ds1slave0`），可按需扩展
- ShardingSphere 读写分离规则，写走主库、读走从库
- Druid 连接池统一管理，内置连接泄漏检测、空闲回收等机制

### 2. 分库分表

- **自定义分库算法** `DatabaseShardingAlgorithm`（`DB_MOD`）：按分片键取模路由到指定数据源
- **自定义分表算法** `TableShardingAlgorithm`（`TABLE_MOD`）：按分片键取模路由到物理表
- 支持数字 / 字符串分片键，字符串按 `hashCode & Integer.MAX_VALUE` 取模
- 通过 yml 配置即可定义多套规则：分库+分表、只分库、只分表，灵活组合

### 3. 字段加密

- **自定义 AES 加密算法** `PostgresAesEncryptAlgorithm`：与 PostgreSQL `pgcrypto` 模块兼容的 AES/ECB/PKCS5Padding 加密方式，采用 SHA-256 密钥派生
- ShardingSphere 加密规则自动装配，在 yml 中声明表和字段即可生效
- 支持等值查询（加密后的相同明文产生相同密文）

### 4. MyBatis TypeHandler 套件

| TypeHandler | 目标类型 | 说明 |
| :--- | :--- | :--- |
| `JsonTypeHandler<T>` | JSON / JSONB | 泛型 JSON 处理器，支持自定义对象序列化；内附 `RawTypeHandler` 处理动态结构 |
| `EnumTypeHandler<E>` | 自定义枚举 | PostgreSQL 枚举类型 ↔ Java Enum 双向映射 |
| `PostgresArrayTypeHandler` | TEXT[] | PostgreSQL 数组类型 ↔ `List<String>` 双向映射 |

### 5. MyBatis-Plus 雪花 ID

- 基于 `DefaultIdentifierGenerator` 的分布式 ID 生成
- `workerId` 根据服务器 IP 最后一段自动取模 32 生成，`datacenterId` 从配置读取
- IP 获取失败时降级为随机值

---

## 📁 模块结构

```
sandbox-common-postgresql/
└── src/main/java/com/sandbox/postgresql/
    ├── algorithm/
    │   ├── DatabaseShardingAlgorithm.java      # 分库算法 (DB_MOD)
    │   ├── TableShardingAlgorithm.java         # 分表算法 (TABLE_MOD)
    │   └── PostgresAesEncryptAlgorithm.java    # AES 加密算法
    ├── config/
    │   ├── PostgresDataSourceConfig.java       # 多数据源配置（yml → DruidDataSource）
    │   ├── PostgresShardingSphereConfig.java   # ShardingSphere 读写分离 + 分片规则
    │   ├── PostgresEncryptConfig.java          # 字段加密规则
    │   └── MybatisPlusConfig.java              # 雪花 ID + TypeHandler 注册
    ├── handler/
    │   ├── JsonTypeHandler.java                # JSON/JSONB 类型处理器
    │   ├── EnumTypeHandler.java                # 枚举类型处理器
    │   ├── PostgresArrayTypeHandler.java       # TEXT[] 数组类型处理器
    │   └── demo/User.java                      # TypeHandler 使用示例实体
    ├── prop/
    │   ├── JdbcDsProp.java                     # 数据源连接属性
    │   └── JdbcBasicProp.java                  # Druid 连接池基础属性
    └── util/
        └── DataSourceUtil.java                 # Druid 数据源构建工具
```

---

## 🔧 依赖

| 依赖 | 用途 |
| :--- | :--- |
| `org.postgresql:postgresql` | PostgreSQL JDBC 驱动 |
| `com.baomidou:mybatis-plus-boot-starter` | MyBatis-Plus ORM |
| `org.apache.shardingsphere:shardingsphere-jdbc` | ShardingSphere 5.5.1 内核 |
| `com.alibaba:druid-spring-boot-3-starter` | Druid 连接池 |
| `com.fasterxml.jackson.core:jackson-databind` | JSONTypeHandler 序列化 |

---

## ⚙️ 快速上手

参考 `sandbox-demo-postgresql` 模块的完整示例。

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.sandbox.services</groupId>
    <artifactId>sandbox-common-postgresql</artifactId>
    <version>${revision}</version>
</dependency>
```

### 2. 配置 application-dev.yml

```yaml
sharding:
  # ---------- 数据源 ----------
  ds0:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: org.postgresql.Driver
    jdbcUrl: jdbc:postgresql://localhost:5433/postgres_demo
    username: root
    password: 123456

  ds1:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: org.postgresql.Driver
    jdbcUrl: jdbc:postgresql://localhost:5435/postgres_demo
    username: root
    password: 123456

  ds0slave0:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: org.postgresql.Driver
    jdbcUrl: jdbc:postgresql://localhost:5434/postgres_demo
    username: root
    password: 123456

  ds1slave0:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: org.postgresql.Driver
    jdbcUrl: jdbc:postgresql://localhost:5436/postgres_demo
    username: root
    password: 123456

  # ---------- 连接池 ----------
  basic:
    max-active: 20
    min-idle: 5
    initial-size: 1
    max-wait: 10000
    validation-query: SELECT 1
    test-while-idle: true
    test-on-borrow: false
    test-on-return: false

  # ---------- AES 加密密钥 ----------
  aes:
    key: 7F2A8C3B9D4E5F1A6B2C3D4E5F6A7B8C

  # ---------- 字段加密 ----------
  encrypt:
    tables:
      - table-name: t_user
        columns:
          - phone

  # ---------- 分库分表规则 ----------
  rules:
    - table-name: t_user
      database-sharding:
        sharding-column: phone              # 按 phone 分库
        algorithm-type: DB_MOD
        count: 2                            # 2 个库
      table-sharding:
        sharding-column: id                 # 按 id 分表
        algorithm-type: TABLE_MOD
        count: 2                            # 每库 2 张表
```

### 3. 注册 SPI 服务

在 `src/main/resources/META-INF/services/` 下创建两个文件：

**`org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`**
```
com.sandbox.postgresql.algorithm.DatabaseShardingAlgorithm
com.sandbox.postgresql.algorithm.TableShardingAlgorithm
```

**`org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm`**
```
com.sandbox.postgresql.algorithm.PostgresAesEncryptAlgorithm
```

### 4. 使用 TypeHandler

```java
@Data
@TableName(value = "t_user", autoResultMap = true)
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;          // JSONB

    @TableField(typeHandler = PostgresArrayTypeHandler.class)
    private List<String> tags;                     // TEXT[]

    @TableField(typeHandler = EnumTypeHandler.class)
    private UserStatus status;                     // 自定义枚举
}
```

详细示例见 `sandbox-common-postgresql/src/main/java/com/sandbox/postgresql/handler/demo/User.java`。

---

## 🧪 测试验证

`sandbox-demo-postgresql` 模块提供了完整的集成测试 `ReadWriteSplittingTest`，覆盖：

- 建表（逻辑表自动展开为物理表）
- 批量插入 20 条数据（观测分库分表路由）
- 精确查询（双分片键 → 单分片）
- 按分库键查询 / 按分表键查询 / 全量查询
- 更新 & 删除（精确路由）
- 物理表数据分布验证

```bash
cd sandbox-demo/sandbox-demo-postgresql
mvn test -Dtest=ReadWriteSplittingTest
```

---

## 📝 设计要点

- **分库分表算法共用逻辑**：`DatabaseShardingAlgorithm` 和 `TableShardingAlgorithm` 共享相同的取模策略，区别仅在路由目标（数据源名 vs 物理表名）
- **范围查询**：当前分片算法的范围分片返回所有可用目标（全广播），生产环境建议配合精确分片键查询
- **加密算法**：使用 ECB 模式保证等值查询，密钥通过 SHA-256 派生为 128 位 AES 密钥，与 PostgreSQL `pgcrypto` 的 `encrypt`/`decrypt` 函数兼容
- **读写分离负载均衡**：使用 ShardingSphere 默认的轮询策略
- **SPI 注册**：自定义算法需要同时出现在 jar 的 `META-INF/services` 和 ShardingSphere 的算法注册表中才能生效
