package com.sandbox.common.mvc.aspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
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
 * Controller 请求日志切面
 *
 * <p>自动打印所有 @RestController 的请求参数、响应结果和耗时
 *
 * @author 0101
 * @since 2026-03-12
 */
@Aspect
@Order(1)
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();  // 记录请求开始时间

    // 拦截所有 @RestController 标注的类
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    private void controller() {
    }

    /**
     * 前置通知：记录请求参数和请求头
     */
    @Before("controller()")
    public void before(JoinPoint joinPoint) {
        try {
            startTime.set(System.currentTimeMillis());
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;
            HttpServletRequest request = attrs.getRequest();

            // 收集请求头
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> names = request.getHeaderNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                headers.put(name, request.getHeader(name));
            }

            // 收集请求体（过滤 HttpServletRequest/HttpServletResponse）
            StringBuilder body = new StringBuilder();
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof HttpServletResponse || arg instanceof HttpServletRequest) continue;
                body.append(arg instanceof MultipartFile ? arg.toString() : JSONObject.toJSONString(arg));
                body.append(",");
            }
            if (body.isEmpty()) body.append(",");

            log.info("uri: {}  method:{}  params: {}  body: {}  headers: {}",
                    request.getRequestURI(), request.getMethod(),
                    JSON.toJSONString(request.getParameterMap()),
                    body.substring(0, body.length() - 1),
                    JSONObject.toJSONString(headers));
        } catch (Exception e) {
            log.error("请求日志前置处理异常", e);
        }
    }

    /**
     * 后置通知：记录响应结果和耗时
     */
    @AfterReturning(returning = "result", pointcut = "controller()")
    public void afterReturning(Object result) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;
            String uri = attrs.getRequest().getRequestURI();

            // 格式化输出响应
            log.info("uri: {}  result: \n{}", uri,
                    JSON.toJSONString(JSONObject.parseObject(JSON.toJSONString(result)),
                            JSONWriter.Feature.PrettyFormat));

            Long start = startTime.get();
            if (start != null) {
                log.info("uri: {}  请求耗时: {} ms", uri, System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            log.error("请求日志后置处理异常", e);
        } finally {
            startTime.remove();
        }
    }
}