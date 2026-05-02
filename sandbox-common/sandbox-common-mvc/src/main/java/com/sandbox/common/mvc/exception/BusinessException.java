package com.sandbox.common.mvc.exception;

import com.sandbox.common.mvc.enumeration.ResponseCodeEnum;

/**
 * 业务异常，用于 Service 层抛出可预料的错误。
 *
 * <pre>
 * throw new BusinessException(ResponseCodeEnum.PARAMETER_ERROR);
 * throw new BusinessException(ResponseCodeEnum.SQL_ERROR, e);
 * </pre>
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