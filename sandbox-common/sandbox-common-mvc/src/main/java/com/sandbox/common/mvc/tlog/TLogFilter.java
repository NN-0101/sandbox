package com.sandbox.common.mvc.tlog;

import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.context.TLogContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * TLog 过滤器，提取/生成 TraceId，写入响应头，请求结束后清理上下文。
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
                TLogWebCommon.loadInstance().preHandle(request);
                response.addHeader(TLogConstants.TLOG_TRACE_KEY, TLogContext.getTraceId());
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            } finally {
                TLogWebCommon.loadInstance().afterCompletion();
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}