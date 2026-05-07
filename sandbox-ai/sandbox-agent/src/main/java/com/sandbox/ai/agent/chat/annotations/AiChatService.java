package com.sandbox.ai.agent.chat.annotations;

import com.sandbox.ai.agent.enumeration.AiChatBizTypeEnum;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AI 聊天服务标记注解
 * <p>
 * 结合 @Qualifier，通过 AiChatTypeEnum 区分不同的 AI 服务实现类，
 * 用于 AiConfig 中构建策略映射。
 *
 * @author 0101
 * @since 2026/03/18
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface AiChatService {

    AiChatBizTypeEnum value();
}