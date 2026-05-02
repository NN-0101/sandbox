package com.sandbox.home.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 表字段加密配置
 * <p>
 * 从 "aes" 前缀加载配置，维护需要加密的表名与字段列表的映射（Map&lt;表名, List&lt;字段名&gt;&gt;）。
 * <p>
 * 注意：表名和字段名必须与数据库完全一致，加密字段需足够长度存储加密后的数据。
 *
 * @author 0101
 * @since 2026-03-13
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aes")
public class TableEncryptConfig {

    /**
     * 表-加密字段映射，Key 为表名，Value 为需加密的字段列表
     */
    private Map<String, List<String>> tables;
}