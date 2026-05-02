package com.sandbox.home.model;

import com.sandbox.home.enumeration.ResponseCodeEnum;
import com.yomahub.tlog.context.TLogContext;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一响应对象
 * <p>
 * 系统所有接口的统一响应格式，包含 code（状态码，0 表示成功）、msg（提示信息）、
 * data（业务数据）和 traceId（自动从 TLogContext 获取的链路追踪 ID）。
 * 通过静态工厂方法快速创建成功/失败响应。
 *
 * @param <T> 响应数据类型
 * @author 0101
 * @see ResponseCodeEnum
 * @since 2026-03-18
 */
@Getter
@Setter
public class R<T> {

    /**
     * 状态码，0 表示成功
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
     * 链路追踪 ID，自动获取
     */
    private final String traceId = TLogContext.getTraceId();

    public R() {
    }

    public R(ResponseCodeEnum responseCodeEnum) {
        this.setCode(responseCodeEnum.getCode());
        this.setMsg(responseCodeEnum.getDescription());
    }

    public R(int code, String message) {
        this.setCode(code);
        this.setMsg(message);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> success(T data) {
        R<T> genericResponse = new R<>(ResponseCodeEnum.SUCCESS);
        genericResponse.setData(data);
        return genericResponse;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> R<T> success() {
        return new R<>(ResponseCodeEnum.SUCCESS);
    }

    /**
     * 失败响应（使用预定义枚举）
     */
    public static <T> R<T> fail(ResponseCodeEnum responseCode, T data) {
        R<T> r = new R<>(responseCode);
        r.setData(data);
        return r;
    }

    /**
     * 失败响应（自定义状态码和消息）
     */
    public static <T> R<T> fail(int code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}