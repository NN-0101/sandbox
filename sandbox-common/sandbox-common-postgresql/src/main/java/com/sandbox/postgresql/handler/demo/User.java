package com.sandbox.postgresql.handler.demo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.sandbox.postgresql.handler.EnumTypeHandler;
import com.sandbox.postgresql.handler.PostgresArrayTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: 0101
 * @create: 2026/05/06
 */
@Data
@TableName(value = "t_user", autoResultMap = true)
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String phone;

    /**
     * JSONB - 元数据
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * JSONB - 自定义对象
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private UserProfile profile;

    /**
     * TEXT[] - 数组
     */
    @TableField(typeHandler = PostgresArrayTypeHandler.class)
    private List<String> tags;

    /**
     * 自定义枚举
     */
    @TableField(typeHandler = EnumTypeHandler.class)
    private UserStatus status;

    /**
     * PostGIS 点坐标
     */
//    @TableField(typeHandler = PostgisPointTypeHandler.class)
//    private Object location;

    /**
     * INET 类型（IP地址）
     */
    private String ipAddress;

    /**
     * 时间类型自动映射
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Data
    static class UserProfile {
        private String avatar;
        private String bio;
        private Integer age;
    }

    enum UserStatus {
        ACTIVE, INACTIVE, BANNED
    }
}



