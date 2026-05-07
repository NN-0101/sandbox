package com.sandbox.ai.agent.annotations;

import com.sandbox.ai.agent.enumeration.AgentTypeEnum;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AI Agent 类型标记注解
 * <p>
 * 结合 @Qualifier，通过 AgentTypeEnum 区分不同的 Agent 实现类，
 * 用于 AiConfig 中构建策略映射。
 *
 * @author 0101
 * @since 2026/03/18
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface AiAgentType {

    /**
     * Agent 的业务类型
     */
    AgentTypeEnum value();

    /**
     * Agent 的描述信息（可选）
     */
    String description() default "";

}