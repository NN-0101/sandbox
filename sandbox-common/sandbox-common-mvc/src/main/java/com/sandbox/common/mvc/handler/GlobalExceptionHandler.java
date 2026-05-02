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
 * 全局异常处理器，统一将异常转换为 {@link R} 返回。
 *
 * <ul>
 *   <li>BusinessException → 200 + 业务错误码</li>
 *   <li>参数校验异常 → 200 + PARAMETER_ERROR</li>
 *   <li>RuntimeException → 500</li>
 *   <li>404 / 请求方式不支持 → 对应状态码</li>
 * </ul>
 *
 * <p>启用 404 需要配置：
 * <pre>
 * spring.mvc.throw-exception-if-no-handler-found: true
 * spring.web.resources.add-mappings: false
 * </pre>
 *
 * @author 0101
 * @since 2026-03-12
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<?>> handleBusiness(BusinessException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        return ResponseEntity.ok(new R<>(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<R<?>> handleRuntime(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new R<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<R<?>> handle404(NoHandlerFoundException e) {
        log.warn("404: {} {}", e.getHttpMethod(), e.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new R<>(HttpStatus.NOT_FOUND.value(), "接口不存在"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<?>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().stream()
                .map(org.springframework.context.support.DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数校验失败: {}", msg, e);
        return ResponseEntity.ok(new R<>(ResponseCodeEnum.PARAMETER_ERROR.getCode(), msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<?>> handleConstraintViolation(ConstraintViolationException e) {
        log.error("参数校验失败: {}", e.getMessage(), e);
        return ResponseEntity.ok(new R<>(ResponseCodeEnum.PARAMETER_ERROR.getCode(), e.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("HTTP 方法不支持: {}", e.getMessage(), e);
        return ResponseEntity.ok(new R<>(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION.getCode(), e.getMessage()));
    }
}