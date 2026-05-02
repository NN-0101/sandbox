package com.sandbox.home.config;

import com.sandbox.home.ai.annotations.AiChatService;
import com.sandbox.home.ai.chat.BaseChatMessage;
import com.sandbox.home.ai.enumeration.AiChatBizTypeEnum;
import com.sandbox.home.ai.memory.DBChatMemory;
import com.sandbox.home.service.AiMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI 模块配置
 * <p>
 * 初始化 ChatClient（日志 + 记忆顾问）、基于数据库的 ChatMemory、
 * AiChatTypeEnum → BaseChatMessage 策略映射，以及 Redis 向量存储。
 *
 * @author 0101
 * @since 2026/03/18
 */
@Slf4j
@Configuration
public class AiConfig {

    @Autowired
    private AiMessageService aiMessageService;

    /**
     * 配置 ChatClient，附加日志和对话记忆顾问
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                //设置提示词.defaultSystem()
                .defaultAdvisors(
                        //配置日志
                        new SimpleLoggerAdvisor(),
                        //聊天记忆
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .build();
    }

    /**
     * 基于数据库的聊天记忆实现
     */
    @Bean
    public ChatMemory chatMemory() {
        return new DBChatMemory(aiMessageService);
    }

    /**
     * 构建 AI 服务策略映射，根据 @AiChatService 注解区分类型，重复类型抛出异常
     */
    @Bean
    public Map<AiChatBizTypeEnum, BaseChatMessage> chatMessageStrategyMap(List<BaseChatMessage> strategies) {
        return strategies.stream().collect(Collectors.toMap(
                strategy -> strategy.getClass().getAnnotation(AiChatService.class).value(),
                Function.identity(),
                (existing, replacement) -> {
                    throw new IllegalStateException("发现重复策略: " + existing.getClass().getName()
                            + " 和 " + replacement.getClass().getName());
                }
        ));
    }

    /**
     * Redis 连接池
     */
    @Bean
    public JedisPooled jedisPooled(RedisProperties redisProperties) {
        return new JedisPooled(redisProperties.getHost(), redisProperties.getPort(),
                redisProperties.getUsername(), redisProperties.getPassword());
    }

    /**
     * Redis 向量存储，用于 RAG 检索增强
     */
    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel, JedisPooled jedisPooled) {
//        // 获取当前嵌入模型的维度
//        int embeddingDimensions = embeddingModel.dimensions();
//        log.info("Using embedding model with dimensions: {}", embeddingDimensions);
//
//        // 删除旧索引（如果存在）
//        try {
//            jedisPooled.ftDropIndex("spring-ai-index");
//            log.info("Dropped existing vector index");
//        } catch (Exception e) {
//            log.info("No existing vector index found");
//        }
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .initializeSchema(true)
                .build();
    }
}