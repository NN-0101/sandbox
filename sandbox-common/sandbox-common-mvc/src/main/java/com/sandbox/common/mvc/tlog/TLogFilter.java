package com.sandbox.common.mvc.tlog;

import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.context.TLogContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * TLog 过滤器
 *
 * <p>拦截所有请求，提取/生成 TraceId，写入响应头，请求结束后清理上下文
 *
 * @author 0101
 * @since 2026-03-12
 */
public class TLogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        if (servletRequest instanceof HttpServletRequest request
                && servletResponse instanceof HttpServletResponse response) {

            try {
                TLogWebCommon.loadInstance().preHandle(request);  // 提取或生成 TraceId
                response.addHeader(TLogConstants.TLOG_TRACE_KEY, TLogContext.getTraceId());  // 响应头返回 TraceId
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            } finally {
                TLogWebCommon.loadInstance().afterCompletion();  // 清理上下文
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}