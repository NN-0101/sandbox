package com.sandbox.ai.agent.skill.impl;

import com.sandbox.ai.agent.config.BusinessConfig;
import com.sandbox.ai.agent.skill.Skill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据库技能
 * <p>
 * 提供数据库查询能力的系统提示词 + 本地 {@code @Tool} 工具（SELECT 查询）。
 * DBSkill 自身即为工具载体，{@link #getTools()} 返回自身引用，
 * Agent 通过 {@code ChatClient.tools(dbskill)} 注册即可。
 * </p>
 *
 * @author 0101
 * @since 2026/05/07
 */
@Slf4j
@Component("dbSkill")
public class DBSkill implements Skill {

    private final BusinessConfig businessConfig;

    public DBSkill(BusinessConfig businessConfig) {
        this.businessConfig = businessConfig;
    }

    @Override
    public String getName() {
        return "db";
    }

    @Override
    public String getPrompt() {
        return businessConfig.getSkills().get("db").getPrompt();
    }

    @Override
    public List<Object> getTools() {
        // DBSkill 自身包含 @Tool 方法，直接返回自身引用
        return List.of(this);
    }

    @Tool(description = "执行SQL查询并返回结果，仅支持SELECT查询")
    public String queryDatabase(String sql) {
        log.info("准备执行sql语句：{}", sql);
        // TODO: 接入实际数据源
        return null;
    }
}
