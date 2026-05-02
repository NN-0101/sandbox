package com.sandbox.home.websocket.model.device;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 流式音频数据消息BO
 * <p>支持音频流式传输，包含帧序号和时间戳
 * @author: 0101
 * @create: 2026/04/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AudioStreamMsgBO extends UpMsgBO {

    /**
     * 音频帧序号（从0开始递增）
     */
    private long frameIndex;

    /**
     * 音频数据(Base64编码的PCM数据)
     */
    private String audioData;

    /**
     * 音频格式固定为pcm
     */
    private String audioFormat = "pcm";

    /**
     * 采样率固定为16000
     */
    private int sampleRate = 16000;

    /**
     * 每帧时长（毫秒），默认100ms
     */
    private int frameDuration = 100;

    /**
     * 是否为最后一帧
     */
    private boolean lastFrame = false;

    /**
     * 时间戳（采集时间，毫秒）
     */
    private long timestamp;
}
