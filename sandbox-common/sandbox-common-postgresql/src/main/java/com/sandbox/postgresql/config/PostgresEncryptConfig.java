package com.sandbox.postgresql.config;

import jakarta.annotation.Resource;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * PostgreSQL 字段加密配置
 * <p>
 * 从 yml 读取需要加密的表和字段，自动生成 ShardingSphere 加密规则。
 * 使用 PostgreSQL 自定义 AES 算法，支持加解密和等值查询。
 *
 * @author 0101
 * @since 2026-05-06
 */
@Configuration
public class PostgresEncryptConfig {

    @Value("${sharding.aes.key}")
    private String aesKey;

    @Resource
    private PostgresDataSourceConfig dataSourceConfig;

    /**
     * 构建加密规则配置
     */
    @Bean
    public EncryptRuleConfiguration encryptRuleConfiguration() {
        // 注册加密算法
        Properties algoProps = new Properties();
        algoProps.setProperty("aes-key-value", aesKey);

        Map<String, AlgorithmConfiguration> encryptors = new HashMap<>();
        encryptors.put("custom_aes", new AlgorithmConfiguration("postgresql", algoProps));

        // 获取 yml 中的加密表配置
        PostgresDataSourceConfig.EncryptConfigItem encryptConfig = dataSourceConfig.getEncrypt();
        if (encryptConfig == null || encryptConfig.getTables() == null || encryptConfig.getTables().isEmpty()) {
            return new EncryptRuleConfiguration(Collections.emptyList(), encryptors);
        }

        List<EncryptTableRuleConfiguration> tables = new ArrayList<>();

        for (PostgresDataSourceConfig.EncryptTableItem tableItem : encryptConfig.getTables()) {
            List<EncryptColumnRuleConfiguration> columns = new ArrayList<>();

            for (String columnName : tableItem.getColumns()) {
                EncryptColumnItemRuleConfiguration cipherItem = new EncryptColumnItemRuleConfiguration(columnName, "custom_aes");
                EncryptColumnRuleConfiguration columnConfig = new EncryptColumnRuleConfiguration(columnName, cipherItem);
                columns.add(columnConfig);
            }

            tables.add(new EncryptTableRuleConfiguration(tableItem.getTableName(), columns));
        }

        return new EncryptRuleConfiguration(tables, encryptors);
    }
}