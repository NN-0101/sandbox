package com.sandbox.ai.agent.skill;

import java.util.Collections;
import java.util.List;

/**
 * Skill 接口 — 可复用的技能抽象
 * <p>
 * 每个 Skill 封装了一段系统提示词（prompt）和一组本地工具（{@code @Tool}），
 * 可被多个 Agent 组合使用，实现能力的复用和灵活编排。
 * </p>
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li><b>Skill = 能力单元</b>：负责提供"做什么"（prompt）和"用什么做"（tools）</li>
 *   <li><b>Agent = 编排单元</b>：负责组合 Skill、管理会话记忆、调度执行</li>
 *   <li><b>可组合</b>：一个 Agent 可以组合多个 Skill，一个 Skill 可被多个 Agent 复用</li>
 * </ul>
 *
 * <p>与 Spring AI 的 Agent + Skill 范式对齐。</p>
 *
 * @author 0101
 * @since 2026/05/07
 */
public interface Skill {

    /**
     * 技能名称（唯一标识）
     */
    String getName();

    /**
     * 系统提示词片段
     * <p>
     * Agent 会将所有组合 Skill 的 prompt 合并注入到系统消息中，
     * 使模型理解当前具备的能力边界。
     * </p>
     */
    String getPrompt();

    /**
     * 本地工具对象列表
     * <p>
     * 返回包含 {@code @Tool} 注解方法的对象。
     * Agent 通过 {@code ChatClient.tools()} 注册给模型。
     * 默认返回空列表（适用于纯对话或仅依赖 MCP 工具的 Skill）。
     * </p>
     */
    default List<Object> getTools() {
        return Collections.emptyList();
    }
}
