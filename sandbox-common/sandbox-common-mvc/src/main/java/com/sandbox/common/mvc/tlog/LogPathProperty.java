package com.sandbox.common.mvc.tlog;

import ch.qos.logback.core.PropertyDefinerBase;

/**
 * Logback 动态日志路径，按 OS 自动适配。
 *
 * <ul>
 *   <li>Mac → {user.home}/logs</li>
 *   <li>Windows → logs</li>
 *   <li>Linux → /{user.name}/logs</li>
 * </ul>
 *
 * <p>在 logback-spring.xml 中：
 * <pre>
 * &lt;define name="logPath" class="com.sandbox.common.mvc.tlog.LogPathProperty"/&gt;
 * </pre>
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