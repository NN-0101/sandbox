package com.sandbox.common.mvc.tlog;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * TLog 链路追踪配置。
 *
 * <p>注册最高优先级过滤器，自动为每个 HTTP 请求生成/传递 TraceId。
 *
 * @author 0101
 * @since 2026-03-12
 */
@Configuration
@ComponentScan(value = "com.yomahub.tlog")
public class LogConfig {

    @Bean
    public FilterRegistrationBean<TLogFilter> loggingFilter() {
        FilterRegistrationBean<TLogFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TLogFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}