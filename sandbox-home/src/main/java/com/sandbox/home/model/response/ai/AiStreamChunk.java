package com.sandbox.home.model.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 流式消息片段
 *
 * @author 0101
 * @since 2026/04/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiStreamChunk {

    /**
     * 内容片段
     */
    private String content;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 是否完成
     */
    private boolean completed;

    /**
     * 片段序号
     */
    private int sequence;
}