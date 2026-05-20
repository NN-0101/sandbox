package com.sandbox.demo.opentelemetry.config;

/**
 * @description:
 * @author: 0101
 * @create: 2026/05/19
 */

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka 配置类
 * @author 0101
 */
/**
 * Kafka 配置类
 * @author 0101
 */
@Configuration
public class KafkaConfig {

    /**
     * 创建测试 Topic（3个分区，1个副本）
     */
    @Bean
    public NewTopic testTopic() {
        return new NewTopic("sandbox-test-topic", 3, (short) 1);
    }

    /**
     * 创建另一个示例 Topic
     */
    @Bean
    public NewTopic helloTopic() {
        return new NewTopic("sandbox-hello-topic", 1, (short) 1);
    }

    /**
     * 配置错误处理器（死信队列）
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
    }

    /**
     * 单条消息监听器容器工厂（用于普通消费）
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.setBatchListener(false);
        return factory;
    }

    /**
     * 批量消息监听器容器工厂（新增，用于批量消费）
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> batchKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);  // 启用批量监听
        factory.setConcurrency(2);
        return factory;
    }
}