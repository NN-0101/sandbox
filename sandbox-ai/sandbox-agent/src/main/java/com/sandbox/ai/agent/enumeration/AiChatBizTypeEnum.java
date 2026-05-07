package com.sandbox.ai.agent.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * AI 聊天业务类型枚举，区分不同聊天业务场景
 *
 * @author 0101
 * @since 2026/03/18
 */
@Getter
public enum AiChatBizTypeEnum {

    USER_TALK("user-talk", "用户聊天"),

    ;

    private final String value;
    private final String description;

    AiChatBizTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值获取描述，找不到抛出 NoSuchElementException
     */
    public static String getDescriptionByValue(String value) {
        return Arrays.stream(values())
                .filter(x -> x.getValue().equals(value))
                .findFirst()
                .map(AiChatBizTypeEnum::getDescription)
                .orElseThrow(() -> new NoSuchElementException("没有相关业务！"));
    }

    /**
     * 根据值获取枚举对象，找不到抛出 NoSuchElementException
     */
    public static AiChatBizTypeEnum getAiTypeEnum(String value) {
        return Arrays.stream(values())
                .filter(x -> x.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("没有相关业务！"));
    }
}