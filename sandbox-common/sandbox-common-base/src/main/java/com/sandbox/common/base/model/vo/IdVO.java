package com.sandbox.common.base.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新增操作返回的 ID 响应。
 *
 * <p>使用 String 类型，兼容雪花算法 19 位长整型，避免前端 JavaScript 精度丢失。
 *
 * <pre>
 * return R.success(IdVO.setResultId("1234567890123456789"));
 * </pre>
 *
 * @author 0101
 * @since 2026-03-18
 */
@Data
public class IdVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 ID，String 类型防止前端精度丢失 */
    private String id;

    public static IdVO setResultId(String id) {
        IdVO vo = new IdVO();
        vo.setId(id);
        return vo;
    }
}