package com.sandbox.home.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常抽象基类
 * <p>
 * 继承 RuntimeException，强制子类提供错误码（code），确保系统中所有业务异常格式统一。
 * 构造方法均为 protected，子类应提供合适的构造方法。
 *
 * @author 0101
 * @see BusinessException
 * @since 2026-03-12
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 803863908956713716L;

    /**
     * 业务错误码
     */
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