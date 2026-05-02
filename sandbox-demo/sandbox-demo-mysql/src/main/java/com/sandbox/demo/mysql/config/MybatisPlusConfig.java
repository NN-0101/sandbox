package com.sandbox.demo.mysql.config;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * MyBatis-Plus 配置
 * <p>
 * 配置雪花算法 ID 生成器，workerId 根据服务器 IP 最后一段取模 32 自动生成，
 * datacenterId 从配置文件读取。IP 获取失败时降级为随机数。
 * <p>
 * 注意：容器/虚拟化环境 IP 可能重复，建议手动配置 workerId。
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
     * 根据服务器 IP 最后一段取模 32 生成 workerId，失败时返回随机值
     */
    public static long getWorkerIdByIp() {
        try {
            String localhost = InetAddress.getLocalHost().getHostAddress();
            List<String> hostSplit = Arrays.asList(localhost.split("\\."));
            long lastSegment = Long.parseLong(hostSplit.get(hostSplit.size() - 1));
            long workerId = Math.abs(lastSegment % 32);
            log.info("根据 IP 生成 workerId 成功: IP={}, lastSegment={}, workerId={}",
                    localhost, lastSegment, workerId);
            return workerId;
        } catch (Exception e) {
            long num = ThreadLocalRandom.current().nextLong(0, 31);
            long workerId = Math.abs(num % 32);
            log.error("根据 IP 生成 workerId 失败，使用随机值 workerId:{}", workerId, e);
            return workerId;
        }
    }
}