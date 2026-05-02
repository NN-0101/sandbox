package com.sandbox.demo.mysql.config.prop;

import lombok.Data;

/**
 * JDBC 数据源连接配置属性
 *
 * @author 0101
 * @since 2026-05-02
 */
@Data
public class JdbcDsProp {

    /** 连接池实现类全限定名 */
    private String type;

    /** JDBC 驱动类名 */
    private String driverClassName;

    /** 数据库连接 URL */
    private String jdbcUrl;

    /** 数据库用户名 */
    private String username;

    /** 数据库密码 */
    private String password;
}