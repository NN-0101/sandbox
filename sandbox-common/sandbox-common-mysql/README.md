# sandbox-common-mysql

> MySQL 数据库配置模块 — 读写分离 + 分库分表 + 字段加密，开箱即用。

基于 **ShardingSphere 5.5.1** + **MyBatis-Plus** + **Druid**，提供完整的数据库中间件解决方案。YAML 零代码配置即可启用多数据源、分片与加密能力。

---

## 🎯 核心功能

### 1. 📡 读写分离

两主两从架构，写操作自动路由到主库，读操作路由到从库。

```
        ┌──────────┐       ┌──────────┐
        │   ds0    │       │   ds1    │
        │ (write)  │       │ (write)  │
        └────┬─────┘       └────┬─────┘
             │                  │
    ┌────────┴────────┐  ┌─────┴────────┐
    │   ds0slave0     │  │  ds1slave0   │
    │   (read)        │  │  (read)      │
    └─────────────────┘  └──────────────┘
```

- ShardingSphere 自动解析 SQL，**写走主库，读走从库**
- 支持按需扩展更多数据源

### 2. 🧩 分库分表

自定义取模分片算法，支持三种模式自由组合：

| 模式 | 说明 | 适用场景 |
| :--- | :--- | :--- |
| **只分库** | 按分片键路由到不同数据源 | 数据量大，多库分散压力 |
| **只分表** | 按分片键路由到同库不同表 | 单表数据量过大 |
| **分库 + 分表** | 两级分片，先分库再分表 | 海量数据场景 |

- 数字类型直接取模，字符串按 hashCode 取模
- 精确分片精确路由，范围分片广播所有节点

### 3. 🔐 字段加密

自定义 AES 加密算法，兼容 MySQL `AES_ENCRYPT/AES_DECRYPT` 密钥生成方式。

- 明文自动加密入库，查询时自动解密返回
- **支持等值查询**（WHERE encrypted_column = ? 可命中索引）
- 不支持模糊查询（LIKE）
- YAML 配置指定表名和字段名，自动生成加密规则

### 4. ❄️ 雪花 ID

MyBatis-Plus 雪花算法，workerId 根据服务器 IP 自动生成。

- workerId = IP 最后一段 % 32，datacenterId 从配置文件读取
- IP 获取失败时降级为随机数
- 容器/虚拟化环境建议手动配置 workerId

---

## 🛠️ 技术栈

| 组件 | 版本 | 用途 |
| :--- | :--- | :--- |
| ShardingSphere JDBC | 5.5.1 | 读写分离 + 分库分表 + 数据加密 |
| MyBatis-Plus | 3.5.x | ORM + 雪花 ID |
| Druid | 1.2.x | 连接池 |
| MySQL Connector | 8.x | JDBC 驱动 |

---

## 📦 依赖引入

```xml
<dependency>
    <groupId>com.sandbox.services</groupId>
    <artifactId>sandbox-common-mysql</artifactId>
    <version>${sandbox.version}</version>
</dependency>
```

模块已聚合以下依赖，无需重复引入：

- `mysql-connector-java`
- `mybatis-plus-boot-starter`
- `shardingsphere-jdbc`
- `druid-spring-boot-3-starter`

---

## ⚙️ 使用方法

### 完整 YAML 配置示例

```yaml
sharding:
  # ========== 数据源 0（主库 + 从库）==========
  ds0:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    jdbc-url: jdbc:mysql://192.168.1.101:3306/sandbox_db0?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_0
  ds0slave0:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    jdbc-url: jdbc:mysql://192.168.1.102:3306/sandbox_db0?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_0

  # ========== 数据源 1（主库 + 从库）==========
  ds1:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    jdbc-url: jdbc:mysql://192.168.1.103:3306/sandbox_db1?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_1
  ds1slave0:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    jdbc-url: jdbc:mysql://192.168.1.104:3306/sandbox_db1?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_1

  # ========== Druid 连接池基础配置 ==========
  basic:
    max-active: "20"
    min-idle: "5"
    initial-size: "1"
    max-wait: "10000"
    validation-query: SELECT 1
    test-while-idle: "true"
    test-on-borrow: "false"
    test-on-return: "false"

  # ========== AES 加密密钥 ==========
  aes-key: your-16-byte-secret-key

  # ========== 分库分表规则 ==========
  rules:
    # --- 规则1：只分表（单库多表）---
    - table-name: t_device
      database-sharding:
        count: 1              # 不分库
      table-sharding:
        sharding-column: device_sn
        algorithm-type: TABLE_MOD
        count: 4              # 分 4 张表：t_device_0 ~ t_device_3

    # --- 规则2：只分库（多库单表）---
    - table-name: t_log
      database-sharding:
        sharding-column: user_id
        algorithm-type: DB_MOD
        count: 2              # 分 2 个库
      table-sharding:
        count: 1              # 不分表

    # --- 规则3：分库 + 分表（海量数据）---
    - table-name: t_order
      database-sharding:
        sharding-column: user_id
        algorithm-type: DB_MOD
        count: 2              # 2 个库
      table-sharding:
        sharding-column: order_id
        algorithm-type: TABLE_MOD
        count: 4              # 每个库 4 张表

  # ========== 字段加密规则 ==========
  encrypt:
    tables:
      - table-name: t_user
        columns:
          - phone               # 手机号加密
          - email               # 邮箱加密
      - table-name: t_device
        columns:
          - device_sn           # 设备序列号加密
```

---

## 📖 使用举例

### 示例 1：只分表

**场景**：设备表 `t_device` 数据量大，按 `device_sn` 分 4 张表。

