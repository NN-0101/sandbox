package com.sandbox.ai.agent.model.request;

import com.sandbox.ai.agent.enumeration.AgentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 消息发送请求参数
 *
 * @author 0101
 * @since 2026/04/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessageRequest {

    /**
     * 聊天业务类型
     */
    private AgentTypeEnum chatType;

    /**
     * 身份类型（如：USER、DEVICE、ADMIN等）
     */
    private String identityType;

    /**
     * 用户标识（如：用户ID、设备ID等）
     */
    private String identifier;

    /**
     * 会话ID（用于多轮对话记忆，可选）
     */
    private String conversationId;

    /**
     * 用户消息内容
     */
    private String message;

    /**
     * 创建请求的静态工厂方法
     */
    public static AiMessageRequest of(AgentTypeEnum chatType, String message) {
        return AiMessageRequest.builder()
                .chatType(chatType)
                .message(message)
                .build();
    }

    /**
     * 创建带会话的请求
     */
    public static AiMessageRequest of(AgentTypeEnum chatType, String conversationId, String message) {
        return AiMessageRequest.builder()
                .chatType(chatType)
                .conversationId(conversationId)
                .message(message)
                .build();
    }

    /**
     * 校验必要参数
     */
    public void validate() {
        if (chatType == null) {
            throw new IllegalArgumentException("聊天类型不能为空");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
    }
}