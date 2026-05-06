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
 * 用户闲聊 AI 服务
 * <p>
 * 不附加系统提示词或工具，仅基于对话历史进行普通对话，支持多轮记忆，返回流式输出。
 *
 * @author 0101
 * @since 2026/03/18
 */
@Slf4j
@Service
@AiChatService(AiChatBizTypeEnum.USER_TALK)
public class UserTalkChatHandler implements BaseChatMessage {

    @Autowired
    private ChatClient chatClient;

    @Override
    public Flux<String> sendMessage(String prompt, String conversationId, String message) {
        return chatClient
                .prompt("聊天")
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}