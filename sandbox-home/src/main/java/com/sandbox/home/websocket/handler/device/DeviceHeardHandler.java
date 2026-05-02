package com.sandbox.home.websocket.handler.device;

import com.alibaba.fastjson2.JSONObject;
import com.sandbox.home.websocket.enumeration.ContentTypeEnum;
import com.sandbox.home.websocket.enumeration.DownMsgTypeEnum;
import com.sandbox.home.websocket.enumeration.UpMsgTypeEnum;
import com.sandbox.home.websocket.handler.BaseBusinessHandler;
import com.sandbox.home.websocket.model.device.DownMsgBO;
import com.sandbox.home.websocket.model.device.UpMsgBO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备心跳处理器
 * <p>处理 HEARTBEAT 消息，维持连接活性。
 *
 * @author 0101
 * @since 2026-03-16
 */
@Slf4j
public class DeviceHeardHandler extends BaseBusinessHandler<UpMsgBO> {

    @Override
    protected void process(ChannelHandlerContext ctx, UpMsgBO msg) {
        AttributeKey<String> macIdKey = AttributeKey.valueOf("macId");
        String macId = ctx.channel().attr(macIdKey).get();

        log.debug("接收设备心跳: macId={}", macId);

        // TODO 更新设备最后心跳时间
    }

    @Override
    public int getHandlerType() {
        return UpMsgTypeEnum.HEARTBEAT.getCode();
    }
}