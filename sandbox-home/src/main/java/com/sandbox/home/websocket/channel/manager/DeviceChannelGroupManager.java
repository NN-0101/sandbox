package com.sandbox.home.websocket.channel.manager;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备连接通道组 - 全局唯一连接管理器
 *
 * <p>基于 {@code static} 修饰的 {@link ConcurrentHashMap} 实现 JVM 级别的全局唯一存储，
 * 确保所有组件访问的是同一个设备连接映射表。
 *
 * <h3>为什么全局唯一？</h3>
 * {@code DEVICE_CHANNEL_GROUP} 是 {@code static final} 修饰的类变量，存储在 JVM 方法区，
 * 整个应用生命周期内只存在一份。无论多少个 Netty Channel、多少个 Handler 实例，
 * 都共享这一个 Map，保证了设备连接信息的全局一致性。
 *
 * <h3>核心职责</h3>
 * <ul>
 *   <li>以设备 MAC 地址为 Key，Netty {@link Channel} 为 Value，维护在线设备连接</li>
 *   <li>提供线程安全的添加、获取、移除操作</li>
 *   <li>作为跨组件（Handler、Service 等）访问设备连接的唯一入口</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>
 * // 设备认证成功后注册
 * DeviceChannelGroup.addChannel("AA:BB:CC:DD:EE:FF", channel);
 *
 * // 向设备推送消息（任何地方都能获取到同一个 Channel）
 * Channel channel = DeviceChannelGroup.getChannel("AA:BB:CC:DD:EE:FF");
 * if (channel != null && channel.isActive()) {
 *     channel.writeAndFlush(message);
 * }
 *
 * // 设备断开时清理
 * DeviceChannelGroup.removeChannel("AA:BB:CC:DD:EE:FF");
 * </pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li><b>单节点局限：</b>Channel 对象不可序列化，仅适用于单机部署。分布式环境需另配 Redis 等</li>
 *   <li><b>防止泄漏：</b>设备断开时务必调用 {@link #removeChannel}，否则 Channel 无法 GC</li>
 *   <li><b>有效性检查：</b>获取 Channel 后应先调用 {@link Channel#isActive()} 验证连接状态</li>
 *   <li><b>设备唯一性：</b>以 MAC 地址为唯一标识，同一 MAC 不会重复注册（{@code computeIfAbsent}）</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-03-16
 */
public class DeviceChannelGroupManager {

    /**
     * 设备连接映射表（全局唯一）
     *
     * <p>{@code static final} 保证 JVM 内仅此一份，所有线程共享。
     * {@link ConcurrentHashMap} 保证高并发下的线程安全。
     */
    private static final Map<String, Channel> DEVICE_CHANNEL_GROUP = new ConcurrentHashMap<>();

    /**
     * 注册设备连接
     *
     * <p>使用 {@link ConcurrentHashMap#computeIfAbsent} 原子操作，
     * 同一 MAC 不会重复注册，防止已认证设备被意外覆盖。
     *
     * @param macId   设备 MAC 地址
     * @param channel Netty 连接通道
     */
    public static void addChannel(String macId, Channel channel) {
        DEVICE_CHANNEL_GROUP.computeIfAbsent(macId, k -> channel);
    }

    /**
     * 获取设备连接
     *
     * <p>返回 {@code null} 表示设备离线或未注册。
     * 调用方获取后应检查 {@link Channel#isActive()}。
     *
     * @param macId 设备 MAC 地址
     * @return 设备 Channel，不存在返回 null
     */
    public static Channel getChannel(String macId) {
        return DEVICE_CHANNEL_GROUP.get(macId);
    }

    /**
     * 移除设备连接
     *
     * <p>设备断开时调用，防止内存泄漏。仅移除映射关系，不主动关闭 Channel。
     *
     * @param macId 设备 MAC 地址
     */
    public static void removeChannel(String macId) {
        DEVICE_CHANNEL_GROUP.remove(macId);
    }
}