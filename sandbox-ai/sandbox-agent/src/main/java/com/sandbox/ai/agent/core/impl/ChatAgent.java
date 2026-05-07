package com.sandbox.ai.agent.core.impl;

import com.sandbox.ai.agent.annotations.AiAgentType;
import com.sandbox.ai.agent.core.AiAgent;
import com.sandbox.ai.agent.enumeration.AgentTypeEnum;
import com.sandbox.ai.agent.skill.Skill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 对话 Agent
 * <p>
 * 组合 ChatSkill + DBSkill，具备通用对话、数据库查询和 MCP 远程工具调用能力。
 * </p>
 *
 * <h3>Skill 组合</h3>
 * <ul>
 *   <li>ChatSkill — 通用对话提示词</li>
 *   <li>DBSkill  — 数据库查询提示词 + DBTool 本地工具</li>
 *   <li>MCP      — 远程工具回调（天气等）</li>
 * </ul>
 *
 * @author 0101
 * @since 2026/03/18
 */
@Slf4j
@Component
@AiAgentType(AgentTypeEnum.USER_TALK)
public class ChatAgent implements AiAgent {

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;
    private final Skill chatSkill;
    private final Skill dbSkill;

    public ChatAgent(ChatClient chatClient,
                     ToolCallbackProvider toolCallbackProvider,
                     @Qualifier("chatSkill") Skill chatSkill,
                     @Qualifier("dbSkill") Skill dbSkill) {
        this.chatClient = chatClient;
        this.toolCallbackProvider = toolCallbackProvider;
        this.chatSkill = chatSkill;
        this.dbSkill = dbSkill;
    }

    @Override
    public Flux<String> execute(String conversationId, String message) {
        log.info("ChatAgent [{}] 开始处理用户消息", conversationId);

        String prompt = mergePrompts();
        Object[] tools = collectTools();

        return chatClient
                .prompt(prompt)
                .user(message)
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .tools(tools)
                .toolCallbacks(toolCallbackProvider)
                .stream()
                .content()
                .doOnNext(chunk -> log.trace("ChatAgent [{}]: {}", conversationId, chunk))
                .doOnError(error -> log.error("ChatAgent [{}] 失败", conversationId, error))
                .doOnComplete(() -> log.info("ChatAgent [{}] 完成", conversationId));
    }

    /** 合并所有 Skill 的系统提示词 */
    private String mergePrompts() {
        return Stream.of(chatSkill, dbSkill)
                .map(Skill::getPrompt)
                .collect(Collectors.joining("\n"));
    }

    /** 收集所有 Skill 的本地工具 */
    private Object[] collectTools() {
        return Stream.of(chatSkill, dbSkill)
                .flatMap(s -> s.getTools().stream())
                .toArray();
    }
}
