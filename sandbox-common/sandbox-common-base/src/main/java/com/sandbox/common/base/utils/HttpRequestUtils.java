package com.sandbox.common.base.utils;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * HTTP 请求工具
 *
 * <p>基于 Hutool，支持 GET/POST/表单请求，默认超时 3 秒，失败返回 null
 *
 * @author 0101
 * @since 2026-03-12
 */
public class HttpRequestUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestUtils.class);
    private static final int DEFAULT_TIMEOUT = 3000;

    private HttpRequestUtils() {
    }

    // ==================== POST ====================

    /**
     * 发送 POST 请求（无参）
     */
    public static JSONObject post(String url) {
        try {
            String body = HttpRequest.post(url).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("post url:{} result:{}", url, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post error url:{}", url, e);
            return null;
        }
    }

    /**
     * 发送 POST 请求（JSON 参数）
     *
     * @param url    请求地址
     * @param params JSON 格式参数
     */
    public static JSONObject post(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url)
                    .body(JSONObject.toJSONString(params))
                    .timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("post url:{} params:{} result:{}", url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post error url:{}", url, e);
            return null;
        }
    }

    /**
     * 发送 POST 请求（自定义请求头 + JSON 参数，默认超时）
     */
    public static JSONObject post(String url, Map<String, String> headers, String jsonParam) {
        return post(url, headers, jsonParam, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 POST 请求（自定义请求头 + JSON 参数 + 超时时间）
     */
    public static JSONObject post(String url, Map<String, String> headers, String jsonParam, int timeout) {
        try {
            String body = HttpRequest.post(url)
                    .addHeaders(headers).body(jsonParam).timeout(timeout).execute().body();
            log.info("post url:{} headers:{} params:{} result:{}",
                    url, JSONObject.toJSONString(headers), jsonParam, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post error url:{}", url, e);
            return null;
        }
    }

    /**
     * 发送表单 POST 请求
     */
    public static JSONObject postForm(String url, Map<String, String> headers, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url)
                    .addHeaders(headers).form(params).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("postForm url:{} headers:{} params:{} result:{}",
                    url, JSONObject.toJSONString(headers), JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("postForm error url:{}", url, e);
            return null;
        }
    }

    /**
     * 发送表单 POST 请求（无请求头）
     */
    public static JSONObject postForm(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.post(url).form(params).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("postForm url:{} params:{} result:{}",
                    url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("postForm error url:{}", url, e);
            return null;
        }
    }

    /**
     * 发送 POST 消息（失败不抛异常）
     * 适用于消息推送等不严格要求成功的场景
     */
    public static JSONObject sendMessagePost(String url, String jsonParam) {
        try {
            String body = HttpRequest.post(url).body(jsonParam).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("post url:{} params:{} result:{}", url, jsonParam, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("post error url:{}", url, e);
            return null;
        }
    }

    // ==================== GET ====================

    /**
     * 发送 GET 请求
     */
    public static JSONObject get(String url) {
        try {
            String body = HttpRequest.get(url).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("get url:{} result:{}", url, JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get error url:{}", url, e);
            return null;
        }
    }

    /**
     * 发送 GET 请求（参数作为表单）
     */
    public static JSONObject get(String url, Map<String, Object> params) {
        try {
            String body = HttpRequest.get(url).form(params).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("get url:{} params:{} result:{}",
                    url, JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get error url:{}", url, e);
            return null;
        }
    }

    /**
     * 发送 GET 请求（自定义请求头 + 参数，默认超时）
     */
    public static JSONObject get(String url, Map<String, String> headers, Map<String, Object> params) {
        return get(url, headers, params, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 GET 请求（自定义请求头 + 参数 + 超时时间）
     */
    public static JSONObject get(String url, Map<String, String> headers, Map<String, Object> params, int timeout) {
        try {
            String body = HttpRequest.get(url)
                    .addHeaders(headers).form(params).timeout(timeout).execute().body();
            log.info("get url:{} headers:{} params:{} result:{}",
                    url, JSONObject.toJSONString(headers), JSONObject.toJSONString(params), JSONObject.toJSONString(body));
            return JSONObject.parseObject(body);
        } catch (Exception e) {
            log.error("get error url:{}", url, e);
            return null;
        }
    }

    /**
     * 发送 GET 请求（参数拼接到 URL 上）
     */
    public static JSONObject getParamUrl(String url, Map<String, String> headers, Map<String, Object> params) {
        try {
            String result = HttpRequest.get(url + paramUrl(params))
                    .addHeaders(headers).timeout(DEFAULT_TIMEOUT).execute().body();
            log.info("get url:{} headers:{} params:{} result:{}",
                    url, JSONObject.toJSONString(headers), JSONObject.toJSONString(params), JSONObject.toJSONString(result));
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("get error url:{}", url, e);
            return null;
        }
    }

    /**
     * 将参数 Map 转换为 URL 查询串
     * <p>例如：{a:1, b:2} → ?a=1&b=2
     */
    public static String paramUrl(Map<String, Object> param) {
        if (param == null || param.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("?");
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return sb.substring(0, sb.length() - 1);
    }
}