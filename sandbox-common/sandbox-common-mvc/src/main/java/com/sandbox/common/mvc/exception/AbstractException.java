package com.sandbox.common.mvc.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常基类，强制携带错误码，便于全局异常处理器统一处理。
 *
 * <p>子类示例：
 * <pre>
 * public class BusinessException extends AbstractException {
 *     public BusinessException(ResponseCodeEnum e) {
 *         super(e.getCode(), e.getDescription());
 *     }
 * }
 * </pre>
 *
 * @author 0101
 * @since 2026-03-12
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 业务错误码 */
    private final int code;

    protected AbstractException(int code, String message) {
        super(message);
        this.code = code;
    }

    protected AbstractException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    protected AbstractException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}