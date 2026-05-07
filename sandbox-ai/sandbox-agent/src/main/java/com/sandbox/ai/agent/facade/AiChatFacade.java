package com.sandbox.ai.agent.facade;

import com.sandbox.ai.agent.chat.BaseChatMessage;
import com.sandbox.ai.agent.config.BusinessConfig;
import com.sandbox.ai.agent.enumeration.AiChatBizTypeEnum;
import com.sandbox.ai.agent.model.request.AiMessageRequest;
import com.sandbox.ai.agent.model.response.AiMessageResponse;
import com.sandbox.ai.agent.model.response.AiStreamChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 聊天服务门面
 * <p>
 * 根据聊天类型枚举，统一调度不同的 BaseChatMessage 策略实现，
 * 作为全局调用的入口点。
 * 会话管理完全依赖 Spring AI 自动配置的 ChatMemory（默认基于内存）
 *
 * @author 0101
 * @since 2026/04/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatFacade {

    private final Map<AiChatBizTypeEnum, BaseChatMessage> chatMessageStrategyMap;
    private final BusinessConfig businessConfig;

    /**
     * 发送消息并返回流式响应
     *
     * @param request AI消息请求参数
     * @return AI 回复的响应式流
     */
    public Flux<String> sendMessageStream(AiMessageRequest request) {
        // 参数校验
        request.validate();

        // 获取对应的处理器
        BaseChatMessage handler = getHandler(request.getChatType());

        // 处理会话ID（如果没有则生成新的）
        String conversationId = resolveConversationId(request);

        // 获取提示词模板
        String promptTemplate = businessConfig.getPrompts().get(request.getChatType().getValue());

        log.info("业务 [{}] 调用 [{}] 处理会话 [{}] 的消息: {}",
                request.getChatType().getValue(),
                request.getChatType().getDescription(),
                conversationId,
                request.getMessage());

        return handler.sendMessage(promptTemplate, conversationId, request.getMessage())
                .doOnComplete(() -> log.info("会话 [{}] 对话完成", conversationId))
                .doOnError(error -> log.error("会话 [{}] 对话异常", conversationId, error));
    }

    /**
     * 发送消息并收集完整响应（非流式）
     *
     * @param request AI消息请求参数
     * @return 完整的AI响应
     */
    public Mono<AiMessageResponse> sendMessage(AiMessageRequest request) {
        return sendMessageStream(request)
                .collectList()
                .map(chunks -> {
                    String fullContent = String.join("", chunks);
                    log.info("会话 [{}] 完整响应收集完成，内容长度: {}",
                            request.getConversationId(), fullContent.length());
                    return AiMessageResponse.success(fullContent, request.getConversationId());
                })
                .onErrorResume(error -> {
                    log.error("业务 [{}] AI响应异常", request.getChatType().getValue(), error);
                    return Mono.just(AiMessageResponse.error(error.getMessage()));
                });
    }

    /**
     * 发送流式消息（带元数据的版本）
     *
     * @param request AI消息请求参数
     * @return 带序号的流式响应片段
     */
    public Flux<AiStreamChunk> sendMessageStreamWithMetadata(AiMessageRequest request) {
        // 先解析会话ID
        String conversationId = resolveConversationId(request);
        request.setConversationId(conversationId);

        AtomicInteger sequence = new AtomicInteger(0);

        return sendMessageStream(request)
                .map(content -> AiStreamChunk.builder()
                        .content(content)
                        .conversationId(conversationId)
                        .sequence(sequence.incrementAndGet())
                        .completed(false)
                        .build())
                .concatWithValues(AiStreamChunk.builder()
                        .content("")
                        .conversationId(conversationId)
                        .completed(true)
                        .build());
    }

    /**
     * 处理会话ID的创建
     * 如果请求中没有会话ID，则生成新的UUID作为会话ID
     * ChatMemory会根据这个conversationId自动管理对话历史
     *
     * @param request AI消息请求参数
     * @return 有效的会话ID
     */
    protected String resolveConversationId(AiMessageRequest request) {
        String conversationId = request.getConversationId();

        if (StringUtils.isEmpty(conversationId)) {
            // 生成新的会话ID
            conversationId = UUID.randomUUID().toString().replace("-", "");
            request.setConversationId(conversationId);
            log.info("创建新会话 [{}] 用于业务 [{}], 用户: [{}:{}]",
                    conversationId,
                    request.getChatType().getValue(),
                    request.getIdentityType(),
                    request.getIdentifier());
        } else {
            log.info("使用现有会话 [{}] 继续对话", conversationId);
        }

        return conversationId;
    }

    /**
     * 获取对应的处理器
     */
    private BaseChatMessage getHandler(AiChatBizTypeEnum chatType) {
        BaseChatMessage handler = chatMessageStrategyMap.get(chatType);
        if (handler == null) {
            log.error("未找到聊天类型 [{}] 对应的处理策略", chatType);
            throw new RuntimeException("AI聊天类型不支持: " + chatType);
        }
        return handler;
    }

    /**
     * 检查是否支持指定的聊天类型
     */
    public boolean supports(AiChatBizTypeEnum chatType) {
        return chatMessageStrategyMap.containsKey(chatType);
    }

    /**
     * 获取所有支持的聊天类型
     */
    public AiChatBizTypeEnum[] getSupportedChatTypes() {
        return chatMessageStrategyMap.keySet().toArray(new AiChatBizTypeEnum[0]);
    }
}