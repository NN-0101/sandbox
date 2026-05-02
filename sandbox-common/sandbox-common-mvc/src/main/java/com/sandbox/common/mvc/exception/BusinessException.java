package com.sandbox.common.mvc.exception;

import com.sandbox.common.mvc.enumeration.ResponseCodeEnum;

/**
 * 业务异常
 *
 * <p>Service 层抛出可预料的业务错误时使用
 *
 * @author 0101
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