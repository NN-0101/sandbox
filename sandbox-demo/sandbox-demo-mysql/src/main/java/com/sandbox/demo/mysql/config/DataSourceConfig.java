package com.sandbox.demo.mysql.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.sandbox.demo.mysql.config.prop.JdbcBasicProp;
import com.sandbox.demo.mysql.config.prop.JdbcDsProp;
import com.sandbox.demo.mysql.util.DataSourceUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源配置 - 两主两从
 *
 * @author 0101
 * @since 2026-05-02
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sharding")
public class DataSourceConfig {

    /** 主库 0 */
    private JdbcDsProp ds0;
    /** 主库 1 */
    private JdbcDsProp ds1;
    /** ds0 的从库 */
    private JdbcDsProp ds0slave0;
    /** ds1 的从库 */
    private JdbcDsProp ds1slave0;

    /** 连接池基础属性（后续扩展用） */
    private JdbcBasicProp basic;

    /**
     * 主库数据源 ds0
     */
    @Bean
    public DataSource ds0DataSource() {
        return createDataSource(ds0, "ds0");
    }

    /**
     * 主库数据源 ds1
     */
    @Bean
    public DataSource ds1DataSource() {
        return createDataSource(ds1, "ds1");
    }

    /**
     * ds0 从库数据源
     */
    @Bean
    public DataSource ds0slave0DataSource() {
        return createDataSource(ds0slave0, "ds0slave0");
    }

    /**
     * ds1 从库数据源
     */
    @Bean
    public DataSource ds1slave0DataSource() {
        return createDataSource(ds1slave0, "ds1slave0");
    }

    /**
     * 统一创建 Druid 数据源
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
            log.error("创建数据源失败: {}", dsName);
            throw new RuntimeException("创建数据源失败: " + dsName);
        }

        ds.setName(dsName);

        // 基础连接池参数
        ds.setMaxActive(20);
        ds.setMinIdle(5);
        ds.setInitialSize(1);
        ds.setMaxWait(10000);
        ds.setValidationQuery("SELECT 1");
        ds.setValidationQueryTimeout(3);
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);

        long cost = System.currentTimeMillis() - startTime;
        log.info("数据源 {} 创建成功，耗时: {}ms", dsName, cost);
        return ds;
    }
}