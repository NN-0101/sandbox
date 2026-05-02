package com.sandbox.common.mvc.tlog;

import ch.qos.logback.core.PropertyDefinerBase;

/**
 * Logback 动态日志路径
 *
 * <p>按操作系统自动适配日志输出目录：Mac→~/logs，Windows→logs，Linux→/用户名/logs
 *
 * @author 0101
 * @since 2026-03-12
 */
public class LogPathProperty extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        String osName = System.getProperty("os.name");
        String userName = System.getProperty("user.name");

        if (osName.startsWith("Mac OS")) {
            return System.getProperty("user.home") + "/logs";
        } else if (osName.startsWith("Windows")) {
            return "logs";
        } else {
            return String.format("/%s/logs", userName);
        }
    }
}