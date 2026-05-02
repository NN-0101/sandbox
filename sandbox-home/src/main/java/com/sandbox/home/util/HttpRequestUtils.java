package com.sandbox.home.util;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONObject;
import com.sandbox.home.enumeration.ResponseCodeEnum;
import com.sandbox.home.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * HTTP 请求工具类
 * <p>
 * 基于 Hutool HttpRequest 封装 GET/POST/表单请求，统一返回 JSONObject。
 * 请求失败抛出 BusinessException，并记录请求-响应日志。默认超时 3000ms。
 *
 * @author 0101
 * @since 2026-03-12
 */
@Slf4j
public class HttpRequestUtils {

    private static final int DEFAULT_TIMEOUT = 3000;

    private HttpRequestUtils() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== POST ====================

    public static JSONObject post(String url) {
        try {
            String body = HttpRequest.post(url).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("post request url:{}, result:{}", url, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    public static JSONObject post(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url)
                    .body(JSONObject.toJSONString(params))
                    .timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("post request url:{}, params:{}, result:{}", url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    public static JSONObject post(String url, Map<String, String> headers, String jsonParam) {
        return post(url, headers, jsonParam, DEFAULT_TIMEOUT);
    }

    public static JSONObject post(String url, Map<String, String> headers, String jsonParam, int timeout) {
        try {
            String body = HttpRequest.post(url)
                    .addHeaders(headers).body(jsonParam).timeout(timeout).execute().body();
            log.info("post request url:{}, headers:{}, params:{}, result:{}",
                    url, JSONObject.toJSONString(headers), jsonParam, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    public static JSONObject postForm(String url, Map<String, String> headers, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url)
                    .addHeaders(headers).form(params).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("post form request url:{}, headers:{}, params:{}, result:{}",
                    url, JSONObject.toJSONString(headers), JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post form request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    public static JSONObject postForm(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url).form(params).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("post form request url:{} ,params:{}, result:{}",
                    url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post form request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /** 发送 POST 请求，失败返回 null 而不抛异常 */
    public static JSONObject sendMessagePost(String url, String jsonParam) {
        try {
            String body = HttpRequest.post(url).body(jsonParam).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("post request url:{}, params:{}, result:{}", url, jsonParam, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post request error url:{}, errorMessage:", url, e);
            return null;
        }
    }

    // ==================== GET ====================

    public static JSONObject get(String url) {
        try {
            String body = HttpRequest.get(url).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("get request url:{}, result:{}", url, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    public static JSONObject get(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.get(url).form(params).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("get request url:{}, params:{}, result:{}",
                    url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    public static JSONObject get(String url, Map<String, String> headers, Map<String, Object> params) {
        return get(url, headers, params, DEFAULT_TIMEOUT);
    }

    public static JSONObject get(String url, Map<String, String> headers, Map<String, Object> params, int timeout) {
        try {
            String body = HttpRequest.get(url)
                    .addHeaders(headers).form(params).timeout(timeout).execute().body();
            log.info("get request url:{}, headers:{}, params:{}, result:{}",
                    url, JSONObject.toJSONString(headers), JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /** GET 请求，参数直接拼接到 URL 上 */
    public static JSONObject getParamUrl(String url, Map<String, String> headers, Map<String, Object> params) {
        try {
            String result = HttpRequest.get(url + paramUrl(params))
                    .addHeaders(headers).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("get request url:{}, headers:{}, params:{}, result:{}",
                    url, JSONObject.toJSONString(headers), JSONObject.toJSONString(params), JSONObject.toJSONString(result));
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("get request error url:{}, errorMessage:", url, e);
            throw new BusinessException(ResponseCodeEnum.HTTP_REQUEST_EXCEPTION);
        }
    }

    /** 将 Map 参数拼接为 URL 查询字符串（?key=value&...） */
    public static String paramUrl(Map<String, Object> param) {
        if (param == null || param.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("?");
        for (Map.Entry<String, Object> map : param.entrySet()) {
            stringBuilder.append(map.getKey()).append("=").append(map.getValue());
            stringBuilder.append("&");
        }
        String path = stringBuilder.toString();
        return path.substring(0, path.length() - 1);
    }
}