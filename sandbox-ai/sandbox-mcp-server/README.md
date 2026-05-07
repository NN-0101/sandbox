# sandbox-mcp-server

> MCP 工具服务端 — 通过 Model Context Protocol 向 AI Agent 暴露远程工具 🔧

sandbox-mcp-server 是 Sandbox 项目的 MCP（Model Context Protocol）工具服务端，将业务工具以标准 MCP 协议暴露给上游 AI Agent。Agent 无需内嵌工具实现，通过 `spring-ai-starter-mcp-client` 即可发现并调用本服务提供的所有工具。

---

## 🔗 与 sandbox-agent 的关系

```
sandbox-agent (MCP Client)          sandbox-mcp-server (MCP Server)
┌──────────────────────────┐        ┌──────────────────────────┐
│  ChatAgent               │  SSE   │  WeatherTool             │
│  WeatherAgent ───toolCallbacks──► │  (可扩展更多 Tool...)     │
│                          │        │                          │
│  spring-ai-starter       │        │  spring-ai-starter       │
│  -mcp-client             │        │  -mcp-server-webmvc      │
└──────────────────────────┘        └──────────────────────────┘
         :1011                              :1012
```

- **Agent** 通过 `mcp.client.sse.connections.server1.url` 指向本服务
- **MCP Server** 将 `@Tool` 注解的方法自动暴露为 MCP 工具
- 协议通信由 Spring AI 框架处理，业务代码只需写 `@Tool` 方法

---

## 🛠️ 当前工具

| 工具 | 方法 | 状态 | 说明 |
|:---|:---|:---|:---|
| `WeatherTool` | `getCurrentWeather(city)` | stub | 天气查询，待接入真实 API |

---

## ⚙️ 配置

### 服务配置 (`application.yml`)

```yaml
server:
  port: 1012
spring:
  application:
    name: sandbox-mcp-server
  ai:
    mcp:
      server:
        name: sandbox-mcp-server
        version: 1.0.0
        type: sync
        sse-message-endpoint: /mcp/message    # SSE 端点
```

### Agent 侧 MCP 客户端配置

在 sandbox-agent 的 `application-dev.yml` 中指向本服务：

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        type: sync
        sse:
          connections:
            server1:
              url: http://localhost:1012
```

---

## 📁 目录结构

```
sandbox-mcp-server/src/main/java/com/sandbox/ai/mcp/server/
├── McpServerApplication.java     # Spring Boot 入口
├── config/
│   └── McpToolConfig.java        # 工具注册配置 (MethodToolCallbackProvider)
└── tool/
    └── WeatherTool.java          # 天气查询工具 (@Tool)
```

---

## 🚀 快速开始

```bash
cd sandbox-ai/sandbox-mcp-server
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

启动后 MCP 工具通过 SSE 端点 `/mcp/message` 对外暴露，sandbox-agent 启动后自动发现并注册。

---

## 🔧 添加新工具

### 1. 创建工具类

```java
@Slf4j
@Component
public class CalculatorTool {

    @Tool(description = "计算数学表达式的结果")
    public double calculate(@ToolParam(description = "数学表达式，如 2+3*4") String expression) {
        log.info("计算表达式: {}", expression);
        // 实现计算逻辑
        return 0.0;
    }
}
```

### 2. 注册到 MCP

在 `McpToolConfig.tools()` 方法参数和 builder 中追加：

```java
@Bean
public ToolCallbackProvider tools(WeatherTool weatherTool,
                                   CalculatorTool calculatorTool) {  // ← 添加参数
    return MethodToolCallbackProvider.builder()
            .toolObjects(weatherTool, calculatorTool)               // ← 追加
            .build();
}
```

无需修改任何其他文件。Agent 端重启后即可通过 `toolCallbacks` 自动发现新工具。

---

## 🔗 关联模块

| 模块 | 关系 |
|:---|:---|
| [sandbox-agent](../sandbox-agent) | MCP 客户端，通过 SSE 调用本服务暴露的工具 |
