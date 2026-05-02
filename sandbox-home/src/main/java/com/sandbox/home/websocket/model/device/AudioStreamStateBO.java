package com.sandbox.home.websocket.model.device;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @description: 音频流状态 - 单个设备的音频流追踪
 * <p>记录设备音频流的实时指标：帧数、持续时间、活跃状态。
 * 使用 {@link AtomicLong} 保证帧计数的线程安全。
 * @author: 0101
 * @create: 2026/4/27
 */
@Data
public class AudioStreamStateBO {

    /**
     * 音频帧计数器（线程安全）
     */
    final AtomicLong frameCount = new AtomicLong(0);

    /**
     * 流开始时间戳
     */
    private final long startTime;

    /**
     * 流是否活跃（volatile 保证多线程可见性）
     */
    volatile boolean isActive = true;

    public AudioStreamStateBO() {
        this.startTime = System.currentTimeMillis();
    }
}
