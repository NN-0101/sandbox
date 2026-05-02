package com.sandbox.home.ai.chat;

import reactor.core.publisher.Flux;

/**
 * AI 聊天消息处理接口
 * <p>
 * 所有 AI 聊天服务必须实现此接口，返回 Flux 支持流式输出。
 *
 * @author 0101
 * @since 2026/03/18
 */
public interface BaseChatMessage {

    /**
     * 发送消息并返回 AI 回复流
     *
     * @param prompt         提示词
     * @param conversationId 会话 ID
     * @param message        用户消息
     * @return AI 回复的响应式流
     */
    Flux<String> sendMessage(String prompt, String conversationId, String message);
}