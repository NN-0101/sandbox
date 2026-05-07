package com.sandbox.ai.agent.core.impl;

import com.sandbox.ai.agent.annotations.AiAgentType;
import com.sandbox.ai.agent.core.AiAgent;
import com.sandbox.ai.agent.enumeration.AgentTypeTypeEnum;
import com.sandbox.ai.agent.tool.DBTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 对话 Agent
 * <p>
 * 一个通用的对话智能体，拥有基础对话能力和工具调用能力。
 * Skills: 自然语言对话、信息查询、天气查询
 * Tools: DBTool, WeatherTool (via MCP)
 *
 * @author 0101
 * @since 2026/03/18
 */
@Slf4j
@Component
@AiAgentType(AgentTypeTypeEnum.USER_TALK)
public class ChatAgent implements AiAgent {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Override
    public Flux<String> execute(String prompt, String conversationId, String message) {
        log.info("ChatAgent [{}] 开始处理用户消息", conversationId);

        return chatClient
                .prompt(prompt)
                .user(message)
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .tools(new DBTool())
                .toolCallbacks(toolCallbackProvider)
                .stream()
                .content()
                .doOnNext(chunk -> log.trace("ChatAgent [{}] 生成片段: {}", conversationId, chunk))
                .doOnError(error -> log.error("ChatAgent [{}] 处理失败", conversationId, error))
                .doOnComplete(() -> log.info("ChatAgent [{}] 处理完成", conversationId));
    }
}
