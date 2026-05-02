package com.sandbox.home.websocket;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.sandbox.home.ai.AiChatFacade;
import com.sandbox.home.funasr.FunASRSessionManager;
import com.sandbox.home.websocket.channel.DeviceWebSocketInitializer;
import com.sandbox.home.websocket.channel.UserWebSocketInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Properties;

/**
 * Netty 服务器启动引导
 * <p>
 * 同时启动设备服务器（9001）和用户服务器（9002），
 * 两者使用独立的线程组，资源隔离、互不影响。
 *
 * <h3>线程模型</h3>
 * <ul>
 *   <li>设备服务器：Boss 1线程 / Worker CPU核数×2</li>
 *   <li>用户服务器：Boss 2线程 / Worker 8线程</li>
 * </ul>
 *
 * @author xp
 * @since 2025-04-27
 */
@Slf4j
@Configuration
public class NettyBootstrap {

    @Value("${netty.device.port:9001}")
    private int devicePort;

    @Value("${netty.device.server-name:sandBoxWebsocket}")
    private String deviceServerName;

    @Value("${netty.user.port:9002}")
    private int userPort;

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private FunASRSessionManager funASRSessionManager;

    @Autowired
    private AiChatFacade aiChatFacade;

    private EventLoopGroup deviceBossGroup;
    private EventLoopGroup deviceWorkerGroup;
    private EventLoopGroup userBossGroup;
    private EventLoopGroup userWorkerGroup;

    /**
     * 启动 Netty 服务器（Spring 容器初始化后自动调用）
     */
    @PostConstruct
    public void start() throws InterruptedException, NacosException {
        log.info("正在启动 Netty 服务器...");
        startDeviceServer();
        // startUserServer();
        log.info("Netty 服务器启动完成 - 设备端口: {}, 用户端口: {}", devicePort, userPort);
    }

    /**
     * 优雅关闭（Spring 容器销毁前自动调用）
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭 Netty 服务器...");

        if (deviceBossGroup != null) deviceBossGroup.shutdownGracefully();
        if (deviceWorkerGroup != null) deviceWorkerGroup.shutdownGracefully();
        if (userBossGroup != null) userBossGroup.shutdownGracefully();
        if (userWorkerGroup != null) userWorkerGroup.shutdownGracefully();

        log.info("Netty 服务器已关闭");
    }

    private void startDeviceServer() throws InterruptedException, NacosException {
        deviceBossGroup = new NioEventLoopGroup(1);
        deviceWorkerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(deviceBossGroup, deviceWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new DeviceWebSocketInitializer(funASRSessionManager, redisTemplate, getNacosRegisterIp(), aiChatFacade))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(devicePort).sync();
        if (future.isSuccess()) {
            log.info("设备服务器启动成功，端口: {}", devicePort);
        } else {
            log.error("设备服务器启动失败: {}", future.cause().getMessage());
        }
        registerToNacos(devicePort, deviceServerName);
    }

    private void startUserServer() throws InterruptedException {
        userBossGroup = new NioEventLoopGroup(2);
        userWorkerGroup = new NioEventLoopGroup(8);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(userBossGroup, userWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new UserWebSocketInitializer())
                .option(ChannelOption.SO_BACKLOG, 256)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(userPort).sync();
        if (future.isSuccess()) {
            log.info("用户服务器启动成功，端口: {}", userPort);
        } else {
            log.error("用户服务器启动失败: {}", future.cause().getMessage());
        }
    }

    /**
     * Netty服务注册到nacos
     *
     * @param port       服务器端口
     * @param serverName 服务名称
     * @throws NacosException 异常
     */
    private void registerToNacos(Integer port, String serverName) throws NacosException {

        // 1. 获取 Nacos 命名服务实例
        Properties properties = new Properties();
        properties.setProperty("serverAddr", nacosDiscoveryProperties.getServerAddr());
        properties.setProperty("namespace", nacosDiscoveryProperties.getNamespace());
        properties.setProperty("username", nacosDiscoveryProperties.getUsername());
        properties.setProperty("password", nacosDiscoveryProperties.getPassword());

        NamingService namingService = NacosFactory.createNamingService(properties);

        Instance instance = new Instance();

        instance.setIp(nacosDiscoveryProperties.getIp());
        instance.setPort(port);
        instance.setServiceName(serverName);
        instance.setClusterName(nacosDiscoveryProperties.getClusterName());
        instance.setMetadata(nacosDiscoveryProperties.getMetadata());

        // 3. 注册实例到 Nacos
        namingService.registerInstance(serverName, nacosDiscoveryProperties.getGroup(), instance);
    }

    /**
     * 获取当前服务在 Nacos 注册的 IP 地址
     *
     * @return Nacos 注册 IP
     */
    public String getNacosRegisterIp() {
        String ip = nacosDiscoveryProperties.getIp();
        log.info("Nacos 注册 IP: {}", ip);
        return ip;
    }
}