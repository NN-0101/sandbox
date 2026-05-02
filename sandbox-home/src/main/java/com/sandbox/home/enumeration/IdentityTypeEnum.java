package com.sandbox.home.enumeration;

import lombok.Getter;

/**
 * @description: 身份标识枚举
 * @author: 0101
 * @create: 2026/04/30
 */
@Getter
public enum IdentityTypeEnum {

    USER("user", "用户"),

    DEVICE("device", "设备");

    private final String value;
    private final String desc;

    IdentityTypeEnum(String code, String desc) {
        this.value = code;
        this.desc = desc;
    }

    public String getValue() { return value; }
    public String getDesc() { return desc; }
}
