package com.sandbox.common.base.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 写操作返回的受影响行数。
 *
 * <pre>
 * // 更新
 * return R.success(ChangeRowVO.changeRow(count));
 *
 * // 批量操作
 * return R.success(ChangeRowVO.changeRow(5));
 * </pre>
 *
 * @author 0101
 * @since 2026-03-18
 */
@Data
public class ChangeRowVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 受影响行数 */
    private Integer changeRow;

    public static ChangeRowVO changeRow(int changeRow) {
        ChangeRowVO vo = new ChangeRowVO();
        vo.setChangeRow(changeRow);
        return vo;
    }
}