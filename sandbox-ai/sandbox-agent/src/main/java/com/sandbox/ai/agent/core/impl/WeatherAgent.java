package com.sandbox.ai.agent.core.impl;

import com.sandbox.ai.agent.annotations.AiAgentType;
import com.sandbox.ai.agent.core.AiAgent;
import com.sandbox.ai.agent.enumeration.AgentTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @description:
 * @author: 0101
 * @create: 2026/05/07
 */
@Slf4j
@Component
@AiAgentType(AgentTypeEnum.MCP_WEATHER)
public class WeatherAgent implements AiAgent {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;


    @Override
    public Flux<String> execute(String prompt, String conversationId, String message) {
        return chatClient
                .prompt(prompt)
                .user(message)
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .toolCallbacks(toolCallbackProvider)
                .stream()
                .content()
                .doOnNext(chunk -> log.trace("ChatAgent [{}] 生成片段: {}", conversationId, chunk))
                .doOnError(error -> log.error("ChatAgent [{}] 处理失败", conversationId, error))
                .doOnComplete(() -> log.info("ChatAgent [{}] 处理完成", conversationId));
    }
}
