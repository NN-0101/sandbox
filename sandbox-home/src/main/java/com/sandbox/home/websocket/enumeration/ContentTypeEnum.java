package com.sandbox.home.websocket.enumeration;

import lombok.Getter;

/**
 * 平台下行消息内容类型枚举
 * <p>标识下行消息中 content 字段的数据格式：
 * <ul>
 *   <li>0 - 纯文本</li>
 *   <li>1 - JSON对象</li>
 *   <li>2 - Base64编码的二进制数据</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-03-19
 */
@Getter
public enum ContentTypeEnum {

    TEXT(0, "纯文本"),
    JSON(1, "JSON对象"),
    BINARY(2, "二进制数据");

    private final int code;
    private final String description;

    ContentTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ContentTypeEnum fromCode(Integer code) {
        if (code == null) return null;
        for (ContentTypeEnum type : values()) {
            if (type.code == code) return type;
        }
        return null;
    }

    public static String getDescriptionByCode(Integer code) {
        ContentTypeEnum type = fromCode(code);
        return type != null ? type.description : "未知内容类型(" + code + ")";
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}