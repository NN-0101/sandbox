package com.sandbox.home.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sandbox.home.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * @description: AI聊天会话表(AiConversation)表数据库实体
 * @author: 0101
 * @create: 2026-04-30 15:26:07
 */
@Data
@TableName("t_ai_conversation")
@EqualsAndHashCode(callSuper = true)
public class AiConversationDO extends BaseModel<AiConversationDO> {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 业务类型
     */
    private String business;
     
    /**
     * 标识类型：user：用户手机号、device：设备MAC
     */
    private String identityType;
     
    /**
     * 标识值（手机号或MAC地址）
     */
    private String identifier;
     
    /**
     * 会话名称
     */
    private String name;
     

}
