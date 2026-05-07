package com.sandbox.ai.agent.core.impl;

import com.sandbox.ai.agent.annotations.AiAgentType;
import com.sandbox.ai.agent.core.AiAgent;
import com.sandbox.ai.agent.enumeration.AgentTypeEnum;
import com.sandbox.ai.agent.skill.Skill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 数据库 Agent
 * <p>
 * 组合 DBSkill，专注于数据库查询操作。
 * </p>
 *
 * @author 0101
 * @since 2026/05/07
 */
@Slf4j
@Component
@AiAgentType(AgentTypeEnum.DB)
public class DBAgent implements AiAgent {

    private final ChatClient chatClient;
    private final Skill dbSkill;

    public DBAgent(ChatClient chatClient,
                   @Qualifier("dbSkill") Skill dbSkill) {
        this.chatClient = chatClient;
        this.dbSkill = dbSkill;
    }

    @Override
    public Flux<String> execute(String conversationId, String message) {
        return chatClient
                .prompt(dbSkill.getPrompt())
                .user(message)
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .tools(dbSkill.getTools().toArray())
                .stream()
                .content()
                .doOnNext(chunk -> log.trace("DBAgent [{}]: {}", conversationId, chunk))
                .doOnError(error -> log.error("DBAgent [{}] 失败", conversationId, error))
                .doOnComplete(() -> log.info("DBAgent [{}] 完成", conversationId));
    }
}
