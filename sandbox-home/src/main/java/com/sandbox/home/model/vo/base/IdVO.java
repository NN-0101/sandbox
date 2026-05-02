package com.sandbox.home.model.vo.base;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 主键 ID 响应 VO
 * <p>
 * 用于新增操作后返回生成的 ID。ID 使用 String 类型，避免雪花算法长整型在前端精度丢失。
 *
 * @author 0101
 * @since 2026-03-18
 */
@Data
public class IdVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    public static IdVO setResultId(String id) {
        IdVO idResponse = new IdVO();
        idResponse.setId(id);
        return idResponse;
    }
}