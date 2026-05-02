package com.sandbox.home.ai.memory;

import com.sandbox.home.service.AiMessageService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 基于数据库的聊天记忆实现
 * <p>
 * 将对话历史持久化到数据库，通过 AiMessageService 进行增删查操作。
 *
 * @author 0101
 * @since 2026/03/18
 */
public class DBChatMemory implements ChatMemory {

    private final AiMessageService aiMessageService;

    public DBChatMemory(AiMessageService aiMessageService) {
        this.aiMessageService = aiMessageService;
    }

    @Override
    public void add(@NonNull String conversationId, @NonNull List<Message> messages) {
        aiMessageService.add(conversationId, messages);
    }

    @NonNull
    @Override
    public List<Message> get(@NonNull String conversationId) {
        return aiMessageService.get(conversationId);
    }

    @Override
    public void clear(@NonNull String conversationId) {
        aiMessageService.clear(conversationId);
    }
}