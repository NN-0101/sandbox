package com.sandbox.postgresql.util;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Druid 数据源构建工具
 *
 * @author 0101
 * @since 2026-05-02
 */
@Slf4j
public class DataSourceUtil {

    /**
     * 根据属性 Map 构建 Druid 数据源
     *
     * @param dsMap 数据源属性（url, username, password, driver）
     * @return DruidDataSource 实例，失败返回 null
     */
    public static DataSource buildDataSource(Map<String, Object> dsMap) {
        try {
            DruidDataSource ds = new DruidDataSource();
            ds.setUrl((String) dsMap.get("url"));
            ds.setUsername((String) dsMap.get("username"));
            ds.setPassword((String) dsMap.get("password"));
            ds.setDriverClassName((String) dsMap.get("driver"));
            return ds;
        } catch (Exception e) {
            log.error("构建数据源失败", e);
            return null;
        }
    }
}