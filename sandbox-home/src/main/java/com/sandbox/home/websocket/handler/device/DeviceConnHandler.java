package com.sandbox.home.websocket.handler.device;

import com.alibaba.fastjson2.JSONObject;
import com.sandbox.home.constant.Constant;
import com.sandbox.home.constant.RedisKeyConstant;
import com.sandbox.home.websocket.channel.manager.DeviceChannelGroupManager;
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
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 设备连接认证处理器
 * <p>处理 CONN 消息，完成设备身份绑定和连接注册。
 *
 * @author 0101
 * @since 2026-03-16
 */
@Slf4j
public class DeviceConnHandler extends BaseBusinessHandler<UpMsgBO> {

    private final RedisTemplate<String, Object> redisTemplate;

    private final String serverInstanceId;

    public DeviceConnHandler(RedisTemplate<String, Object> redisTemplate, String serverInstanceId) {
        this.redisTemplate = redisTemplate;
        this.serverInstanceId = serverInstanceId;
    }

    @Override
    protected void process(ChannelHandlerContext ctx, UpMsgBO msg) {
        // TODO 实现设备认证逻辑（签名校验、令牌验证等）
        String macId = msg.getMacId();

        AttributeKey<String> macIdKey = AttributeKey.valueOf("macId");
        ctx.channel().attr(macIdKey).set(macId);
        DeviceChannelGroupManager.addChannel(macId, ctx.channel());
        // 将设备所在机器ip写入redis
        String redisKey = String.format(RedisKeyConstant.DEVICE_NETTY_CONNECTION_REDIS_KEY, macId);
        redisTemplate.opsForValue().set(redisKey, serverInstanceId);
        // 这里的过期时间秒跟DeviceWebSocketInitializer的ReadTimeoutHandler保持一致
        redisTemplate.expire(redisKey, Constant.DEVICE_NETTY_READ_TIMEOUT, TimeUnit.SECONDS);
        log.info("设备认证成功: macId={}, remoteAddress={}", macId, ctx.channel().remoteAddress());

        // 返回认证响应
        DownMsgBO response = DownMsgBO.builder()
                .contentType(ContentTypeEnum.TEXT.getCode())
                .messageType(DownMsgTypeEnum.CONN_RESPONSE.getCode())
                .messageTime(String.valueOf(System.currentTimeMillis())).build();
        ctx.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(response)));
    }

    @Override
    public int getHandlerType() {
        return UpMsgTypeEnum.CONN.getCode();
    }
}