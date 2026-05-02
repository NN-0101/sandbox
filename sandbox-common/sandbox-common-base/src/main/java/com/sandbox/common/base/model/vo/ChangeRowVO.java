package com.sandbox.common.base.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 写操作返回的受影响行数
 *
 * <p>用于 INSERT/UPDATE/DELETE 操作的统一响应
 *
 * @author 0101
 * @since 2026-03-18
 */
@Data
public class ChangeRowVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 受影响行数
     */
    private Integer changeRow;

    /**
     * 构建受影响行数对象
     *
     * @param changeRow 受影响行数
     */
    public static ChangeRowVO changeRow(int changeRow) {
        ChangeRowVO vo = new ChangeRowVO();
        vo.setChangeRow(changeRow);
        return vo;
    }
}