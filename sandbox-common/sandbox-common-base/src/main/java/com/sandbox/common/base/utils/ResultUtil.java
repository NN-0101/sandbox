package com.sandbox.common.base.utils;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;

import java.util.Objects;

/**
 * HTTP 响应结果解析工具，配合 {@link HttpRequestUtils} 使用。
 *
 * <p>约定响应格式为 {@code {"code": "0", "msg": "success", "data": {...}}}。
 *
 * <pre>
 * JSONObject response = HttpRequestUtils.get("<a href="http://api.example.com/users">...</a>");
 * if (ResultUtil.isSuccess(response)) {
 *     UserDTO user = ResultUtil.getDataFieldObject(response, UserDTO.class);
 * }
 * </pre>
 *
 * @author 0101
 * @since 2026-03-12
 */
public class ResultUtil {

    private ResultUtil() {
    }

    /**
     * code = "0" 视为成功
     */
    public static boolean isSuccess(JSONObject result) {
        return result != null && Objects.equals(result.getString("code"), "0");
    }

    /**
     * 获取 data 字段（JSONObject）
     */
    public static JSONObject getJsonData(JSONObject result) {
        return isSuccess(result) ? result.getJSONObject("data") : null;
    }

    /**
     * 整个响应转目标对象
     */
    public static <T> T getDataObject(JSONObject result, Class<T> clazz) {
        return isSuccess(result) ? BeanUtil.copyProperties(result, clazz) : null;
    }

    /**
     * data 字段转目标对象
     */
    public static <T> T getDataFieldObject(JSONObject result, Class<T> clazz) {
        JSONObject data = getJsonData(result);
        return data != null ? BeanUtil.copyProperties(data, clazz) : null;
    }
}