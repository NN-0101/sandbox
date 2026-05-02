package com.sandbox.home.model.vo.base;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 受影响行数响应 VO
 * <p>
 * 用于更新/删除/批量操作后返回影响的数据行数。
 *
 * @author 0101
 * @since 2026-03-18
 */
@Data
public class ChangeRowVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer changeRow;

    public static ChangeRowVO changeRow(int changeRow) {
        ChangeRowVO changeRowResponse = new ChangeRowVO();
        changeRowResponse.setChangeRow(changeRow);
        return changeRowResponse;
    }
}