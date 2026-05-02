package com.sandbox.home.exception;


import com.sandbox.home.enumeration.ResponseCodeEnum;

/**
 * 业务异常
 * <p>
 * 用于业务逻辑中的可预期错误（参数校验失败、资源不存在、权限不足等）。
 * 推荐使用 ResponseCodeEnum 构造，由 GlobalExceptionHandler 统一处理。
 *
 * @author 0101
 * @see AbstractException
 * @see ResponseCodeEnum
 * @since 2026-03-12
 */
public class BusinessException extends AbstractException {

    public BusinessException(ResponseCodeEnum responseCodeEnum) {
        super(responseCodeEnum.getCode(), responseCodeEnum.getDescription());
    }

    public BusinessException(ResponseCodeEnum responseCodeEnum, Throwable cause) {
        super(responseCodeEnum.getCode(), responseCodeEnum.getDescription(), cause);
    }

    protected BusinessException(int code, String message) {
        super(code, message);
    }

    protected BusinessException(int code, Throwable cause) {
        super(code, cause);
    }

    protected BusinessException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }
}