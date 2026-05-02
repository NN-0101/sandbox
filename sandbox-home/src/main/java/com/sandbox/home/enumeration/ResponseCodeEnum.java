package com.sandbox.home.enumeration;

import lombok.Getter;

/**
 * 全局响应码枚举
 * <p>
 * 0 表示成功，其他正数表示失败：100001-199999 系统级异常，100003 参数错误，100007 HTTP异常等。
 *
 * @author 0101
 * @since 2026-03-12
 */
@Getter
public enum ResponseCodeEnum {

    SUCCESS(0, "success"),
    SYSTEM_ERROR(999999, "系统异常"),
    INFRASTRUCTURE_ERROR(100001, "基础服务异常，请联系管理员"),
    SQL_ERROR(100002, "数据处理异常，请联系管理员"),
    PARAMETER_ERROR(100003, "参数错误"),
    TIMEOUT_EXCEPTION(100004, "调用超时"),
    ASYNC_SERVICE_EXCEPTION(100005, "服务异常"),
    SERVICE_EXCEPTION(100006, "服务异常"),
    HTTP_REQUEST_EXCEPTION(100007, "HTTP调用异常"),
    JSON_PARSER_EXCEPTION(100008, "JSON解析异常"),

    AI_CONVERSATION_NOT_EXITS(200000, "会话不存在"),
    AI_CHAT_TYPE_NOT_SUPPORTED(200001, "聊天类型不支持")




    ;

    private final int code;
    private final String description;

    ResponseCodeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}