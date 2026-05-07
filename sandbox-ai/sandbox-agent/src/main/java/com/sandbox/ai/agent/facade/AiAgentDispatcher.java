package com.sandbox.ai.agent.facade;

import com.sandbox.ai.agent.core.AiAgent;
import com.sandbox.ai.agent.enumeration.AgentTypeEnum;
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
 * AI Agent 调度门面
 * <p>
 * 根据业务类型统一调度不同的 Agent 实现。
 * Agent 自身通过组合 Skill 管理提示词和工具，调度器不再负责 prompt 注入。
 * </p>
 *
 * @author 0101
 * @since 2026/04/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAgentDispatcher {

    private final Map<AgentTypeEnum, AiAgent> agentStrategyMap;

    /** 发送流式消息 */
    public Flux<String> sendMessageStream(AiMessageRequest request) {
        request.validate();

        AiAgent handler = getHandler(request.getChatType());
        String conversationId = resolveConversationId(request);

        log.info("业务 [{}] 调用 [{}] 处理会话 [{}]",
                request.getChatType().getValue(),
                request.getChatType().getDescription(),
                conversationId);

        return handler.execute(conversationId, request.getMessage())
                .doOnComplete(() -> log.info("会话 [{}] 对话完成", conversationId))
                .doOnError(error -> log.error("会话 [{}] 对话异常", conversationId, error));
    }

    /** 发送消息并收集完整响应（非流式） */
    public Mono<AiMessageResponse> sendMessage(AiMessageRequest request) {
        return sendMessageStream(request)
                .collectList()
                .map(chunks -> {
                    String fullContent = String.join("", chunks);
                    return AiMessageResponse.success(fullContent, request.getConversationId());
                })
                .onErrorResume(error -> Mono.just(AiMessageResponse.error(error.getMessage())));
    }

    /** 发送流式消息（带元数据） */
    public Flux<AiStreamChunk> sendMessageStreamWithMetadata(AiMessageRequest request) {
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

    /** 解析会话ID，不存在则生成新的 */
    protected String resolveConversationId(AiMessageRequest request) {
        String conversationId = request.getConversationId();

        if (StringUtils.isEmpty(conversationId)) {
            conversationId = UUID.randomUUID().toString().replace("-", "");
            request.setConversationId(conversationId);
            log.info("创建新会话 [{}] 用于业务 [{}]", conversationId, request.getChatType().getValue());
        }
        return conversationId;
    }

    /** 获取对应的 Agent 处理器 */
    private AiAgent getHandler(AgentTypeEnum chatType) {
        AiAgent handler = agentStrategyMap.get(chatType);
        if (handler == null) {
            throw new RuntimeException("AI聊天类型不支持: " + chatType);
        }
        return handler;
    }

    /** 是否支持指定聊天类型 */
    public boolean supports(AgentTypeEnum chatType) {
        return agentStrategyMap.containsKey(chatType);
    }

    /** 获取所有支持的聊天类型 */
    public AgentTypeEnum[] getSupportedChatTypes() {
        return agentStrategyMap.keySet().toArray(new AgentTypeEnum[0]);
    }
}
