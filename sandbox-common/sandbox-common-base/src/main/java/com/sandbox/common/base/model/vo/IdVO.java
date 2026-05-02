package com.sandbox.common.base.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新增操作返回的 ID
 *
 * <p>使用 String 类型兼容雪花算法 19 位长整型，避免前端精度丢失
 *
 * @author 0101
 * @since 2026-03-18
 */
@Data
public class IdVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，String 类型防止前端精度丢失
     */
    private String id;

    /**
     * 构建 ID 对象
     *
     * @param id 主键 ID（字符串形式）
     */
    public static IdVO setResultId(String id) {
        IdVO vo = new IdVO();
        vo.setId(id);
        return vo;
    }
}