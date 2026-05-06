package com.sandbox.postgresql.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.sandbox.postgresql.prop.JdbcBasicProp;
import com.sandbox.postgresql.prop.JdbcDsProp;
import com.sandbox.postgresql.util.DataSourceUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.*;

/**
 * PostgreSQL 数据源配置
 * <p>
 * 从 yml 读取多数据源、加密、分库分表规则配置。
 * 默认两主两从架构，支持按需扩展。
 *
 * @author 0101
 * @since 2026-05-06
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sharding")
public class PostgresDataSourceConfig {

    /**
     * 主库 0
     */
    private JdbcDsProp ds0;
    /**
     * 主库 1
     */
    private JdbcDsProp ds1;
    /**
     * ds0 的从库
     */
    private JdbcDsProp ds0slave0;
    /**
     * ds1 的从库
     */
    private JdbcDsProp ds1slave0;

    /**
     * 连接池基础属性
     */
    private JdbcBasicProp basic;

    /**
     * 分库分表规则列表
     */
    private List<ShardingRuleItem> rules = new ArrayList<>();

    /**
     * 加密配置
     */
    private EncryptConfigItem encrypt;

    // ==================== 数据源 Bean ====================

    @Bean
    public DataSource ds0DataSource() {
        return createDataSource(ds0, "ds0");
    }

    @Bean
    public DataSource ds1DataSource() {
        return createDataSource(ds1, "ds1");
    }

    @Bean
    public DataSource ds0slave0DataSource() {
        return createDataSource(ds0slave0, "ds0slave0");
    }

    @Bean
    public DataSource ds1slave0DataSource() {
        return createDataSource(ds1slave0, "ds1slave0");
    }

    /**
     * 创建 Druid 数据源并配置连接池参数
     */
    private DataSource createDataSource(JdbcDsProp prop, String dsName) {
        long startTime = System.currentTimeMillis();

        Map<String, Object> dsMap = new HashMap<>();
        dsMap.put("type", prop.getType());
        dsMap.put("url", prop.getJdbcUrl());
        dsMap.put("driver", prop.getDriverClassName());
        dsMap.put("username", prop.getUsername());
        dsMap.put("password", prop.getPassword());

        DruidDataSource ds = (DruidDataSource) DataSourceUtil.buildDataSource(dsMap);
        if (ds == null) {
            log.error("创建PostgreSQL数据源失败: {}", dsName);
            throw new RuntimeException("创建PostgreSQL数据源失败: " + dsName);
        }

        // 连接池基础配置
        ds.setName(dsName);
        ds.setMaxActive(20);
        ds.setMinIdle(5);
        ds.setInitialSize(1);
        ds.setMaxWait(10000);
        // PostgreSQL验证查询
        ds.setValidationQuery("SELECT 1");
        ds.setValidationQueryTimeout(3);
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);

        // PostgreSQL特有配置
        ds.addConnectionProperty("ApplicationName", "sandbox-pg-" + dsName);
        ds.addConnectionProperty("connectTimeout", "10");
        ds.addConnectionProperty("socketTimeout", "30");

        long cost = System.currentTimeMillis() - startTime;
        log.info("PostgreSQL数据源 {} 创建成功，耗时: {}ms", dsName, cost);
        return ds;
    }

    // ==================== 内部配置类 ====================

    /**
     * 单条分库分表规则
     */
    @Getter
    @Setter
    public static class ShardingRuleItem {
        /**
         * 逻辑表名
         */
        private String tableName;
        /**
         * 分库配置
         */
        private ShardingItem databaseSharding;
        /**
         * 分表配置
         */
        private ShardingItem tableSharding;
    }

    /**
     * 单个分片项（库或表）
     */
    @Getter
    @Setter
    public static class ShardingItem {
        /**
         * 分片字段
         */
        private String shardingColumn = "";
        /**
         * 算法类型
         */
        private String algorithmType = "";
        /**
         * 分片数量（1 表示不分片）
         */
        private int count = 1;
    }

    /**
     * 加密配置
     */
    @Getter
    @Setter
    public static class EncryptConfigItem {
        /**
         * 需要加密的表列表
         */
        private List<EncryptTableItem> tables = new ArrayList<>();
    }

    /**
     * 加密表配置
     */
    @Getter
    @Setter
    public static class EncryptTableItem {
        /**
         * 表名
         */
        private String tableName;
        /**
         * 需要加密的字段列表
         */
        private List<String> columns = new ArrayList<>();
    }
}