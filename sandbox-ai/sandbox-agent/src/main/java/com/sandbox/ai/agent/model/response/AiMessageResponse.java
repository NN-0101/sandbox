package com.sandbox.ai.agent.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 消息响应结果（用于非流式场景）
 *
 * @author 0101
 * @since 2026/04/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessageResponse {

    /**
     * 响应内容
     */
    private String content;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 响应时间
     */
    private LocalDateTime timestamp;

    public static AiMessageResponse success(String content, String conversationId) {
        return AiMessageResponse.builder()
                .content(content)
                .conversationId(conversationId)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static AiMessageResponse error(String errorMessage) {
        return AiMessageResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}