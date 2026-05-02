package com.sandbox.home.websocket.model.device;

import com.sandbox.home.websocket.model.BaseUpMsgBO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备上行消息
 * <p>设备发送给平台的消息载体，包含设备唯一标识。
 *
 * @author 0101
 * @since 2026-03-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpMsgBO extends BaseUpMsgBO {

    /**
     * 设备MAC地址，用于设备认证和消息路由
     */
    private String macId;
}