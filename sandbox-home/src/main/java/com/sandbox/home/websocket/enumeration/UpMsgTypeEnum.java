package com.sandbox.home.websocket.enumeration;

import lombok.Getter;

/**
 * 设备上行消息类型枚举
 * <p>定义设备主动上报给平台的消息类型，编码规则：
 * <ul>
 *   <li>1xx - 连接管理</li>
 *   <li>2xx - 心跳保活</li>
 *   <li>3xx - 数据上报</li>
 *   <li>5xx - 响应反馈</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-03-19
 */
@Getter
public enum UpMsgTypeEnum {

    // ========== 连接管理类 (1xx) ==========
    CONN(101, "连接认证请求"),
    DIS_CONN(102, "断开连接通知"),

    // ========== 心跳保活类 (2xx) ==========
    HEARTBEAT(201, "心跳请求"),

    // ========== 数据上报类 (3xx) ==========
    SENSOR_DATA(301, "传感器数据上报"),
    DEVICE_STATUS(302, "设备状态上报"),
    EVENT(303, "事件上报"),
    ALARM(304, "告警上报"),

    // ========== 音频数据上报类 (4xx) ==========
    AUDIO_STREAM(400, "音频上报"),
    AUDIO_STREAM_CONTROL(401, "音频流控制（预留）"),
    AUDIO_STREAM_START(402, "音频流开始通知（开发测试用）"),
    AUDIO_STREAM_STOP(403, "音频流停止通知"),

    // ========== 响应反馈类 (5xx) ==========
    COMMAND_RESPONSE(501, "命令执行响应"),
    CONFIG_RESPONSE(502, "配置查询响应"),
    GENERAL_RESPONSE(599, "通用响应");

    private final int code;
    private final String description;

    UpMsgTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UpMsgTypeEnum fromValue(Integer code) {
        if (code == null) return null;
        for (UpMsgTypeEnum type : values()) {
            if (type.code == code) return type;
        }
        return null;
    }

    public static String getDescriptionByValue(Integer code) {
        UpMsgTypeEnum type = fromValue(code);
        return type != null ? type.description : "未知上行消息类型(" + code + ")";
    }

    public static boolean isValid(Integer code) {
        return fromValue(code) != null;
    }

    public boolean isConnectionType() {
        return code >= 100 && code < 200;
    }

    public boolean isHeartbeatType() {
        return code >= 200 && code < 300;
    }

    public boolean isReportType() {
        return code >= 300 && code < 400;
    }

    public boolean isResponseType() {
        return code >= 500 && code < 600;
    }
}