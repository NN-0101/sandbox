package com.sandbox.common.base.utils;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * HTTP 请求工具，基于 Hutool，支持 GET/POST/表单，超时默认 3s，失败返回 null。
 *
 * <pre>
 * JSONObject result = HttpRequestUtils.get("<a href="https://api.example.com/users">...</a>");
 * JSONObject result = HttpRequestUtils.post(url, headers, jsonBody);
 * JSONObject result = HttpRequestUtils.postForm(url, formData);
 * </pre>
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

    public static JSONObject post(String url, Map<String, String> headers, String jsonParam) {
        return post(url, headers, jsonParam, DEFAULT_TIMEOUT);
    }

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
     * 发送 POST，失败返回 null，适用于消息推送等不严格要求成功的场景
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

    public static JSONObject get(String url, Map<String, String> headers, Map<String, Object> params) {
        return get(url, headers, params, DEFAULT_TIMEOUT);
    }

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
     * 参数 Map → ?a=1&b=2
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