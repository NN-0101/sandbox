package com.sandbox.demo.postgresql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @description:
 * @author: 0101
 * @create: 2026/05/06
 */
@SpringBootApplication(scanBasePackages = {"com.sandbox.**"})
public class PostgresqlDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostgresqlDemoApplication.class, args);
    }
}
