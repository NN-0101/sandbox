package com.sandbox.mysql.config;

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
 * 字段加密配置（5.5.1 新版 API）
 */
@Configuration
public class EncryptConfig {

    @Value("${sharding.aes.key}")
    private String aesKey;

    @Resource
    private DataSourceConfig dataSourceConfig;

    /**
     * 加密规则：遍历 yml 中配置的表和字段，为每个字段生成加密规则
     */
    @Bean
    public EncryptRuleConfiguration encryptRuleConfiguration() {
        // 加密算法配置
        Properties algoProps = new Properties();
        algoProps.setProperty("aes-key-value", aesKey);

        Map<String, AlgorithmConfiguration> encryptors = new HashMap<>();
        encryptors.put("custom_aes", new AlgorithmConfiguration("CUSTOM_AES", algoProps));

        // 获取加密配置
        DataSourceConfig.EncryptConfigItem encryptConfig = dataSourceConfig.getEncrypt();
        if (encryptConfig == null || encryptConfig.getTables() == null || encryptConfig.getTables().isEmpty()) {
            // 没有配置加密，返回空规则
            return new EncryptRuleConfiguration(Collections.emptyList(), encryptors);
        }

        List<EncryptTableRuleConfiguration> tables = new ArrayList<>();

        for (DataSourceConfig.EncryptTableItem tableItem : encryptConfig.getTables()) {
            List<EncryptColumnRuleConfiguration> columns = new ArrayList<>();

            for (String columnName : tableItem.getColumns()) {
                // 每个字段生成一个密文列配置
                EncryptColumnItemRuleConfiguration cipherItem = new EncryptColumnItemRuleConfiguration(columnName, "custom_aes");

                EncryptColumnRuleConfiguration columnConfig = new EncryptColumnRuleConfiguration(columnName, cipherItem);

                columns.add(columnConfig);
            }

            EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration(tableItem.getTableName(), columns);

            tables.add(tableConfig);
        }

        return new EncryptRuleConfiguration(tables, encryptors);
    }
}