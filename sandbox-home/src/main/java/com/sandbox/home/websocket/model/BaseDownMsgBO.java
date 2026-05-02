package com.sandbox.home.websocket.model;

import com.sandbox.home.websocket.enumeration.DownMsgTypeEnum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 下行消息基类
 * <p>所有平台发送给设备/客户端的消息均继承此类。
 *
 * @author 0101
 * @since 2026-03-19
 */
@Data
@SuperBuilder
public class BaseDownMsgBO {

    /**
     * 消息类型，对应 {@link DownMsgTypeEnum}
     */
    private Integer messageType;
    /**
     * 消息发送时间，格式 yyyy-MM-dd HH:mm:ss
     */
    private String messageTime;
    /**
     * 消息内容，解析方式由 contentType 决定
     */
    private String content;
    /**
     * 内容类型，0-文本 1-JSON 2-Base64二进制
     */
    private Integer contentType;
    /**
     * 协议版本号，默认1
     */
    private int version = 1;
}