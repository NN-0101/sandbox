package com.sandbox.home.enumeration;

import lombok.Getter;

/**
 * 逻辑删除标记枚举
 * <p>
 * NO(0) 未删除，YES(1) 已删除。可与 MyBatis-Plus 逻辑删除功能配合使用，
 * 在 BaseModel 的 delFlag 字段中使用，避免硬编码数字。
 *
 * @author 0101
 * @since 2026-03-18
 */
@Getter
public enum DelFlagEnum {

    /**
     * 未删除
     */
    NO(0, "未删除"),

    /**
     * 已删除
     */
    YES(1, "已删除");

    /**
     * 删除状态代码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String description;

    DelFlagEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}