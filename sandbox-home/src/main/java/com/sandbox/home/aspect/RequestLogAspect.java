package com.sandbox.home.aspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求日志切面
 * <p>
 * 拦截所有 @RestController 方法，打印请求信息（URI、方法、请求头、参数、Body）和响应结果，
 * 并统计请求耗时。使用 ThreadLocal 存储开始时间，请求结束后清理。
 * <p>
 * 注意：生产环境需考虑日志量及敏感信息脱敏。
 *
 * @author 0101
 * @since 2026-03-12
 */
@Aspect
@Order(1)
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);

    private final ThreadLocal<Long> requestCostThreadLocal = new ThreadLocal<>();

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    private void controllerAspect() {}

    @Before(value = "controllerAspect()")
    public void methodBefore(JoinPoint joinPoint) {
        try {
            requestCostThreadLocal.set(System.currentTimeMillis());

            ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert requestAttributes != null;
            HttpServletRequest request = requestAttributes.getRequest();

            String uri = request.getRequestURI();
            String method = request.getMethod();

            Map<String, String> headerMap = new HashMap<>(16);
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headerMap.put(headerName, headerValue);
            }

            StringBuilder bodyBuilder = new StringBuilder();
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof HttpServletResponse || arg instanceof HttpServletRequest) {
                    continue;
                }
                if (arg instanceof MultipartFile) {
                    bodyBuilder.append(arg);
                } else {
                    bodyBuilder.append(JSONObject.toJSONString(arg));
                    bodyBuilder.append(",");
                }
            }
            if (bodyBuilder.length() <= 0) {
                bodyBuilder.append(",");
            }

            log.info("uri: {}  method:{}  params: {}  body: {}  headers: {}",
                    uri, method,
                    JSON.toJSONString(request.getParameterMap()),
                    bodyBuilder.substring(0, bodyBuilder.length() - 1),
                    JSONObject.toJSONString(headerMap));
        } catch (Exception e) {
            log.error("请求日志切面前置处理异常:", e);
        }
    }

    @AfterReturning(returning = "o", pointcut = "controllerAspect()")
    public void methodAfterReturning(Object o) {
        try {
            ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert requestAttributes != null;
            HttpServletRequest request = requestAttributes.getRequest();
            String uri = request.getRequestURI();

            String pretty = JSON.toJSONString(
                    JSONObject.parseObject(JSON.toJSONString(o)),
                    JSONWriter.Feature.PrettyFormat);

            log.info("uri: {}  result: \n{}", request.getRequestURI(), pretty);

            Long startTime = requestCostThreadLocal.get();
            if (startTime != null) {
                log.info("uri: {}  请求耗时: {} ms", uri, System.currentTimeMillis() - startTime);
            }
        } catch (Exception e) {
            log.error("请求日志切面后置处理异常:", e);
        } finally {
            requestCostThreadLocal.remove();
        }
    }
}