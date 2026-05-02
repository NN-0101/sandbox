package com.sandbox.home.websocket.handler.device;

import com.alibaba.fastjson2.JSONObject;
import com.sandbox.home.websocket.channel.manager.DeviceChannelGroupManager;
import com.sandbox.home.websocket.model.device.UpMsgBO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备消息帧处理器 - 协议转换与连接生命周期管理
 * <p>作为管道入口，负责 WebSocket 帧到业务对象的转换，以及连接的建立、断开、异常处理。
 *
 * @author 0101
 * @since 2026-03-16
 */
@Slf4j
public class DeviceFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String request = msg.text();

        // 保存原始消息到Channel属性（供后续Handler使用）
        AttributeKey<String> rawMessageKey = AttributeKey.valueOf("rawMessage");
        ctx.channel().attr(rawMessageKey).set(request);

        // 解析为业务对象
        UpMsgBO deviceUpMessageBO = JSONObject.parseObject(request, UpMsgBO.class);

        // 传递给下一个Handler
        ctx.fireChannelRead(deviceUpMessageBO);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("新设备连接: remoteAddress={}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        AttributeKey<String> macIdKey = AttributeKey.valueOf("macId");
        String macId = ctx.channel().attr(macIdKey).get();

        if (macId != null) {
            DeviceChannelGroupManager.removeChannel(macId);
            log.info("设备离线: macId={}, remoteAddress={}", macId, ctx.channel().remoteAddress());
        } else {
            log.info("未认证设备断开连接: remoteAddress={}", ctx.channel().remoteAddress());
        }

        // 清理原始消息属性
        AttributeKey<String> rawMessageKey = AttributeKey.valueOf("rawMessage");
        ctx.channel().attr(rawMessageKey).set(null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        try {
            this.channelInactive(ctx);
            log.error("设备连接异常: remoteAddress={}, error={}",
                    ctx.channel().remoteAddress(), cause.getClass().getSimpleName(), cause);
        } finally {
            ctx.close();
        }
    }
}