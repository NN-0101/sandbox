package com.sandbox.mcp.server.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 天气查询工具 - 调用 vvhan API 获取城市天气
 *
 * @author 0101
 * @create 2026/05/07
 */
@Slf4j
@Component
public class WeatherTool {

    private final RestClient restClient;
    private static final String BASE_URL = "https://api.vvhan.com";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public WeatherTool() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/geo+json")
                .defaultHeader("User-Agent", "WeatherApiClient/1.0 (your@email.com)")
                .build();
    }

    @Tool(description = "获取某个城市的天气情况")
    public String getCurrentWeather(@ToolParam(description = "城市名") String city) throws JsonProcessingException {
        log.info("准备查询{}天气", city);
        Map<?, ?> body = restClient.get()
                .uri("/api/weather?city={0}", city)
                .retrieve()
                .body(Map.class);
        log.info("查询结果：{}", OBJECT_MAPPER.writeValueAsString(body));
        return OBJECT_MAPPER.writeValueAsString(body);
    }
}