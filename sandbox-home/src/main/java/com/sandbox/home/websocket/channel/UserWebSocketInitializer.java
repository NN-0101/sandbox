package com.sandbox.home.websocket.channel;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 用户 WebSocket 通道初始化器
 * <p>
 * 与设备服务器隔离，使用独立路径 "/ws/user"。
 * 协议层已就绪，业务处理器待扩展。
 *
 * @author 0101
 * @since 2026-03-16
 */
public class UserWebSocketInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // HTTP 层
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(65536));

        // WebSocket 层（路径 /ws/user，与设备 /ws/device 区分）
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws/user"));

        // TODO: 用户业务处理器（待扩展）
        // pipeline.addLast(new UserFrameHandler());       // 协议转换
        // pipeline.addLast(new UserAuthHandler());        // 认证
        // pipeline.addLast(new UserBusinessHandler());    // 业务处理
        // pipeline.addLast(new UserDefaultHandler());     // 兜底
    }
}