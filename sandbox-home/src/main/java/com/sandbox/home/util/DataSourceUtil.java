package com.sandbox.home.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 数据源构建工具
 * <p>
 * 根据配置 Map 动态创建 DataSource 实例，默认使用 Druid。用于多数据源场景下通过配置驱动数据源创建。
 * 构建失败返回 null，调用方需检查返回值。
 *
 * @author 0101
 * @since 2026-03-13
 */
@Slf4j
public class DataSourceUtil {

    private DataSourceUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 构建数据源
     *
     * @param dataSourceMap 配置 Map，需包含 driver、url、username、password，可选 type（默认 Druid）
     * @return DataSource 实例，失败返回 null
     */
    public static DataSource buildDataSource(Map<String, Object> dataSourceMap) {
        Object type = dataSourceMap.get("type");
        if (type == null) {
            type = "com.alibaba.druid.pool.DruidDataSource";
            log.debug("未指定数据源类型，使用默认 Druid 数据源");
        }

        try {
            Class<? extends DataSource> dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);

            String driverClassName = dataSourceMap.get("driver").toString();
            String url = dataSourceMap.get("url").toString();
            String username = dataSourceMap.get("username").toString();
            String password = dataSourceMap.get("password").toString();

            DataSourceBuilder<? extends DataSource> factory = DataSourceBuilder.create()
                    .url(url)
                    .username(username)
                    .password(password)
                    .type(dataSourceType)
                    .driverClassName(driverClassName);

            DataSource dataSource = factory.build();
            log.debug("数据源构建成功: type={}, url={}", type, url);
            return dataSource;

        } catch (Exception e) {
            log.error("构建数据源出错 - type: {}, 错误信息: {}", type, e.getMessage(), e);
            return null;
        }
    }
}