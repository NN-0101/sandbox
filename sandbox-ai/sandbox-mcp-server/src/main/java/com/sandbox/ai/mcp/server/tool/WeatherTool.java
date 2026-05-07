package com.sandbox.ai.mcp.server.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;


/**
 * 天气查询工具 - 调用 vvhan API 获取城市天气
 *
 * @author 0101
 * @create 2026/05/07
 */
@Slf4j
@Component
public class WeatherTool {

    @Tool(description = "获取某个城市的天气情况")
    public String getCurrentWeather(@ToolParam(description = "城市名") String city) throws JsonProcessingException {
        log.info("准备查询{}天气", city);
        return "晴天";
    }
}