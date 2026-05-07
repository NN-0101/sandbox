package com.sandbox.ai.agent.config;

import com.sandbox.ai.agent.chat.BaseChatMessage;
import com.sandbox.ai.agent.chat.annotations.AiChatService;
import com.sandbox.ai.agent.enumeration.AiChatBizTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    private ChatMemory chatMemory;

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
                        //聊天记忆 内存
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
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
}