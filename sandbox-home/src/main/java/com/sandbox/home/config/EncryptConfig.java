package com.sandbox.home.config;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ShardingSphere 加密规则配置
 * <p>
 * 根据 TableEncryptConfig 中的表-字段映射，动态构建加密规则，使用 AES 密钥对指定字段自动加解密。
 * 加密过程对业务代码透明，无需手动处理。
 *
 * @author 0101
 * @since 2026-03-13
 */
@Configuration
public class EncryptConfig {

    @Value("${aes.key}")
    private String aesKey;

    @Resource
    private TableEncryptConfig tableEncryptConfig;

    /**
     * 构建加密规则配置
     * <p>
     * 遍历 TableEncryptConfig 中所有表和字段，为每个字段创建加密列规则，
     * 统一使用 "custom_aes" 加密器和配置文件中的 AES 密钥。
     *
     * @return 加密规则配置，无加密表时返回 null
     */
    public EncryptRuleConfiguration buildEncryptRule() {
        Map<String, List<String>> tables = tableEncryptConfig.getTables();

        if (tables == null || tables.isEmpty()) {
            return null;
        }

        List<EncryptTableRuleConfiguration> encryptTableRuleConfigurations = new ArrayList<>(tables.size());

        for (Map.Entry<String, List<String>> encryptMap : tables.entrySet()) {
            String tableName = encryptMap.getKey();
            List<String> encryptColumns = encryptMap.getValue();

            List<EncryptColumnRuleConfiguration> encryptColumnConfigList = new ArrayList<>(encryptColumns.size());
            for (String encryptColumn : encryptColumns) {
                EncryptColumnRuleConfiguration encryptColumnConfig = new EncryptColumnRuleConfiguration(
                        encryptColumn, encryptColumn, "", "", "custom_aes");
                encryptColumnConfigList.add(encryptColumnConfig);
            }

            EncryptTableRuleConfiguration tableRuleConfiguration = new EncryptTableRuleConfiguration(
                    tableName, encryptColumnConfigList, true);
            encryptTableRuleConfigurations.add(tableRuleConfiguration);
        }

        Properties props = new Properties();
        props.setProperty("aes.key.value", aesKey);

        ShardingSphereAlgorithmConfiguration aesAlgorithm =
                new ShardingSphereAlgorithmConfiguration("mysql", props);

        return new EncryptRuleConfiguration(
                encryptTableRuleConfigurations,
                Collections.singletonMap("custom_aes", aesAlgorithm));
    }
}