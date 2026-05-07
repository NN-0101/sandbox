package com.sandbox.ai.agent.skill.impl;

import com.sandbox.ai.agent.config.BusinessConfig;
import com.sandbox.ai.agent.skill.Skill;
import org.springframework.stereotype.Component;

/**
 * 天气技能
 * <p>
 * 提供天气查询指引的系统提示词。天气查询工具通过 MCP 远程调用
 * {@code sandbox-mcp-server} 暴露的 WeatherTool，不在本地实现。
 * </p>
 *
 * @author 0101
 * @since 2026/05/07
 */
@Component("weatherSkill")
public class WeatherSkill implements Skill {

    private final BusinessConfig businessConfig;

    public WeatherSkill(BusinessConfig businessConfig) {
        this.businessConfig = businessConfig;
    }

    @Override
    public String getName() {
        return "weather";
    }

    @Override
    public String getPrompt() {
        return businessConfig.getSkills().get("weather").getPrompt();
    }

    // 无本地工具 — 天气查询完全依赖 MCP 远程调用
}
