package com.sandbox.ai.mcp.server.config;

import com.sandbox.ai.mcp.server.tool.WeatherTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server 工具配置类
 * <p>
 * 负责将所有 {@code @Tool} 注解标注的工具类注册到 MCP Server 中，
 * 使 AI 客户端能够发现并调用这些工具。
 * </p>
 *
 * <p><b>如何添加新工具：</b></p>
 * <pre>
 * // 1. 创建新的工具类（参考 WeatherTool）
 * &#64;Component
 * public class MyNewTool {
 *     &#64;Tool(description = "工具描述")
 *     public String myMethod(&#64;ToolParam(description = "参数描述") String param) {
 *         // 业务逻辑
 *     }
 * }
 *
 * // 2. 在此配置类的 tools 方法参数中添加即可
 * &#64;Bean
 * public ToolCallbackProvider tools(WeatherTool weatherTool, MyNewTool myNewTool) {
 *     return MethodToolCallbackProvider.builder()
 *             .toolObjects(weatherTool, myNewTool)  // 添加新工具
 *             .build();
 * }
 * </pre>
 *
 * @author 0101
 * @create 2026/05/07
 */
@Configuration
public class McpToolConfig {

    /**
     * 注册 MCP 工具提供者
     * <p>
     * 将所有需要暴露给 AI 客户端的工具对象注册到此 Bean 中。
     * 添加新工具时，只需在方法参数和 builder 中追加热即可。
     * </p>
     *
     * @param weatherTool 天气查询工具（由 Spring 自动注入）
     * @return 包含所有工具对象的 ToolCallbackProvider
     */
    @Bean
    public ToolCallbackProvider tools(WeatherTool weatherTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherTool)
                .build();
    }
}