**YAML 配置**：

```yaml
sharding:
  rules:
    - table-name: t_device
      database-sharding:
        count: 1
      table-sharding:
        sharding-column: device_sn
        algorithm-type: TABLE_MOD
        count: 4
```

**数据库建表**（需手动执行）：

```sql
-- 4 张物理表结构完全相同
CREATE TABLE t_device_0 LIKE t_device;
CREATE TABLE t_device_1 LIKE t_device;
CREATE TABLE t_device_2 LIKE t_device;
CREATE TABLE t_device_3 LIKE t_device;
```

**效果**：

```
INSERT INTO t_device (device_sn, name) VALUES ('DEV-001', '传感器A')
  → device_sn.hashCode() = 123456, 123456 % 4 = 0
  → 路由到 t_device_0

INSERT INTO t_device (device_sn, name) VALUES ('DEV-002', '传感器B')
  → device_sn.hashCode() = 654321, 654321 % 4 = 1
  → 路由到 t_device_1

SELECT * FROM t_device WHERE device_sn = 'DEV-001'
  → 精确路由到 t_device_0

SELECT * FROM t_device WHERE create_time > '2026-01-01'
  → 范围查询，广播到 t_device_0 ~ t_device_3，合并结果
```

---

### 示例 2：只分库

**场景**：日志表 `t_log` 按 `user_id` 分 2 个库，分散写入压力。

**YAML 配置**：

```yaml
sharding:
  rules:
    - table-name: t_log
      database-sharding:
        sharding-column: user_id
        algorithm-type: DB_MOD
        count: 2
      table-sharding:
        count: 1
```

**效果**：

```
user_id = 1001 → 1001 % 2 = 1 → datasource1（路由到 ds1 库）
user_id = 1002 → 1002 % 2 = 0 → datasource0（路由到 ds0 库）
```

---

### 示例 3：分库 + 分表

**场景**：订单表 `t_order` 海量数据，按 `user_id` 分库、`order_id` 分表。

**YAML 配置**：

```yaml
sharding:
  rules:
    - table-name: t_order
      database-sharding:
        sharding-column: user_id
        algorithm-type: DB_MOD
        count: 2
      table-sharding:
        sharding-column: order_id
        algorithm-type: TABLE_MOD
        count: 4
```

**数据库准备**：

```
datasource0: t_order_0, t_order_1, t_order_2, t_order_3
datasource1: t_order_0, t_order_1, t_order_2, t_order_3
```

**效果**：

```
INSERT INTO t_order (user_id, order_id, amount) VALUES (1001, 5001, 99.00)
  → user_id=1001 % 2 = 1 → datasource1
  → order_id=5001 % 4 = 1 → t_order_1
  → 最终路由: datasource1.t_order_1

SELECT * FROM t_order WHERE user_id = 1001 AND order_id = 5001
  → 精确路由到 datasource1.t_order_1
```

---

### 示例 4：字段加密

**场景**：用户表的手机号和邮箱需要加密存储，同时支持按手机号等值查询。

**YAML 配置**：

```yaml
sharding:
  aes-key: MySecretKey123!!  # 16 字节密钥

  encrypt:
    tables:
      - table-name: t_user
        columns:
          - phone
          - email
```

**使用效果**：

```java
// 插入 — 自动加密
User user = new User();
user.setPhone("13800138000");       // 明文
user.setEmail("user@example.com");  // 明文
userMapper.insert(user);
// → 数据库实际存储：phone = "a1b2c3d4..."（十六进制密文）

// 等值查询 — 自动加密查询条件
User result = userMapper.selectOne(
    new LambdaQueryWrapper<User>().eq(User::getPhone, "13800138000")
);
// → SQL: SELECT * FROM t_user WHERE phone = 'a1b2c3d4...'
// → 返回时自动解密，result.getPhone() = "13800138000"

// 列表查询 — 全部自动解密
List<User> users = userMapper.selectList(null);
// → 每条记录的 phone、email 自动解密返回明文
```

---

## 🏗️ 包结构

```
com.sandbox.mysql
├── algorithm          # 自定义分片算法
│   ├── CustomAesEncryptAlgorithm      # AES 加密算法（兼容 MySQL）
│   ├── DatabaseShardingAlgorithm      # 分库算法（取模）
│   └── TableShardingAlgorithm         # 分表算法（取模）
├── config             # 自动配置
│   ├── DataSourceConfig               # 多数据源配置（从 yml 读取）
│   ├── EncryptConfig                  # 加密规则配置
│   ├── MybatisPlusConfig              # 雪花 ID 生成器
│   └── ShardingSphereConfig           # ShardingSphere 整合配置
├── prop               # 配置属性
│   ├── JdbcBasicProp                  # Druid 连接池属性
│   └── JdbcDsProp                     # JDBC 数据源属性
└── util               # 工具类
    └── DataSourceUtil                 # Druid 数据源构建工具
```

---

## ⚠️ 注意事项

1. **分片键选择**：尽量选择区分度高、不易变更的字段作为分片键，避免数据倾斜
2. **物理表建表**：分表模式下需手动创建所有物理表（如 `t_order_0` ~ `t_order_3`），结构必须一致
3. **AES 密钥**：生产环境密钥不要硬编码在配置文件中，建议通过环境变量或配置中心注入
4. **范围查询**：范围分片会广播到所有节点，数据量较大时注意性能
5. **容器环境**：雪花 ID 的 workerId 依赖 IP 取模，容器 IP 可能重复，建议生产环境手动指定
6. **testOnBorrow/Return**：默认关闭，开启会显著降低连接获取性能
