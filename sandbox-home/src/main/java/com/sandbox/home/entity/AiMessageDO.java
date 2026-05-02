package com.sandbox.home.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sandbox.home.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * @description: AI聊天消息表(AiMessage)表数据库实体
 * @author: 0101
 * @create: 2026-04-30 14:26:30
 */
@Data
@TableName("t_ai_message")
@EqualsAndHashCode(callSuper = true)
public class AiMessageDO extends BaseModel<AiMessageDO> {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 会话id
     */
    private String conversationId;
     
    /**
     * 消息类型
     */
    private String messageType;
     
    /**
     * 消息内容
     */
    private String content;
     
    /**
     * 元数据（JSON格式）
     */
    private String metadata;
     

}
