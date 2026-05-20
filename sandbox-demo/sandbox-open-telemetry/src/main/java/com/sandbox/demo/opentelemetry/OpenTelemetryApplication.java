package com.sandbox.demo.opentelemetry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * @description:
 * @author: 0101
 * @create: 2026/05/19
 */
@SpringBootApplication
@EnableKafka
public class OpenTelemetryApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenTelemetryApplication.class, args);
    }
}
