package com.sandbox.demo.mysql.config;

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

    @Value("${aes.key}")
    private String aesKey;

    @Bean
    public EncryptRuleConfiguration encryptRuleConfiguration() {
        // 密文列配置：列名 + 加密器名
        EncryptColumnItemRuleConfiguration cipherItem =
                new EncryptColumnItemRuleConfiguration("phone", "custom_aes");

        // 加密列规则：逻辑列名 + 密文配置
        EncryptColumnRuleConfiguration columnConfig =
                new EncryptColumnRuleConfiguration("phone", cipherItem);

        // 加密表规则
        EncryptTableRuleConfiguration tableConfig =
                new EncryptTableRuleConfiguration("t_user",
                        Collections.singletonList(columnConfig));

        // 加密算法配置
        Properties algoProps = new Properties();
        algoProps.setProperty("aes-key-value", aesKey);

        Map<String, AlgorithmConfiguration> encryptors = new HashMap<>();
        encryptors.put("custom_aes",
                new AlgorithmConfiguration("CUSTOM_AES", algoProps));

        return new EncryptRuleConfiguration(
                Collections.singletonList(tableConfig),
                encryptors
        );
    }
}