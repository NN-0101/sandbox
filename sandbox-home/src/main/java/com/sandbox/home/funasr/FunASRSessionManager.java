package com.sandbox.home.funasr;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * FunASR 会话管理器
 * <p>
 * 管理所有设备的 {@link FunASRSession}，每个设备一个独立会话。
 *
 * <h3>核心设计</h3>
 * <ul>
 *   <li><b>会话隔离：</b>deviceId → FunASRSession 一对一映射</li>
 *   <li><b>懒加载：</b>首次发送音频时才建立连接</li>
 *   <li><b>回调绑定：</b>Session 创建时绑定识别结果回调，不可变</li>
 *   <li><b>自动清理：</b>设备断开时调用 removeSession() 释放资源</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-04-26
 */
@Slf4j
@Component
public class FunASRSessionManager {

    @Value("${funasr.ws.url:ws://localhost:10095}")
    private String funASRWsUrl;

    /** 设备会话映射：deviceId → FunASRSession */
    private final Map<String, FunASRSession> sessions = new ConcurrentHashMap<>();

    /**
     * 获取或创建设备会话
     *
     * @param deviceId 设备唯一标识
     * @param callback 识别结果回调（仅在首次创建时生效）
     * @return FunASRSession 实例
     */
    public FunASRSession ensureSession(String deviceId, Consumer<String> callback) {
        return sessions.computeIfAbsent(deviceId, id -> {
            log.info("为设备 {} 创建 FunASR 会话", id);
            FunASRSession session = new FunASRSession(id, funASRWsUrl, callback);
            session.start();
            return session;
        });
    }

    /**
     * 获取或创建设备会话（无回调版本，用于只需发送不需接收结果的场景）
     */
    public FunASRSession ensureSession(String deviceId) {
        return ensureSession(deviceId, null);
    }

    /**
     * 发送音频数据到指定设备的 FunASR 会话
     */
    public void sendAudio(String deviceId, byte[] audioData) {
        FunASRSession session = sessions.get(deviceId);
        if (session != null) {
            session.sendAudio(audioData);
        } else {
            log.warn("设备 {} 的 FunASR 会话不存在", deviceId);
        }
    }

    /**
     * 发送语音结束标识到指定设备
     */
    public void finishSpeaking(String deviceId) {
        FunASRSession session = sessions.get(deviceId);
        if (session != null) {
            session.finishSpeaking();
        }
    }

    /**
     * 移除并关闭设备会话
     */
    public void removeSession(String deviceId) {
        FunASRSession session = sessions.remove(deviceId);
        if (session != null) {
            session.close();
        }
    }

    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    @PreDestroy
    public void destroy() {
        log.info("关闭所有 FunASR 会话，数量: {}", sessions.size());
        sessions.forEach((id, session) -> {
            try {
                session.close();
            } catch (Exception e) {
                log.error("关闭设备 {} 会话异常", id, e);
            }
        });
        sessions.clear();
    }
}