package com.sandbox.ai.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 业务配置
 * <p>
 * 加载 "business-config" 前缀的配置，包括提示词映射和免认证接口列表。
 *
 * @author 0101
 * @since 2026/03/18
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "business-config")
public class BusinessConfig {

    /**
     * 提示词映射，key 为 AiChatTypeEnum 枚举值，value 为系统提示词
     */
    private Map<String, String> prompts;
}