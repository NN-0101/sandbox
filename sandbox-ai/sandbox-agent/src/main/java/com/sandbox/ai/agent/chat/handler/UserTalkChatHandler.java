package com.sandbox.ai.agent.chat.handler;

import com.sandbox.ai.agent.chat.BaseChatMessage;
import com.sandbox.ai.agent.chat.annotations.AiChatService;
import com.sandbox.ai.agent.enumeration.AiChatBizTypeEnum;
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
//                .user(message)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
//                .stream()
//                .content();
                .user(message)
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .stream()
                .content()
                .doOnNext(chunk -> log.trace("会话 [{}] 生成片段: {}", conversationId, chunk))
                .doOnError(error -> log.error("会话 [{}] 生成失败", conversationId, error));
    }
}