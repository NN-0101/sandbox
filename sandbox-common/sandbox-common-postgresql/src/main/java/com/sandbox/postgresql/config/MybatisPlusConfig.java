package com.sandbox.postgresql.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * MyBatis-Plus 雪花算法 ID 生成器配置
 * <p>
 * workerId 根据服务器 IP 最后一段取模 32 自动生成，
 * datacenterId 从配置文件读取。IP 获取失败时降级为随机数。
 * <p>
 * 注意：容器/虚拟化环境 IP 可能重复，建议生产环境手动配置。
 *
 * @author 0101
 * @since 2026-03-13
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    @Value("${mybatis-plus.datacenter-id:0}")
    private long datacenterId;

    @Bean
    public IdentifierGenerator idGenerator() {
        long workerId = getWorkerIdByIp();
        log.info("idGenerator workerId:{}, datacenterId:{}", workerId, datacenterId);
        return new DefaultIdentifierGenerator(workerId, datacenterId);
    }

    /**
     * 配置 MyBatis-Plus 自定义 TypeHandler
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            // 注册 JacksonTypeHandler 用于 JSON/JSONB 类型
            configuration.getTypeHandlerRegistry().register(JacksonTypeHandler.class);

            // 注意：其他 TypeHandler 在实体类的 @TableField 中通过 typeHandler 属性指定即可
        };
    }

    /**
     * 根据服务器 IP 最后一段取模 32 生成 workerId，失败时返回随机值
     */
    public static long getWorkerIdByIp() {
        try {
            String localhost = InetAddress.getLocalHost().getHostAddress();
            List<String> hostSplit = Arrays.asList(localhost.split("\\."));
            long lastSegment = Long.parseLong(hostSplit.get(hostSplit.size() - 1));
            long workerId = Math.abs(lastSegment % 32);
            log.info("根据 IP 生成 workerId 成功: IP={}, lastSegment={}, workerId={}", localhost, lastSegment, workerId);
            return workerId;
        } catch (Exception e) {
            long workerId = Math.abs(ThreadLocalRandom.current().nextLong(0, 31) % 32);
            log.error("根据 IP 生成 workerId 失败，使用随机值 workerId:{}", workerId, e);
            return workerId;
        }
    }
}