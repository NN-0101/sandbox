package com.sandbox.common.mvc.tlog;

import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.core.rpc.TLogLabelBean;
import com.yomahub.tlog.core.rpc.TLogRPCHandler;
import jakarta.servlet.http.HttpServletRequest;

/**
 * TLog Web 端核心处理器（单例）
 *
 * <p>在请求前后分别调用 preHandle/afterCompletion，管理链路追踪上下文
 *
 * @author 0101
 * @since 2026-03-12
 */
public class TLogWebCommon extends TLogRPCHandler {

    private static volatile TLogWebCommon instance;

    private TLogWebCommon() {
    }

    /**
     * 双重检查锁获取单例
     */
    public static TLogWebCommon loadInstance() {
        if (instance == null) {
            synchronized (TLogWebCommon.class) {
                if (instance == null) {
                    instance = new TLogWebCommon();
                }
            }
        }
        return instance;
    }

    /**
     * 请求前置处理：从请求头提取链路信息并绑定到当前线程
     */
    public void preHandle(HttpServletRequest request) {
        String traceId = request.getHeader(TLogConstants.TLOG_TRACE_KEY);
        String spanId = request.getHeader(TLogConstants.TLOG_SPANID_KEY);
        String preIvkApp = request.getHeader(TLogConstants.PRE_IVK_APP_KEY);
        String preIvkHost = request.getHeader(TLogConstants.PRE_IVK_APP_HOST);
        String preIp = request.getHeader(TLogConstants.PRE_IP_KEY);

        processProviderSide(new TLogLabelBean(preIvkApp, preIvkHost, preIp, traceId, spanId));
    }

    /**
     * 请求后置处理：清理线程本地变量
     */
    public void afterCompletion() {
        cleanThreadLocal();
    }
}