package com.sandbox.common.base.utils;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;

import java.util.Objects;

/**
 * HTTP 响应解析工具
 *
 * <p>配合 HttpRequestUtils 使用，解析统一格式响应：{"code": "0", "msg": "...", "data": {...}}
 *
 * @author 0101
 * @since 2026-03-12
 */
public class ResultUtil {

    private ResultUtil() {
    }

    /**
     * 判断响应是否成功（code = "0"）
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
     * 将整个响应转换为目标对象
     */
    public static <T> T getDataObject(JSONObject result, Class<T> clazz) {
        return isSuccess(result) ? BeanUtil.copyProperties(result, clazz) : null;
    }

    /**
     * 将 data 字段转换为目标对象
     */
    public static <T> T getDataFieldObject(JSONObject result, Class<T> clazz) {
        JSONObject data = getJsonData(result);
        return data != null ? BeanUtil.copyProperties(data, clazz) : null;
    }
}