package com.sandbox.common.mvc.model.vo;

import com.sandbox.common.mvc.enumeration.ResponseCodeEnum;
import com.yomahub.tlog.context.TLogContext;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一响应体
 *
 * <p>所有接口统一返回此格式，包含状态码、消息、数据和链路追踪 ID
 *
 * @param <T> 响应数据类型
 * @author 0101
 * @since 2026-03-18
 */
@Getter
@Setter
public class R<T> {

    /**
     * 状态码，0 = 成功
     */
    private int code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 链路追踪 ID，自动从 TLog 获取
     */
    private final String traceId = TLogContext.getTraceId();

    public R() {
    }

    public R(ResponseCodeEnum responseCodeEnum) {
        this.code = responseCodeEnum.getCode();
        this.msg = responseCodeEnum.getDescription();
    }

    public R(int code, String message) {
        this.code = code;
        this.msg = message;
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> success(T data) {
        R<T> r = new R<>(ResponseCodeEnum.SUCCESS);
        r.setData(data);
        return r;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> R<T> success() {
        return new R<>(ResponseCodeEnum.SUCCESS);
    }

    /**
     * 失败响应（使用枚举）
     */
    public static <T> R<T> fail(ResponseCodeEnum responseCode, T data) {
        R<T> r = new R<>(responseCode);
        r.setData(data);
        return r;
    }

    /**
     * 失败响应（自定义错误码和消息）
     */
    public static <T> R<T> fail(int code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}