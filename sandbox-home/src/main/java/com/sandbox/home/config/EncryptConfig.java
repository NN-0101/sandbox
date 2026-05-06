package com.sandbox.home.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 字段加密配置
 * <p>
 * 从 yml 读取需要加密的表和字段，自动生成 ShardingSphere 加密规则。
 * 使用自定义 AES 算法，支持加解密和等值查询。
 *
 * @author 0101
 * @since 2026-05-06
 */
@Slf4j
@Configuration
public class EncryptConfig {

    @Value("${sharding.aes.key}")
    private String aesKey;

    @Resource
    private DataSourceConfig dataSourceConfig;

    /**
     * 构建加密规则配置
     * <p>
     * 遍历 yml 中配置的表和字段，为每个字段生成密文列规则，
     * 未配置加密时返回空表规则。
     */
    @Bean
    public EncryptRuleConfiguration encryptRuleConfiguration() {
        // 注册加密算法
        Properties algoProps = new Properties();
        algoProps.setProperty("aes-key-value", aesKey);

        Map<String, AlgorithmConfiguration> encryptors = new HashMap<>();
        encryptors.put("custom_aes", new AlgorithmConfiguration("CUSTOM_AES", algoProps));

        // 获取 yml 中的加密表配置
        DataSourceConfig.EncryptConfigItem encryptConfig = dataSourceConfig.getEncrypt();
        if (encryptConfig == null || encryptConfig.getTables() == null || encryptConfig.getTables().isEmpty()) {
            log.info("未配置字段加密");
            return new EncryptRuleConfiguration(Collections.emptyList(), encryptors);
        }

        List<EncryptTableRuleConfiguration> tables = new ArrayList<>();

        for (DataSourceConfig.EncryptTableItem tableItem : encryptConfig.getTables()) {
            List<EncryptColumnRuleConfiguration> columns = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();

            for (String columnName : tableItem.getColumns()) {
                // 每个字段绑定 custom_aes 加密器
                EncryptColumnItemRuleConfiguration cipherItem = new EncryptColumnItemRuleConfiguration(columnName, "custom_aes");
                EncryptColumnRuleConfiguration columnConfig = new EncryptColumnRuleConfiguration(columnName, cipherItem);
                columns.add(columnConfig);

                columnNames.add(columnName);
            }

            tables.add(new EncryptTableRuleConfiguration(tableItem.getTableName(), columns));
            log.info("表 [{}] 加密字段: {}", tableItem.getTableName(), columnNames);
        }

        return new EncryptRuleConfiguration(tables, encryptors);
    }
}