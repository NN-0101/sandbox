package com.sandbox.ai.agent.core;

import reactor.core.publisher.Flux;

/**
 * AI Agent 接口
 * <p>
 * 所有 AI Agent 必须实现此接口。每个 Agent 通过组合一个或多个 {@link com.sandbox.ai.agent.skill.Skill}
 * 来获得能力和工具：Agent 负责请求调度和会话管理，Skill 负责提供提示词和工具绑定。
 * </p>
 *
 * <h3>Agent + Skill 范式</h3>
 * <pre>{@code
 * // Agent 组合 Skill
 * @Component
 * public class ChatAgent implements AiAgent {
 *     private final Skill chatSkill;   // 对话能力
 *     private final Skill dbSkill;     // 数据库查询能力
 *
 *     public Flux<String> execute(String conversationId, String message) {
 *         String prompt = chatSkill.getPrompt() + "\n" + dbSkill.getPrompt();
 *         return chatClient.prompt(prompt)
 *                 .tools(collectTools(chatSkill, dbSkill))
 *                 .stream().content();
 *     }
 * }
 * }</pre>
 *
 * @author 0101
 * @since 2026/03/18
 */
public interface AiAgent {

    /**
     * 执行 Agent 任务并返回流式响应
     *
     * @param conversationId 会话 ID（用于记忆管理）
     * @param message        用户输入消息
     * @return AI 回复的响应式流
     */
    Flux<String> execute(String conversationId, String message);
}
