package com.sandbox.home.websocket.handler.device;

import com.alibaba.fastjson2.JSONObject;
import com.sandbox.home.ai.AiChatFacade;
import com.sandbox.home.ai.enumeration.AiChatBizTypeEnum;
import com.sandbox.home.enumeration.IdentityTypeEnum;
import com.sandbox.home.funasr.FunASRSessionManager;
import com.sandbox.home.model.request.ai.AiMessageRequest;
import com.sandbox.home.websocket.channel.manager.AudioStreamManager;
import com.sandbox.home.websocket.channel.manager.DeviceChannelGroupManager;
import com.sandbox.home.websocket.enumeration.ContentTypeEnum;
import com.sandbox.home.websocket.enumeration.DownMsgTypeEnum;
import com.sandbox.home.websocket.enumeration.UpMsgTypeEnum;
import com.sandbox.home.websocket.handler.BaseBusinessHandler;
import com.sandbox.home.websocket.model.device.AudioStreamMsgBO;
import com.sandbox.home.websocket.model.device.DownMsgBO;
import com.sandbox.home.websocket.model.device.UpMsgBO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备音频数据处理器（消息类型 400）
 * <p>
 * 职责：接收设备音频帧 → 解码 → 转发 FunASR → 累积识别结果 → 触发大模型。
 *
 * <h3>数据流</h3>
 * <pre>
 * 设备 ── Base64 PCM ──→ Netty ──解码──→ FunASRSession ──PCM──→ FunASR
 *                                                      │
 *                                                      │ onMessage（FunASR 线程）
 *                                                      ↓
 *                                          handleAsrResult() ← 回调
 *                                                      │
 *                                ┌─────────────────────┼──────────────┐
 *                                │ 2pass-online        │ 2pass-offline       │ is_final=true
 *                                │ → 忽略（实时片段）  │ → append 到缓冲    │ → 取全部缓冲
 *                                │                     │                     │   推大模型
 *                                └─────────────────────┴──────────────┘
 * </pre>
 *
 * <h3>2pass 模式说明</h3>
 * 2pass 是 FunASR 的"实时+离线"双通道模式：
 * <ul>
 *   <li><b>2pass-online</b>：实时流式结果，每个音频帧都可能输出，文本是增量片段，延迟低但不准</li>
 *   <li><b>2pass-offline</b>：离线校验结果，静音停顿后输出完整一句，准确度高，每句一次性吐出</li>
 *   <li><b>is_final=true</b>：整个语音段结束，text 通常为空，不代表识别内容</li>
 * </ul>
 * 本处理器只用 offline 结果累积会话文本，online 结果仅打日志。
 *
 * <h3>回调设计</h3>
 * 回调不捕获 {@link ChannelHandlerContext}，改为通过 {@link DeviceChannelGroupManager#getChannel}
 * 获取当前活跃连接。设备重连后新连接自动生效，避免引用失效的 ctx。
 *
 * @author 0101
 * @since 2026-04-26
 */
@Slf4j
public class DeviceAudioDataHandler extends BaseBusinessHandler<UpMsgBO> {

    private final FunASRSessionManager sessionManager;

    private final AiChatFacade aiChatFacade;

    /**
     * 标记 Session 回调是否已绑定，避免每帧重复设置
     */
    private final Map<String, Boolean> callbackSetMap = new ConcurrentHashMap<>();

    /**
     * 设备会话文本累积缓冲：macId → 从开始说话到现在的全部文本（跨句拼接）
     */
    private final Map<String, StringBuilder> sessionTextBuffer = new ConcurrentHashMap<>();

    public DeviceAudioDataHandler(FunASRSessionManager sessionManager, AiChatFacade aiChatFacade) {

        this.sessionManager = sessionManager;
        this.aiChatFacade = aiChatFacade;
    }

    @Override
    protected void process(ChannelHandlerContext ctx, UpMsgBO msg) {
        String macId = getMacId(ctx);
        if (macId == null) {
            log.warn("未认证设备尝试发送音频数据");
            ctx.close();
            return;
        }

        String rawMessage = getRawMessage(ctx);
        if (rawMessage == null) {
            return;
        }

        try {
            AudioStreamMsgBO audioMsg = JSONObject.parseObject(rawMessage, AudioStreamMsgBO.class);
            if (audioMsg.isLastFrame()) {
                handleLastFrame(macId);
            } else {
                handleAudioData(macId, audioMsg);
            }
        } catch (Exception e) {
            log.error("设备 {} 音频消息处理失败", macId, e);
        }
    }

    /**
     * 处理音频数据帧：流管理 → Session 确保 → Base64 解码 → 发送 PCM
     */
    private void handleAudioData(String macId, AudioStreamMsgBO audioMsg) {
        AudioStreamManager.startStream(macId);

        if (!AudioStreamManager.isStreamActive(macId)) {
            log.warn("设备 {} 音频流已结束", macId);
            return;
        }

        ensureSessionWithCallback(macId);

        String base64Audio = audioMsg.getAudioData();
        if (base64Audio == null || base64Audio.isEmpty()) {
            return;
        }

        try {
            byte[] audioData = Base64.getDecoder().decode(base64Audio);
            long frameIndex = AudioStreamManager.incrementFrame(macId);
            sessionManager.sendAudio(macId, audioData);

            if (frameIndex >= 0 && frameIndex % 50 == 0) {
                log.debug("设备 {} 音频流: 已接收 {} 帧", macId, frameIndex);
            }
        } catch (IllegalArgumentException e) {
            log.error("设备 {} Base64解码失败", macId, e);
        } catch (Exception e) {
            log.error("设备 {} 音频处理异常", macId, e);
        }
    }

    /**
     * 处理最后一帧：结束音频流，通知 FunASR 语音结束
     */
    private void handleLastFrame(String macId) {
        if (AudioStreamManager.endStream(macId) == null) {
            log.warn("设备 {} 没有活跃的音频流", macId);
            return;
        }
        sessionManager.finishSpeaking(macId);
        log.info("设备 {} 音频流最后一帧已处理，等待 FunASR 最终结果", macId);
    }

    /**
     * 确保 Session 存在且回调已绑定（仅首次绑定回调，后续调用只确保 Session 存在）
     */
    private void ensureSessionWithCallback(String macId) {
        if (callbackSetMap.putIfAbsent(macId, Boolean.TRUE) == null) {
            sessionManager.ensureSession(macId, asrResult -> handleAsrResult(macId, asrResult));
        } else {
            sessionManager.ensureSession(macId);
        }
    }

    /**
     * 处理 FunASR 识别结果（在 FunASR WebSocket 的 onMessage 线程中执行）。
     *
     * <h3>处理策略</h3>
     * <ul>
     *   <li>2pass-online：忽略（实时增量片段，不参与最终文本）</li>
     *   <li>2pass-offline：追加到 {@code sessionTextBuffer}</li>
     *   <li>is_final=true：取出缓冲中全部累积文本，触发下游处理</li>
     * </ul>
     */
    private void handleAsrResult(String macId, String asrResult) {
        try {
            JSONObject asrJson = JSONObject.parseObject(asrResult);
            String text = asrJson.getString("text");
            boolean isFinal = asrJson.getBooleanValue("is_final");
            String mode = asrJson.getString("mode");

            log.debug("设备 {} ASR结果: mode={} isFinal={} text={}", macId, mode, isFinal, text);

            // 最终结束帧 → 取全部累积文本
            if (isFinal) {
                StringBuilder buffer = sessionTextBuffer.remove(macId);
                String finalText = (buffer != null) ? buffer.toString().trim() : "";
                if (!finalText.isEmpty()) {
                    onFinalRecognition(macId, finalText);
                }
                return;
            }

            // offline 结果 → 完整一句，追加到会话缓冲
            if ("2pass-offline".equals(mode) && StringUtils.isNotBlank(text)) {
                sessionTextBuffer.computeIfAbsent(macId, k -> new StringBuilder()).append(text);
                log.info("设备 {} 离线校验结果(累积中): {}", macId, text);
                return;
            }

            // online 结果 → 仅 debug 日志
            log.debug("设备 {} 实时片段: {}", macId, text);

        } catch (Exception e) {
            log.error("设备 {} 处理识别结果失败", macId, e);
        }
    }

    /**
     * 最终识别结果 → 大模型 → TTS → 下发设备
     */
    private void onFinalRecognition(String macId, String text) {
        log.info("TODO: 设备 {} 最终识别文本 → 大模型: {}", macId, text);
        AiMessageRequest build = AiMessageRequest.builder()
                .chatType(AiChatBizTypeEnum.DEVICE_DIALOGUE)
                .identifier(macId)
                .identityType(IdentityTypeEnum.DEVICE.getValue())
                .message(text)
                .build();
        Flux<String> stringFlux = aiChatFacade.sendMessageStream(build);
//        stringFlux
//                .doOnNext(chunk -> System.out.print(chunk))
//                .doOnComplete(() -> System.out.println("\n--- 流式输出完成 ---"))
//                .doOnError(e -> log.error("设备 {} 大模型调用失败", macId, e))
//                .subscribe();

        stringFlux
                .doOnNext(chunk -> sendTextChunkToDevice(macId, chunk))
                .doOnComplete(() -> sendStreamEndToDevice(macId))
                .doOnError(e -> {
                    log.error("设备 {} 大模型调用失败", macId, e);
                    sendErrorToDevice(macId, e.getMessage());
                })
                .subscribe();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String macId = getMacId(ctx);
        if (macId != null) {
            AudioStreamManager.endStream(macId);
            sessionManager.finishSpeaking(macId);
            sessionManager.removeSession(macId);
            callbackSetMap.remove(macId);
            sessionTextBuffer.remove(macId);
            log.info("设备 {} 已断开，资源已清理", macId);
        }
        super.channelInactive(ctx);
    }


    /**
     * 下发流式文本片段给设备
     */
    private void sendTextChunkToDevice(String macId, String chunk) {
        Channel channel = DeviceChannelGroupManager.getChannel(macId);
        if (channel == null || !channel.isActive()) {
            log.warn("设备 {} 连接已断开，无法下发文本", macId);
            return;
        }

        DownMsgBO downMsg = DownMsgBO.builder()
                .messageType(DownMsgTypeEnum.TTS_STREAM_CHUNK.getCode())
                .content(chunk)
                .contentType(ContentTypeEnum.TEXT.getCode())  // 0-文本
                .build();

        String jsonMsg = JSONObject.toJSONString(downMsg);
        channel.writeAndFlush(new TextWebSocketFrame(jsonMsg));

        log.debug("设备 {} 下发TTS片段: {}", macId, chunk);
    }

    /**
     * 通知设备流式输出结束
     */
    private void sendStreamEndToDevice(String macId) {
        Channel channel = DeviceChannelGroupManager.getChannel(macId);
        if (channel == null || !channel.isActive()) {
            log.warn("设备 {} 连接已断开，无法发送结束通知", macId);
            return;
        }

        DownMsgBO downMsg = DownMsgBO.builder()
                .messageType(DownMsgTypeEnum.TTS_STREAM_END.getCode())
                .content("")
                .contentType(ContentTypeEnum.TEXT.getCode())
                .build();

        String jsonMsg = JSONObject.toJSONString(downMsg);
        channel.writeAndFlush(new TextWebSocketFrame(jsonMsg));

        log.info("设备 {} TTS流式输出已结束", macId);
    }

    /**
     * 下发AI错误信息给设备
     */
    private void sendErrorToDevice(String macId, String errorMsg) {
        Channel channel = DeviceChannelGroupManager.getChannel(macId);
        if (channel == null || !channel.isActive()) {
            return;
        }

        DownMsgBO downMsg = DownMsgBO.builder()
                .messageType(DownMsgTypeEnum.AI_RESPONSE_ERROR.getCode())
                .content(errorMsg)
                .contentType(ContentTypeEnum.TEXT.getCode())
                .build();

        String jsonMsg = JSONObject.toJSONString(downMsg);
        channel.writeAndFlush(new TextWebSocketFrame(jsonMsg));

        log.error("设备 {} AI响应错误: {}", macId, errorMsg);
    }

    private String getRawMessage(ChannelHandlerContext ctx) {
        return ctx.channel().attr(AttributeKey.<String>valueOf("rawMessage")).get();
    }

    private String getMacId(ChannelHandlerContext ctx) {
        return ctx.channel().attr(AttributeKey.<String>valueOf("macId")).get();
    }

    @Override
    public int getHandlerType() {
        return UpMsgTypeEnum.AUDIO_STREAM.getCode();
    }
}