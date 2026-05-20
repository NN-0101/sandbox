package com.sandbox.demo.opentelemetry.controller;

import com.sandbox.demo.opentelemetry.kafka.produce.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: 0101
 * @create: 2026/05/19
 */
@RestController
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @GetMapping("/test")
    public String test() {
        log.info("收到测试请求");
        log.info("当前 TraceId 应该出现在这里");
        return "OK - 检查日志中的 TraceId";
    }

    @GetMapping("/hello")
    public String hello() {
        log.info("Hello 接口被调用");
        return "Hello, OpenTelemetry!";
    }

    /**
     * 发送消息到 Kafka
     * @param topic Topic 名称
     * @param message 消息内容
     * @return 发送结果
     */
    @GetMapping("/kafka/send")
    public Map<String, Object> sendKafkaMessage(
            @RequestParam(defaultValue = "sandbox-test-topic") String topic,
            @RequestParam(defaultValue = "test-key") String key,
            @RequestParam String message) {

        log.info("收到 Kafka 发送请求: topic={}, key={}, message={}", topic, key, message);

        Map<String, Object> result = new HashMap<>();
        try {
            kafkaProducerService.sendMessage(topic, key, message);
            result.put("success", true);
            result.put("message", "消息已发送");
            result.put("topic", topic);
            result.put("key", key);
        } catch (Exception e) {
            log.error("发送消息失败", e);
            result.put("success", false);
            result.put("message", "发送失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 同步发送消息
     */
    @GetMapping("/kafka/sendSync")
    public Map<String, Object> sendKafkaMessageSync(
            @RequestParam(defaultValue = "sandbox-test-topic") String topic,
            @RequestParam String message) {

        Map<String, Object> result = new HashMap<>();
        try {
            var sendResult = kafkaProducerService.sendMessageSync(topic, null, message);
            result.put("success", true);
            result.put("topic", sendResult.getRecordMetadata().topic());
            result.put("partition", sendResult.getRecordMetadata().partition());
            result.put("offset", sendResult.getRecordMetadata().offset());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 发送测试消息到测试 Topic
     */
    @GetMapping("/kafka/sendTest")
    public Map<String, Object> sendTestMessage(@RequestParam String message) {
        return sendKafkaMessage("sandbox-test-topic", "test-key", message);
    }

    /**
     * 发送消息到 Hello Topic
     */
    @GetMapping("/kafka/sendHello")
    public Map<String, Object> sendHelloMessage(@RequestParam String message) {
        return sendKafkaMessage("sandbox-hello-topic", "hello-key", message);
    }
}