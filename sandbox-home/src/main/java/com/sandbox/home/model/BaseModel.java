package com.sandbox.home.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * MyBatis-Plus 基础实体模型
 * <p>
 * 所有数据库实体类的基类，提供 id（雪花算法）、createDate、updateDate、delFlag 通用字段，
 * 继承 Model 支持 ActiveRecord 模式直接操作数据库。
 * <p>
 * 注意：子类泛型需指定为自身类型（如 class User extends BaseModel&lt;User&gt;）；
 * 逻辑删除需在 application.yml 中配置 mybatis-plus 全局逻辑删除规则。
 *
 * @param <T> 子实体类型
 * @author 0101
 * @since 2026-03-18
 */
@Setter
@Getter
public class BaseModel<T extends Model<?>> extends Model<T> {

    /**
     * 主键 ID，雪花算法自动生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    protected String id;

    /**
     * 创建时间
     */
    protected Date createDate;

    /**
     * 更新时间
     */
    protected Date updateDate;

    /**
     * 逻辑删除标记（0-正常，1-已删除）
     */
    protected String delFlag;
}