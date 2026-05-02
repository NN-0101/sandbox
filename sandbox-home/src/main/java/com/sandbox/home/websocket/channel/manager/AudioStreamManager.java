package com.sandbox.home.websocket.channel.manager;

import com.sandbox.home.websocket.model.device.AudioStreamStateBO;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 音频流管理器 - 全局唯一音频流状态管理
 *
 * <p>基于 {@code static} 修饰的 {@link ConcurrentHashMap} 实现 JVM 级别全局唯一存储，
 * 确保所有组件访问的是同一份音频流状态。
 *
 * <h3>为什么全局唯一？</h3>
 * 音频处理涉及 Handler、Service、线程池等多个组件，必须保证它们看到的是
 * 同一份音频流状态。{@code static final} 的 {@code AUDIO_STREAMS} 存储在 JVM 方法区，
 * 整个应用生命周期内只存在一份。
 *
 * <h3>设计一致性</h3>
 * 与 {@code DeviceChannelGroup} 保持相同设计：
 * <ul>
 *   <li>static 变量 + static 方法，无需实例化</li>
 *   <li>方便任意组件直接调用，零依赖注入</li>
 *   <li>全局统一管理，便于监控和清理</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>
 * // 开始接收音频
 * AudioStreamManager.startStream("AA:BB:CC:DD:EE:FF");
 *
 * // 记录音频帧
 * long frameIndex = AudioStreamManager.incrementFrame("AA:BB:CC:DD:EE:FF");
 *
 * // 结束音频流
 * AudioStreamManager.endStream("AA:BB:CC:DD:EE:FF");
 * </pre>
 *
 * @author 0101
 * @since 2026-04-27
 */
@Slf4j
public class AudioStreamManager {

    /**
     * 音频流状态映射表（全局唯一）
     *
     * <p>{@code static final} 保证 JVM 内仅此一份。
     */
    private static final Map<String, AudioStreamStateBO> AUDIO_STREAMS = new ConcurrentHashMap<>();

    /**
     * 开始音频流（设备开始发送音频时调用）
     *
     * <p>使用 {@link ConcurrentHashMap#computeIfAbsent} 保证原子性，
     * 同一设备不会重复创建。
     */
    public static void startStream(String macId) {
        AUDIO_STREAMS.computeIfAbsent(macId, k -> {
            log.info("设备 {} 开始接收音频流", macId);
            return new AudioStreamStateBO();
        });
    }

    /**
     * 记录音频帧并返回当前帧序号
     *
     * @return 当前帧序号（从0开始），流不存在返回 -1
     */
    public static long incrementFrame(String macId) {
        AudioStreamStateBO state = AUDIO_STREAMS.get(macId);
        if (state != null && state.isActive()) {
            return state.getFrameCount().getAndIncrement();
        }
        return -1;
    }

    /**
     * 结束音频流（设备停止发送或断开连接时调用）
     */
    public static AudioStreamStateBO endStream(String macId) {
        AudioStreamStateBO state = AUDIO_STREAMS.remove(macId);
        if (state != null) {
            state.setActive(false);
            long duration = System.currentTimeMillis() - state.getStartTime();
            log.info("设备 {} 音频流结束: 总帧数={}, 持续 {}ms",
                    macId, state.getFrameCount().get(), duration);
        }
        return state;
    }

    /**
     * 检查音频流是否活跃
     *
     * @return true-流活跃，false-流不存在或已结束
     */
    public static boolean isStreamActive(String macId) {
        AudioStreamStateBO state = AUDIO_STREAMS.get(macId);
        return state != null && state.isActive();
    }

    /**
     * 获取活跃音频流数量（监控用）
     */
    public static int getActiveStreamCount() {
        return AUDIO_STREAMS.size();
    }

    /**
     * 应用关闭时清理所有流
     *
     * <p>注意：由于是纯静态工具类，此方法需在应用关闭时手动调用，
     * 或通过 Spring 的 @PreDestroy 在某个 Bean 中触发。
     *
     * <p>建议在 {@code FunASRSessionManager.destroy()} 中一并调用：
     * <pre>
     * AudioStreamManager.destroy();  // 清理音频流状态
     * // ... 清理 FunASR 会话
     * </pre>
     */
    public static void destroy() {
        if (!AUDIO_STREAMS.isEmpty()) {
            log.warn("清理所有音频流状态，数量: {}", AUDIO_STREAMS.size());
            AUDIO_STREAMS.clear();
        }
    }
}