package com.sandbox.demo.opentelemetry.kafka.consummer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

/**
 * Kafka 消费者服务
 * @author 0101
 */
/**
 * Kafka 消费者服务
 * @author 0101
 */
@Configuration
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    /**
     * 消费 sandbox-test-topic 的消息
     * 临时使用自动确认模式
     */
    @KafkaListener(
            topics = "sandbox-test-topic",
            groupId = "sandbox-open-telemetry-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTestTopic(String message) {  // 去掉 Acknowledgment 参数
        log.info("===== 收到 sandbox-test-topic 消息: {} =====", message);

        // 处理消息的业务逻辑
        processMessage(message);
        log.info("消息处理完成");
    }

    /**
     * 消费 sandbox-hello-topic 的消息
     */
    @KafkaListener(
            topics = "sandbox-hello-topic",
            groupId = "sandbox-open-telemetry-group"
    )
    public void consumeHelloTopic(String message) {
        log.info("===== 收到 sandbox-hello-topic 消息: {} =====", message);

        if (message.contains("error")) {
            log.warn("消息包含 error，模拟处理失败");
            throw new RuntimeException("模拟处理失败");
        }

        log.info("消息处理完成");
    }

    private void processMessage(String message) {
        log.debug("处理消息内容: {}", message);
    }
}
