package com.sandbox.ai.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 业务配置
 * <p>
 * 加载 "business-config" 前缀的配置，包括各 Skill 的系统提示词。
 * </p>
 *
 * @author 0101
 * @since 2026/03/18
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "business-config")
public class BusinessConfig {

    /**
     * 技能配置映射 — key 为技能名称（与 {@code Skill.getName()} 对齐），value 为技能配置
     */
    private Map<String, SkillConfig> skills;

    @Data
    public static class SkillConfig {

        /** 系统提示词 */
        private String prompt;
    }
}
