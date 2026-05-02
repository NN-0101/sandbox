package com.sandbox.common.mvc.enumeration;

import lombok.Getter;

/**
 * 全局响应码枚举
 *
 * <p>0 = 成功，999999 = 系统异常，100001~100008 = 基础设施错误，业务错误码由各模块自行扩展
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
    JSON_PARSER_EXCEPTION(100008, "JSON解析异常");

    private final int code;
    private final String description;

    ResponseCodeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}