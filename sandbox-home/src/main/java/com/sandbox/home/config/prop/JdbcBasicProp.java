package com.sandbox.home.config.prop;

import lombok.Data;

/**
 * Druid 连接池基础配置属性
 * <p>
 * 封装连接池大小、超时、连接测试、泄漏检测等核心参数，所有数据源共享此配置。
 * 属性使用 String 类型便于从配置文件读取，使用时再进行类型转换。
 * <p>
 * 注意：removeAbandoned 生产环境慎用，testOnBorrow/Return 会显著降低性能。
 *
 * @author 0101
 * @since 2026-03-13
 */
@Data
public class JdbcBasicProp {

    /**
     * 最大活跃连接数
     */
    private String maxActive;

    /**
     * 最小空闲连接数
     */
    private String minIdle;

    /**
     * 初始连接数
     */
    private String initialSize;

    /**
     * 是否记录连接泄漏日志（需配合 removeAbandoned）
     */
    private String logAbandoned;

    /**
     * 是否回收泄露连接（生产环境不推荐）
     */
    private String removeAbandoned;

    /**
     * 连接泄漏超时时间（秒）
     */
    private String removeAbandonedTimeout;

    /**
     * 获取连接的最大等待时间（毫秒），-1 表示无限等待
     */
    private String maxWait;

    /**
     * 空闲连接检查间隔（毫秒）
     */
    private String timeBetweenEvictionRunsMillis;

    /**
     * 每次检查的空闲连接数量，-1 表示全部
     */
    private String numTestsPerEvictionRun;

    /**
     * 连接最小空闲时间（毫秒），超过可能被回收
     */
    private String minEvictableIdleTimeMillis;

    /**
     * 连接测试 SQL（如 SELECT 1）
     */
    private String validationQuery;

    /**
     * 是否在空闲时测试连接（建议开启）
     */
    private String testWhileIdle;

    /**
     * 是否在获取连接时测试（性能影响大，建议关闭）
     */
    private String testOnBorrow;

    /**
     * 是否在归还连接时测试（性能影响大，建议关闭）
     */
    private String testOnReturn;
}