package com.sandbox.home.websocket.channel;

import com.sandbox.home.ai.AiChatFacade;
import com.sandbox.home.funasr.FunASRSessionManager;
import com.sandbox.home.websocket.handler.device.DeviceAudioDataHandler;
import com.sandbox.home.websocket.handler.device.DeviceConnHandler;
import com.sandbox.home.websocket.handler.device.DeviceDefaultHandler;
import com.sandbox.home.websocket.handler.device.DeviceFrameHandler;
import com.sandbox.home.websocket.handler.device.DeviceHeardHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 设备 WebSocket 通道初始化器
 *
 * <h3>Pipeline 顺序</h3>
 * <ol>
 *   <li>HTTP 编解码</li>
 *   <li>WebSocket 协议处理</li>
 *   <li>DeviceFrameHandler → TextWebSocketFrame 转 UpMsgBO</li>
 *   <li>DeviceConnHandler → 认证(101)</li>
 *   <li>DeviceHeardHandler → 心跳</li>
 *   <li>DeviceAudioDataHandler → 音频流(400)</li>
 *   <li>DeviceDefaultHandler → 兜底</li>
 * </ol>
 *
 * @author 0101
 * @since 2026-03-16
 */
public class DeviceWebSocketInitializer extends ChannelInitializer<SocketChannel> {

    private final FunASRSessionManager funASRSessionManager;

    private final RedisTemplate<String, Object> redisTemplate;

    private final AiChatFacade aiChatFacade;

    private final String serverInstanceId;

    public DeviceWebSocketInitializer(FunASRSessionManager funASRSessionManager, RedisTemplate<String, Object> redisTemplate, String serverInstanceId, AiChatFacade aiChatFacade) {
        this.funASRSessionManager = funASRSessionManager;
        this.redisTemplate = redisTemplate;
        this.serverInstanceId = serverInstanceId;
        this.aiChatFacade = aiChatFacade;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // HTTP 层
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(65536));

        // WebSocket 层
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws/device"));
//        pipeline.addLast(new ReadTimeoutHandler(Constant.DEVICE_NETTY_READ_TIMEOUT, TimeUnit.SECONDS));

        // 协议转换
        pipeline.addLast(new DeviceFrameHandler());

        // 业务处理（责任链）
        pipeline.addLast(new DeviceConnHandler(redisTemplate, serverInstanceId));       // 101 认证
        pipeline.addLast(new DeviceHeardHandler());      // 心跳
        pipeline.addLast(new DeviceAudioDataHandler(funASRSessionManager, aiChatFacade)); // 400 音频流

        // 兜底
        pipeline.addLast(new DeviceDefaultHandler());
    }
}