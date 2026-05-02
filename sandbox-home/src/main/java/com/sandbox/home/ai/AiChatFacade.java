package com.sandbox.home.ai;

import com.sandbox.home.ai.chat.BaseChatMessage;
import com.sandbox.home.ai.enumeration.AiChatBizTypeEnum;
import com.sandbox.home.config.BusinessConfig;
import com.sandbox.home.entity.AiConversationDO;
import com.sandbox.home.enumeration.ResponseCodeEnum;
import com.sandbox.home.exception.BusinessException;
import com.sandbox.home.model.request.ai.AiMessageRequest;
import com.sandbox.home.model.response.ai.AiMessageResponse;
import com.sandbox.home.model.response.ai.AiStreamChunk;
import com.sandbox.home.service.AiConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 聊天服务门面
 * <p>
 * 根据聊天类型枚举，统一调度不同的 BaseChatMessage 策略实现，
 * 作为全局调用的入口点。
 *
 * @author 0101
 * @since 2026/04/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatFacade {

    private final Map<AiChatBizTypeEnum, BaseChatMessage> chatMessageStrategyMap;
    private final AiConversationService aiConversationService;
    private final BusinessConfig businessConfig;

    /**
     * 发送消息并返回流式响应
     *
     * @param request AI消息请求参数
     * @return AI 回复的响应式流
     * @throws BusinessException 如果请求参数无效或不支持的聊天类型
     */
    public Flux<String> sendMessageStream(AiMessageRequest request) {
        // 参数校验
        request.validate();

        // 获取对应的处理器
        BaseChatMessage handler = getHandler(request.getChatType());

        // 处理会话ID（创建或验证）
        String conversationId = resolveConversationId(request);

        // 获取提示词模板
        String promptTemplate = businessConfig.getPrompts().get(request.getChatType().getValue());

        log.info("业务 [{}] 调用 [{}] 处理会话 [{}] 的消息",
                request.getChatType().getValue(),
                request.getChatType().getDescription(),
                conversationId);

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
                    log.info("会话 [{}] 完整响应收集完成", request.getConversationId());
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
        // 先解析会话ID（确保在流开始前完成数据库操作）
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
     * 发送消息（批量消息版本）
     *
     * @param requests AI消息请求参数列表
     * @return 多个AI响应的流式合并结果
     */
    public Flux<String> sendMessageBatch(Flux<AiMessageRequest> requests) {
        return requests.flatMap(this::sendMessageStream);
    }

    /**
     * 处理会话ID的创建或验证
     *
     * @param request AI消息请求参数
     * @return 有效的会话ID
     * @throws BusinessException 如果会话不存在或身份不匹配
     */
    protected String resolveConversationId(AiMessageRequest request) {
        String conversationId = request.getConversationId();

        if (StringUtils.isEmpty(conversationId)) {
            // 创建新会话
            AiConversationDO entity = new AiConversationDO();
            entity.setName(truncateConversationName(request.getMessage()));
            entity.setIdentityType(request.getIdentityType());
            entity.setIdentifier(request.getIdentifier());
            entity.setBusiness(request.getChatType().getValue());
            aiConversationService.getBaseMapper().insert(entity);
            conversationId = entity.getId();

            log.info("创建新会话 [{}] 用于业务 [{}]", conversationId, request.getChatType().getValue());
        } else {
            // 验证现有会话
            AiConversationDO entity = aiConversationService.getBaseMapper().selectById(conversationId);
            if (entity == null) {
                throw new BusinessException(ResponseCodeEnum.AI_CONVERSATION_NOT_EXITS);
            }
            if (!Objects.equals(entity.getIdentifier(), request.getIdentifier())) {
                log.warn("会话 [{}] 身份标识不匹配: {} vs {}", conversationId, entity.getIdentifier(), request.getIdentifier());
                throw new BusinessException(ResponseCodeEnum.AI_CONVERSATION_NOT_EXITS);
            }

            log.info("使用现有会话 [{}] 继续对话", conversationId);
        }

        // 更新请求中的会话ID
        request.setConversationId(conversationId);
        return conversationId;
    }

    /**
     * 截断会话名称（避免过长）
     */
    private String truncateConversationName(String message) {
        if (StringUtils.isEmpty(message)) {
            return "新对话";
        }
        return message.length() > 50 ? message.substring(0, 50) + "..." : message;
    }

    /**
     * 获取对应的处理器
     */
    private BaseChatMessage getHandler(AiChatBizTypeEnum chatType) {
        BaseChatMessage handler = chatMessageStrategyMap.get(chatType);
        if (handler == null) {
            log.error("未找到聊天类型 [{}] 对应的处理策略", chatType);
            throw new BusinessException(ResponseCodeEnum.AI_CHAT_TYPE_NOT_SUPPORTED);
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