package com.sandbox.home.websocket.model;

import lombok.Data;

/**
 * 上行消息基类
 * <p>所有设备/客户端发送给平台的消息均继承此类。
 * messageType 用于消息路由，version 用于协议兼容。
 *
 * @author 0101
 * @since 2026-03-16
 */
@Data
public class BaseUpMsgBO {

    /**
     * 消息类型，对应具体的业务枚举值
     */
    private int messageType;
    /**
     * 协议版本号，默认1
     */
    private int version = 1;
}