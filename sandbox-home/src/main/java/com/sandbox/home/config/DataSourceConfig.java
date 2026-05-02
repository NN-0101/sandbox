package com.sandbox.home.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import com.sandbox.home.config.prop.JdbcBasicProp;
import com.sandbox.home.config.prop.JdbcDsProp;
import com.sandbox.home.util.DataSourceUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源配置
 * <p>
 * 通过 "sharding" 前缀加载配置，创建 ds0/ds1（主库）和 ds0slave0/ds1slave0（从库）四个数据源。
 * 集成 Druid 监控（慢 SQL 阈值 5s），根据环境自动调整初始连接数。
 * <p>
 * 注意：生产环境建议关闭 removeAbandoned，testOnBorrow/Return 默认关闭以保证性能。
 *
 * @author 0101
 * @since 2026-03-13
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sharding")
public class DataSourceConfig {

    private JdbcDsProp ds0;
    private JdbcDsProp ds1;
    private JdbcDsProp ds0slave0;
    private JdbcDsProp ds1slave0;
    private JdbcBasicProp basic;

    @PostConstruct
    public void init() {
        log.info("DataSourceConfig initialized with basic properties: {}", basic);
    }

    public DataSource ds0() { return createDatasource(getDs0(), "ds0"); }
    public DataSource ds1() { return createDatasource(getDs1(), "ds1"); }
    public DataSource ds0slave0() { return createDatasource(getDs0slave0(), "ds0slave0"); }
    public DataSource ds1slave0() { return createDatasource(getDs1slave0(), "ds1slave0"); }

    private DataSource createDatasource(JdbcDsProp jdbcdsProp, String dsName) {
        long startTime = System.currentTimeMillis();

        Map<String, Object> dsMap = new HashMap<>();
        dsMap.put("type", jdbcdsProp.getType());
        dsMap.put("url", jdbcdsProp.getJdbcUrl());
        dsMap.put("driver", jdbcdsProp.getDriverClassName());
        dsMap.put("username", jdbcdsProp.getUsername());
        dsMap.put("password", jdbcdsProp.getPassword());

        DruidDataSource ds = (DruidDataSource) DataSourceUtil.buildDataSource(dsMap);
        if (ds == null) {
            log.error("创建数据源失败: {}", dsName);
            throw new RuntimeException("创建数据源失败: " + dsName);
        }

        ds.setName(dsName);
        initBasicProperties(ds, dsName);

        long cost = System.currentTimeMillis() - startTime;
        log.info("数据源 {} 创建成功，耗时: {}ms", dsName, cost);
        return ds;
    }

    private void initBasicProperties(DruidDataSource ds, String dsName) {
        try {
            ds.setMaxActive(Integer.parseInt(basic.getMaxActive()));
            ds.setMinIdle(Integer.parseInt(basic.getMinIdle()));

            int initialSize = Integer.parseInt(basic.getInitialSize());
            String env = System.getProperty("spring.profiles.active", "dev");
            if ("dev".equals(env) || "test".equals(env)) {
                initialSize = Math.min(initialSize, 1);
            }
            ds.setInitialSize(initialSize);
            ds.setMaxWait(Integer.parseInt(basic.getMaxWait()));

            boolean removeAbandoned = Boolean.parseBoolean(basic.getRemoveAbandoned());
            if (removeAbandoned) {
                log.warn("{}: removeAbandoned 已开启，生产环境不推荐", dsName);
                ds.setRemoveAbandonedTimeout(Integer.parseInt(basic.getRemoveAbandonedTimeout()));
                ds.setLogAbandoned(Boolean.parseBoolean(basic.getLogAbandoned()));
            } else {
                ds.setRemoveAbandoned(false);
            }

            ds.setTimeBetweenEvictionRunsMillis(Integer.parseInt(basic.getTimeBetweenEvictionRunsMillis()));
            ds.setMinEvictableIdleTimeMillis(Integer.parseInt(basic.getMinEvictableIdleTimeMillis()));
            ds.setValidationQuery(basic.getValidationQuery());
            ds.setValidationQueryTimeout(3);
            ds.setTestWhileIdle(Boolean.parseBoolean(basic.getTestWhileIdle()));

            boolean testOnBorrow = Boolean.parseBoolean(basic.getTestOnBorrow());
            boolean testOnReturn = Boolean.parseBoolean(basic.getTestOnReturn());
            if (testOnBorrow || testOnReturn) {
                log.warn("{}: testOnBorrow={}, testOnReturn={} 可能影响性能", dsName, testOnBorrow, testOnReturn);
            }
            ds.setTestOnBorrow(testOnBorrow);
            ds.setTestOnReturn(testOnReturn);

            ds.setProxyFilters(Lists.newArrayList(statFilter()));
            ds.setFilters("stat,wall,log4j2");
            ds.setPoolPreparedStatements(true);
            ds.setMaxPoolPreparedStatementPerConnectionSize(20);
            ds.setBreakAfterAcquireFailure(false);
            ds.setConnectionErrorRetryAttempts(3);
            ds.setDefaultAutoCommit(true);
            ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            log.debug("{} 初始化完成 - 最大连接数: {}, 最小空闲: {}, 初始连接数: {}",
                    dsName, ds.getMaxActive(), ds.getMinIdle(), ds.getInitialSize());
        } catch (Exception e) {
            log.error("初始化数据源 {} 失败: {}", dsName, e.getMessage(), e);
            throw new RuntimeException("数据源初始化失败: " + dsName, e);
        }
    }

    /** 创建 Druid 监控过滤器（慢 SQL 阈值 5000ms） */
    private Filter statFilter() {
        StatFilter filter = new StatFilter();
        filter.setSlowSqlMillis(5000);
        filter.setLogSlowSql(true);
        filter.setMergeSql(true);
        return filter;
    }

    /** 优雅关闭数据源 */
    public void closeDataSource(DataSource dataSource) {
        if (dataSource instanceof DruidDataSource) {
            try {
                ((DruidDataSource) dataSource).close();
                log.info("数据源关闭成功");
            } catch (Exception e) {
                log.error("关闭数据源时发生错误: {}", e.getMessage());
            }
        }
    }
}