package com.sandbox.home.util;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;

import java.util.Objects;

/**
 * 接口响应结果处理工具
 * <p>
 * 判断响应是否成功（code="0"），提取 data 字段，或将响应转换为目标对象。
 * 与 HttpRequestUtils 配合使用，约定响应格式：{code, msg, data}。
 *
 * @author 0101
 * @see HttpRequestUtils
 * @since 2026-03-12
 */
public class ResultUtil {

    private ResultUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isSuccess(JSONObject result) {
        return result != null && Objects.equals(result.getString("code"), "0");
    }

    public static JSONObject getJsonData(JSONObject result) {
        if (isSuccess(result)) {
            return result.getJSONObject("data");
        }
        return null;
    }

    public static <T> T getDataObject(JSONObject result, Class<T> clazz) {
        if (isSuccess(result)) {
            return BeanUtil.copyProperties(result, clazz);
        }
        return null;
    }

    public static <T> T getDataFieldObject(JSONObject result, Class<T> clazz) {
        JSONObject data = getJsonData(result);
        if (data != null) {
            return BeanUtil.copyProperties(data, clazz);
        }
        return null;
    }
}