package com.sandbox.home.ai.chat.handler;

import com.sandbox.home.ai.annotations.AiChatService;
import com.sandbox.home.ai.chat.BaseChatMessage;
import com.sandbox.home.ai.enumeration.AiChatBizTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 设备对话 AI 服务
 * <p>
 * 不附加系统提示词或工具，仅进行普通对话，返回流式输出。
 *
 * @author 0101
 * @since 2026/03/18
 */
@Slf4j
@Service
@AiChatService(AiChatBizTypeEnum.DEVICE_DIALOGUE)
public class DeviceTalkChatHandler implements BaseChatMessage {

    @Autowired
    private ChatClient chatClient;

    @Override
    public Flux<String> sendMessage(String prompt, String conversationId, String message) {
        return chatClient
                .prompt(prompt)
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}