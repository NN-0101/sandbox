package com.sandbox.ai.agent.core.impl;

import com.sandbox.ai.agent.annotations.AiAgentType;
import com.sandbox.ai.agent.core.AiAgent;
import com.sandbox.ai.agent.enumeration.AgentTypeEnum;
import com.sandbox.ai.agent.skill.Skill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 天气 Agent
 * <p>
 * 组合 WeatherSkill，通过 MCP 远程调用天气查询工具。
 * WeatherSkill 不包含本地工具，所有工具能力由 MCP 的 {@code toolCallbackProvider} 提供。
 * </p>
 *
 * @author 0101
 * @since 2026/05/07
 */
@Slf4j
@Component
@AiAgentType(AgentTypeEnum.MCP_WEATHER)
public class WeatherAgent implements AiAgent {

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;
    private final Skill weatherSkill;

    public WeatherAgent(ChatClient chatClient,
                        ToolCallbackProvider toolCallbackProvider,
                        @Qualifier("weatherSkill") Skill weatherSkill) {
        this.chatClient = chatClient;
        this.toolCallbackProvider = toolCallbackProvider;
        this.weatherSkill = weatherSkill;
    }

    @Override
    public Flux<String> execute(String conversationId, String message) {
        return chatClient
                .prompt(weatherSkill.getPrompt())
                .user(message)
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .toolCallbacks(toolCallbackProvider)
                .stream()
                .content()
                .doOnNext(chunk -> log.trace("WeatherAgent [{}]: {}", conversationId, chunk))
                .doOnError(error -> log.error("WeatherAgent [{}] 失败", conversationId, error))
                .doOnComplete(() -> log.info("WeatherAgent [{}] 完成", conversationId));
    }
}
