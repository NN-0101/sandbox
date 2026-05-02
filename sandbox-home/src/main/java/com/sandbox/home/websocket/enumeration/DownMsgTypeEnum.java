package com.sandbox.home.websocket.enumeration;

import lombok.Getter;

/**
 * 平台下行设备消息类型枚举
 * <p>定义平台下发给设备的消息类型，编码规则：
 * <ul>
 *   <li>1xx - 控制命令</li>
 *   <li>2xx - 配置管理</li>
 *   <li>3xx - 固件升级</li>
 *   <li>4xx - 数据同步</li>
 *   <li>5xx - 系统管理</li>
 *   <li>6xx - AI交互</li>
 *   <li>9xx - 通用响应</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-03-19
 */
@Getter
public enum DownMsgTypeEnum {

    // ========== 控制命令类 (1xx) ==========
    DEVICE_REBOOT(101, "设备重启命令"),
    DEVICE_RESET(102, "设备复位命令"),
    SWITCH_CONTROL(103, "开关控制命令"),
    MODE_SWITCH(104, "模式切换命令"),

    // ========== 配置管理类 (2xx) ==========
    CONFIG_UPDATE(201, "配置更新命令"),
    CONFIG_QUERY(202, "配置查询命令"),

    // ========== 固件升级类 (3xx) ==========
    FIRMWARE_UPGRADE(301, "固件升级命令"),
    UPGRADE_STATUS_QUERY(302, "升级状态查询命令"),

    // ========== 数据同步类 (4xx) ==========
    IMMEDIATE_REPORT(401, "立即上报命令"),
    HISTORY_SYNC(402, "历史数据同步命令"),

    // ========== 系统管理类 (5xx) ==========
    TIME_SYNC(501, "时间同步命令"),
    LOG_UPLOAD(502, "日志上传命令"),

    // ========== AI交互类 (6xx) ==========
    TTS_STREAM_CHUNK(601, "TTS流式文本片段"),
    TTS_STREAM_END(602, "TTS流式输出结束"),
    AI_RESPONSE_ERROR(603, "AI响应错误"),

    // ========== 通用响应类 (9xx) ==========
    CONN_RESPONSE(901, "连接认证响应"),
    HEARTBEAT_RESPONSE(902, "心跳响应"),
    DATA_REPORT_RESPONSE(903, "数据上报响应"),
    GENERAL_RESPONSE(999, "通用响应");

    private final int code;
    private final String description;

    DownMsgTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DownMsgTypeEnum fromValue(Integer code) {
        if (code == null) return null;
        for (DownMsgTypeEnum type : values()) {
            if (type.code == code) return type;
        }
        return null;
    }

    public static String getDescriptionByValue(Integer code) {
        DownMsgTypeEnum type = fromValue(code);
        return type != null ? type.description : "未知下行消息类型(" + code + ")";
    }

    public static boolean isValid(Integer value) {
        return fromValue(value) != null;
    }

    public boolean isControlType() {
        return code >= 100 && code < 200;
    }

    public boolean isConfigType() {
        return code >= 200 && code < 300;
    }

    public boolean isUpgradeType() {
        return code >= 300 && code < 400;
    }

    public boolean isSyncType() {
        return code >= 400 && code < 500;
    }

    public boolean isSystemType() {
        return code >= 500 && code < 600;
    }

    public boolean isAiType() {
        return code >= 600 && code < 700;
    }

    public boolean isResponseType() {
        return code >= 900 && code < 1000;
    }
}