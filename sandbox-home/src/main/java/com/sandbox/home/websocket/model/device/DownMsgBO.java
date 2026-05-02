package com.sandbox.home.websocket.model.device;

import com.sandbox.home.websocket.model.BaseDownMsgBO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 平台下行设备消息
 * <p>平台发送给设备的消息载体，用于指令下发和响应返回。
 *
 * @author 0101
 * @since 2026-03-16
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DownMsgBO extends BaseDownMsgBO {
    // 继承 BaseDownMessageBO 的所有字段，可根据需要扩展设备特有字段
}