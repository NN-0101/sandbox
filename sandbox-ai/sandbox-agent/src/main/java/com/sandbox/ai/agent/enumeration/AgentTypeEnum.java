package com.sandbox.ai.agent.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Agent 业务类型枚举
 *
 * @author 0101
 * @since 2026/03/18
 */
@Getter
public enum AgentTypeEnum {

    USER_TALK("user-talk", "用户聊天"),

    DB("db", "数据库操作"),

    MCP_WEATHER("mcp-weather", "mcp天气查询"),
    ;

    private final String value;
    private final String description;

    AgentTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /** 根据值获取描述 */
    public static String getDescriptionByValue(String value) {
        return Arrays.stream(values())
                .filter(x -> x.getValue().equals(value))
                .findFirst()
                .map(AgentTypeEnum::getDescription)
                .orElseThrow(() -> new NoSuchElementException("没有相关业务！"));
    }

    /** 根据值获取枚举对象 */
    public static AgentTypeEnum getAiTypeEnum(String value) {
        return Arrays.stream(values())
                .filter(x -> x.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("没有相关业务！"));
    }
}