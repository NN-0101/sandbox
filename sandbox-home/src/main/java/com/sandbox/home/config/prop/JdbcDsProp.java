package com.sandbox.home.config.prop;

import lombok.Data;

/**
 * JDBC 数据源连接配置属性
 * <p>
 * 封装单个数据源的连接信息（URL、用户名、密码、驱动类、连接池类型），
 * 每个数据源（主库/从库）对应一个独立的实例。
 *
 * @author 0101
 * @since 2026-03-13
 */
@Data
public class JdbcDsProp {

    /**
     * 数据库连接 URL
     */
    private String jdbcUrl;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码（生产环境建议加密存储或从配置中心获取）
     */
    private String password;

    /**
     * 连接池实现类全限定名（如 com.alibaba.druid.pool.DruidDataSource）
     */
    private String type;

    /**
     * JDBC 驱动类名（如 com.mysql.cj.jdbc.Driver）
     */
    private String driverClassName;
}