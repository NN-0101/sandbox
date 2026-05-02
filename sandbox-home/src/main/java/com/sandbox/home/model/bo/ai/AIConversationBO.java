package com.sandbox.home.model.bo.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: xp
 * @create: 2025/6/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIConversationBO {

    private String id;

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
