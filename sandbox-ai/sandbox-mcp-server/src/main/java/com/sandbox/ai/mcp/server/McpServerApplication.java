package com.sandbox.ai.mcp.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @description:
 * @author: 0101
 * @create: 2026/05/07
 */
@SpringBootApplication(scanBasePackages = {"com.sandbox.**"})
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
