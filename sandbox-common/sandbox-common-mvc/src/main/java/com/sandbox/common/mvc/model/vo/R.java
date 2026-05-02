package com.sandbox.common.mvc.model.vo;

import com.sandbox.common.mvc.enumeration.ResponseCodeEnum;
import com.yomahub.tlog.context.TLogContext;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一响应体，所有接口返回此格式。
 *
 * <pre>
 * R.success(data);                          // 成功带数据
 * R.success();                              // 成功无数据
 * R.fail(ResponseCodeEnum.PARAMETER_ERROR, null);  // 失败
 * R.fail(10001, "用户不存在", null);           // 自定义失败
 * </pre>
 *
 * @param <T> 响应数据类型
 * @author 0101
 * @since 2026-03-18
 */
@Getter
@Setter
public class R<T> {

    /** 状态码，0 = 成功，非 0 = 失败 */
    private int code;

    /** 提示信息 */
    private String msg;

    /** 响应数据 */
    private T data;

    /** 链路追踪 ID，自动从 TLog 获取 */
    private final String traceId = TLogContext.getTraceId();

    public R() {}

    public R(ResponseCodeEnum responseCodeEnum) {
        this.code = responseCodeEnum.getCode();
        this.msg = responseCodeEnum.getDescription();
    }

    public R(int code, String message) {
        this.code = code;
        this.msg = message;
    }

    public static <T> R<T> success(T data) {
        R<T> r = new R<>(ResponseCodeEnum.SUCCESS);
        r.setData(data);
        return r;
    }

    public static <T> R<T> success() {
        return new R<>(ResponseCodeEnum.SUCCESS);
    }

    public static <T> R<T> fail(ResponseCodeEnum responseCode, T data) {
        R<T> r = new R<>(responseCode);
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(int code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}