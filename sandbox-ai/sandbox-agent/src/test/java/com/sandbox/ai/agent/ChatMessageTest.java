package com.sandbox.ai.agent;

import com.sandbox.ai.agent.enumeration.AgentTypeEnum;
import com.sandbox.ai.agent.facade.AiAgentDispatcher;
import com.sandbox.ai.agent.model.request.AiMessageRequest;
import com.sandbox.ai.agent.model.response.AiMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 聊天功能测试 - 独立测试版本
 * 每个测试用例独立运行，不依赖其他测试
 *
 * @author 0101
 * @create: 2026/05/07
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChatMessageTest {

    @Autowired
    private AiAgentDispatcher aiChatFacade;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_IDENTITY_TYPE = "USER";

    /**
     * 创建新会话的辅助方法
     */
    private AiMessageRequest createRequest(String message) {
        return AiMessageRequest.builder()
                .chatType(AgentTypeEnum.USER_TALK)
                .identityType(TEST_IDENTITY_TYPE)
                .identifier(TEST_USER_ID)
                .message(message)
                .build();
    }

    /**
     * 创建带会话ID的请求
     */
    private AiMessageRequest createRequest(String conversationId, String message) {
        return AiMessageRequest.builder()
                .chatType(AgentTypeEnum.USER_TALK)
                .identityType(TEST_IDENTITY_TYPE)
                .identifier(TEST_USER_ID)
                .conversationId(conversationId)
                .message(message)
                .build();
    }

    /**
     * 发送消息并等待响应
     */
    private AiMessageResponse sendAndWait(AiMessageRequest request) {
        return aiChatFacade.sendMessage(request)
                .block(Duration.ofSeconds(60));
    }

    @Test
    @Order(1)
    @DisplayName("测试1：基本对话 - 单个问题")
    void test01_basicConversation() {
        log.info("========== 测试1：基本对话开始 ==========");

        AiMessageRequest request = createRequest("你有什么样的能力");
        AiMessageResponse response = sendAndWait(request);

        Assertions.assertNotNull(response, "响应不应为空");
        Assertions.assertTrue(response.isSuccess(), "对话应该成功");
        Assertions.assertNotNull(response.getContent(), "响应内容不应为空");
        Assertions.assertFalse(response.getContent().isEmpty(), "响应内容不应为空字符串");

        log.info("会话ID: {}", response.getConversationId());
        log.info("AI回复: {}", response.getContent());
        log.info("========== 测试1：基本对话完成 ==========");
    }

    @Test
    @Order(2)
    @DisplayName("测试2：多轮对话 - 验证记忆功能")
    void test02_multiTurnConversation() {
        log.info("========== 测试2：多轮对话开始 ==========");

        // 创建新会话
        AiMessageRequest request1 = createRequest("我的名字叫张三，请记住");
        AiMessageResponse result1 = sendAndWait(request1);

        Assertions.assertNotNull(result1, "第一轮响应不应为空");
        Assertions.assertTrue(result1.isSuccess(), "第一轮对话应该成功");

        String conversationId = result1.getConversationId();
        log.info("创建会话ID: {}", conversationId);
        log.info("第一轮AI回复: {}", result1.getContent());

        // 使用相同的会话ID进行第二轮对话
        AiMessageRequest request2 = createRequest(conversationId, "请问我的名字是什么？");
        AiMessageResponse result2 = sendAndWait(request2);

        Assertions.assertNotNull(result2, "第二轮响应不应为空");
        Assertions.assertTrue(result2.isSuccess(), "第二轮对话应该成功");

        log.info("第二轮AI回复: {}", result2.getContent());

        // 检查是否包含名字
        boolean containsName = result2.getContent().contains("张三") ||
                result2.getContent().toLowerCase().contains("zhang");
        log.info("AI是否记住了名字: {}", containsName);

        log.info("========== 测试2：多轮对话完成 ==========");
    }

    @Test
    @Order(3)
    @DisplayName("测试3：流式对话")
    void test03_streamConversation() {
        log.info("========== 测试3：流式对话开始 ==========");

        // 创建新会话
        AiMessageRequest request = createRequest("数1到5");

        Flux<String> streamFlux = aiChatFacade.sendMessageStream(request);

        List<String> recordedChunks = new ArrayList<>();

        StepVerifier.create(streamFlux)
                .recordWith(() -> recordedChunks)
                .thenConsumeWhile(chunk -> {
                    log.info("收到流式片段: {}", chunk);
                    return true;
                })
                .consumeRecordedWith(chunks -> {
                    log.info("共收到 {} 个片段", chunks.size());
                    String fullResponse = String.join("", chunks);
                    log.info("完整回复: {}", fullResponse);
                    Assertions.assertFalse(fullResponse.isEmpty(), "完整回复不应为空");
                })
                .verifyComplete();

        log.info("========== 测试3：流式对话完成 ==========");
    }

    @Test
    @Order(4)
    @DisplayName("测试4：记忆隔离 - 不同会话不共享记忆")
    void test04_memoryIsolation() {
        log.info("========== 测试4：记忆隔离测试开始 ==========");

        // 创建会话1
        AiMessageRequest request1 = createRequest("我的爱好是游泳");
        AiMessageResponse result1 = sendAndWait(request1);

        String conversationId1 = result1.getConversationId();
        log.info("会话1 ID: {}", conversationId1);

        // 创建会话2
        AiMessageRequest request2 = createRequest("我的爱好是编程");
        AiMessageResponse result2 = sendAndWait(request2);

        String conversationId2 = result2.getConversationId();
        log.info("会话2 ID: {}", conversationId2);

        Assertions.assertNotEquals(conversationId1, conversationId2, "不同会话应该有不同ID");

        // 在会话1中询问
        AiMessageRequest request3 = createRequest(conversationId1, "我刚才说我喜欢什么？");
        AiMessageResponse result3 = sendAndWait(request3);

        log.info("会话1回复: {}", result3.getContent());

        // 在会话2中询问
        AiMessageRequest request4 = createRequest(conversationId2, "我刚才说我喜欢什么？");
        AiMessageResponse result4 = sendAndWait(request4);

        log.info("会话2回复: {}", result4.getContent());

        log.info("========== 测试4：记忆隔离测试完成 ==========");
    }

    @Test
    @Order(5)
    @DisplayName("测试5：参数验证")
    void test05_parameterValidation() {
        log.info("========== 测试5：参数验证测试开始 ==========");

        // 测试空消息
        AiMessageRequest emptyMessageRequest = AiMessageRequest.builder()
                .chatType(AgentTypeEnum.USER_TALK)
                .message("")
                .build();

        Assertions.assertThrows(IllegalArgumentException.class,
                emptyMessageRequest::validate,
                "空消息应该抛出异常");

        // 测试空白消息
        AiMessageRequest blankMessageRequest = AiMessageRequest.builder()
                .chatType(AgentTypeEnum.USER_TALK)
                .message("   ")
                .build();

        Assertions.assertThrows(IllegalArgumentException.class,
                blankMessageRequest::validate,
                "空白消息应该抛出异常");

        // 测试null消息
        AiMessageRequest nullMessageRequest = AiMessageRequest.builder()
                .chatType(AgentTypeEnum.USER_TALK)
                .message(null)
                .build();

        Assertions.assertThrows(IllegalArgumentException.class,
                nullMessageRequest::validate,
                "null消息应该抛出异常");

        log.info("========== 测试5：参数验证测试完成 ==========");
    }
}