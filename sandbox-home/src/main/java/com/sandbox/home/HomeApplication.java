package com.sandbox.home;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动类
 *
 * @author 0101
 * @since 2026-04-30
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.baomidou.mybatisplus.core.mapper", "com.sandbox.home.mapper"})
public class HomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeApplication.class, args);
    }
}