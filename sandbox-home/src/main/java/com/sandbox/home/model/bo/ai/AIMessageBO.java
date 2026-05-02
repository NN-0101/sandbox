package com.sandbox.home.model.bo.ai;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

/**
 * @description:
 * @author: xp
 * @create: 2025/6/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIMessageBO {

    private String conversationId;

    private String messageType;

    private String content;

    private String metadata;

    public AIMessageBO(String conversationId, Message message) {
        this.conversationId = conversationId;
        this.messageType = message.getMessageType().getValue();
        this.content = message.getText();
        this.metadata = JSONObject.toJSONString(message.getMetadata());
    }
}
