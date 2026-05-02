package com.sandbox.common.mvc.handler;

import com.sandbox.common.mvc.enumeration.ResponseCodeEnum;
import com.sandbox.common.mvc.exception.BusinessException;
import com.sandbox.common.mvc.model.vo.R;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>统一将各类异常转换为 R 响应体，避免异常信息直接暴露给前端
 *
 * @author 0101
 * @since 2026-03-12
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常 → 200 + 业务错误码
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<?>> handleBusiness(BusinessException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        return ResponseEntity.ok(new R<>(e.getCode(), e.getMessage()));
    }

    /**
     * 运行时异常 → 500
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<R<?>> handleRuntime(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new R<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    /**
     * 404 异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<R<?>> handle404(NoHandlerFoundException e) {
        log.warn("404: {} {}", e.getHttpMethod(), e.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new R<>(HttpStatus.NOT_FOUND.value(), "接口不存在"));
    }

    /**
     * @Valid 参数校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<?>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().stream()
                .map(org.springframework.context.support.DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数校验失败: {}", msg, e);
        return ResponseEntity.ok(new R<>(ResponseCodeEnum.PARAMETER_ERROR.getCode(), msg));
    }

    /**
     * 方法参数校验失败（@Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<?>> handleConstraintViolation(ConstraintViolationException e) {
        log.error("参数校验失败: {}", e.getMessage(), e);
        return ResponseEntity.ok(new R<>(ResponseCodeEnum.PARAMETER_ERROR.getCode(), e.getMessage()));
    }

    /**
     * HTTP 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("HTTP 方法不支持: {}", e.getMessage(), e);
        return ResponseEntity.ok(new R<>(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION.getCode(), e.getMessage()));
    }
}