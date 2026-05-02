package com.sandbox.home.websocket.handler.device;

import com.sandbox.home.websocket.model.device.UpMsgBO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备消息兜底处理器
 * <p>捕获所有未被前面处理器处理的消息，记录错误日志。
 *
 * @author 0101
 * @since 2026-03-16
 */
@Slf4j
public class DeviceDefaultHandler extends SimpleChannelInboundHandler<UpMsgBO> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, UpMsgBO msg) {
        AttributeKey<String> macIdKey = AttributeKey.valueOf("macId");
        String macId = ctx.channel().attr(macIdKey).get();

        if (macId != null) {
            log.error("设备 {} 发送了未处理的消息类型: {}", macId, msg.getMessageType());
        } else {
            log.error("未认证设备发送了未处理的消息类型: {}, remoteAddress={}",
                    msg.getMessageType(), ctx.channel().remoteAddress());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("兜底处理器异常: remoteAddress={}", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
}