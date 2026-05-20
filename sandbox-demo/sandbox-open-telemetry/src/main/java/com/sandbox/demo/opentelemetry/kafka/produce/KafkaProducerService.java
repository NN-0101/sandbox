package com.sandbox.demo.opentelemetry.kafka.produce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 生产者服务
 * @author 0101
 */
@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送消息到指定 Topic
     * @param topic Kafka Topic
     * @param key 消息 Key（用于分区）
     * @param message 消息内容
     */
    public void sendMessage(String topic, String key, String message) {
        log.info("准备发送消息到 Topic: {}, Key: {}, Message: {}", topic, key, message);

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("消息发送成功! Topic: {}, Partition: {}, Offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("消息发送失败! Topic: {}, Key: {}, Message: {}", topic, key, message, ex);
            }
        });
    }

    /**
     * 发送消息（不指定 Key）
     */
    public void sendMessage(String topic, String message) {
        sendMessage(topic, null, message);
    }

    /**
     * 同步发送消息
     */
    public SendResult<String, String> sendMessageSync(String topic, String key, String message) {
        log.info("同步发送消息到 Topic: {}, Key: {}, Message: {}", topic, key, message);
        try {
            SendResult<String, String> result = kafkaTemplate.send(topic, key, message).get();
            log.info("消息同步发送成功! Topic: {}, Partition: {}, Offset: {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            return result;
        } catch (Exception e) {
            log.error("消息同步发送失败!", e);
            throw new RuntimeException("Kafka 消息发送失败", e);
        }
    }
}