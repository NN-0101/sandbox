package com.sandbox.home.exception.handler;

import com.sandbox.home.enumeration.ResponseCodeEnum;
import com.sandbox.home.exception.BusinessException;
import com.sandbox.home.model.R;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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
 * <p>
 * 集中捕获各类异常，转换为统一的 R 响应：
 * <ul>
 *   <li>BusinessException → 200 + 业务错误码</li>
 *   <li>参数校验异常 → 200 + PARAMETER_ERROR</li>
 *   <li>运行时异常 → 500</li>
 *   <li>404 / 请求方式不支持 → 对应状态码</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-03-12
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<R<?>> handler(BusinessException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        return new ResponseEntity<>(new R<>(e.getCode(), e.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<R<?>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new R<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<R<?>> handleNoHandlerFound(NoHandlerFoundException e) {
        String requestInfo = String.format("%s %s", e.getHttpMethod(), e.getRequestURL());
        log.warn("404 未找到: {}", requestInfo);
        return new ResponseEntity<>(
                new R<>(HttpStatus.NOT_FOUND.value(), "接口不存在: " + requestInfo),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<?>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("请求体参数校验失败: {}", errorMsg, e);
        return new ResponseEntity<>(
                new R<>(ResponseCodeEnum.PARAMETER_ERROR.getCode(), errorMsg),
                HttpStatus.OK);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<?>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("参数校验失败: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new R<>(ResponseCodeEnum.PARAMETER_ERROR.getCode(), e.getMessage()),
                HttpStatus.OK);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<?>> handleHttpMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("HTTP 请求方式不支持: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new R<>(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION.getCode(), e.getMessage()),
                HttpStatus.OK);
    }
}