package com.sandbox.ai.agent.skill.impl;

import com.sandbox.ai.agent.config.BusinessConfig;
import com.sandbox.ai.agent.skill.Skill;
import org.springframework.stereotype.Component;

/**
 * 对话技能
 * <p>
 * 提供通用对话能力的系统提示词，不绑定任何本地工具。
 * 适合作为基础 Skill 被多个 Agent 组合使用。
 * </p>
 *
 * @author 0101
 * @since 2026/05/07
 */
@Component("chatSkill")
public class ChatSkill implements Skill {

    private final BusinessConfig businessConfig;

    public ChatSkill(BusinessConfig businessConfig) {
        this.businessConfig = businessConfig;
    }

    @Override
    public String getName() {
        return "chat";
    }

    @Override
    public String getPrompt() {
        return businessConfig.getSkills().get("chat").getPrompt();
    }
}
