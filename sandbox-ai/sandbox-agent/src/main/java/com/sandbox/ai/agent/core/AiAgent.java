package com.sandbox.ai.agent.core;

import reactor.core.publisher.Flux;

/**
 * AI Agent 接口
 * <p>
 * 所有 AI Agent 必须实现此接口，每个 Agent 代表一个独立的智能体，
 * 拥有特定的技能（Skills）和工具（Tools），通过流式输出响应。
 *
 * @author 0101
 * @since 2026/03/18
 */
public interface AiAgent {

    /**
     * 执行 Agent 任务并返回流式响应
     *
     * @param prompt         系统提示词（定义 Agent 的角色和能力）
     * @param conversationId 会话 ID（用于记忆管理）
     * @param message        用户输入消息
     * @return AI 回复的响应式流
     */
    Flux<String> execute(String prompt, String conversationId, String message);
}
