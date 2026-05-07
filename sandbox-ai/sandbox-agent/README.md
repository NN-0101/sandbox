# sandbox-agent

> AI Agent 运行时 — 基于 Spring AI 的 Agent + Skill 范式 🤖

sandbox-agent 是 Sandbox 项目的 AI 智能体运行时模块，实现可组合的 Agent + Skill 架构。

**核心收益**：Skill 作为独立的能力单元，可被多个 Agent 组合复用。一个 Skill 写好一次，注入到任意 Agent 即可生效——提示词和工具同步到位，无需重复编码。Agent 通过 MCP 协议连接远程工具服务，本地工具与远程工具在 Skill 层统一管理。

---

## 🎯 核心设计

### Agent + Skill 范式

```
┌─────────────────────────────────────────────────┐
│                   AiAgentDispatcher              │
│              按 AgentTypeEnum 路由请求             │
└──────┬──────────────┬──────────────┬────────────┘
       │              │              │
       ▼              ▼              ▼
┌─────────────┐ ┌──────────┐ ┌──────────────┐
│  ChatAgent  │ │ DBAgent  │ │ WeatherAgent │
│  USER_TALK  │ │    DB    │ │ MCP_WEATHER  │
└──┬──────┬───┘ └────┬─────┘ └──────┬───────┘
   │      │          │              │
   ▼      ▼          ▼              ▼
┌────────┐┌────────┐┌────────┐┌──────────┐
│ChatSkill││DBSkill ││DBSkill ││WeatherSkill│
│ prompt ││ prompt ││ prompt ││  prompt   │
│   —    ││DBTool  ││DBTool  ││    —      │
└────────┘└────────┘└────────┘└─────┬─────┘
                                    │ MCP
                                    ▼
                           ┌─────────────────┐
                           │ sandbox-mcp-server│
                           │   WeatherTool    │
                           └─────────────────┘
```

- **Agent = 编排层**：组合 Skill、管理会话记忆（ChatMemory）、调度执行
- **Skill = 能力层**：封装系统提示词 + 本地工具（`@Tool`），可跨 Agent 复用
- **MCP = 远程工具**：独立部署的工具服务，通过 Model Context Protocol 调用

### 请求链路

```
Client → AiAgentDispatcher.sendMessageStream(request)
           ├─ 校验 (validate)
           ├─ 路由 (AgentTypeEnum → AiAgent)
           ├─ 解析会话ID (ChatMemory)
           └─ agent.execute(conversationId, message)
                 ├─ 合并 Skill prompts → system message
                 ├─ 收集 Skill tools → ChatClient.tools()
                 ├─ 注入 MCP toolCallbacks（如有）
                 └─ chatClient.stream().content() → Flux<String>
```

### 策略映射

```java
// AiConfig 自动扫描所有 @AiAgentType 标注的 Bean，构建策略映射
@Bean
public Map<AgentTypeEnum, AiAgent> chatMessageStrategyMap(List<AiAgent> strategies) {
    // 重复类型 → IllegalStateException
}
```

| 枚举值 | Agent | 组合的 Skill | 工具来源 |
|:---|:---|:---|:---|
| `USER_TALK` | ChatAgent | ChatSkill + DBSkill | 本地 DBTool + MCP |
| `DB` | DBAgent | DBSkill | 本地 DBTool |
| `MCP_WEATHER` | WeatherAgent | WeatherSkill | MCP 远程 |

### Skill 复用：以 DBSkill 为例

上表中 `DBSkill` 被 **ChatAgent** 和 **DBAgent** 两个 Agent 同时使用——这是 Agent + Skill 范式最核心的价值。

```
DBSkill (一个 Bean, 写好一次)
  ├── prompt: "你是一个数据库专家，擅长编写SQL查询并解释查询结果"
  └── tools:  [ DBTool — @Tool queryDatabase ]

     ┌──────────── 注入 ────────────┐
     ▼                              ▼
ChatAgent                       DBAgent
  "帮我查一下用户表"              "SELECT COUNT(*) FROM users"
  → DBSkill 提供 DB 知识          → DBSkill 提供 DB 知识
  → DBTool 执行查询               → DBTool 执行查询
  → ChatSkill 用自然语言解释      → 直接返回查询结果
```

**改造前的做法**（每个 Agent 各自硬编码）：
- `ChatAgent` 里 `new DBTool()` 一份
- `DBAgent` 里 `new DBTool()` 又一份
- 提示词散落在 `BusinessConfig.prompts` 的扁平 Map 中，Agent 和 prompt 之间没有显式关联

**改造后**：一个 `DBSkill` Bean 定义一次，两个 Agent 通过 `@Qualifier("dbSkill")` 注入即可。加第三个需要 DB 能力的 Agent 时，只需多一行注入声明，零重复代码。

---

## 🛠️ 技术栈

| 类别 | 技术 | 用途 |
|:---|:---|:---|
| 核心框架 | Spring Boot 3.x | 应用框架 |
| AI 框架 | Spring AI | ChatClient / ChatMemory / MCP Client |
| LLM | DeepSeek (OpenAI 兼容) | 模型服务 |
| 微服务 | Spring Cloud Alibaba | Nacos 服务注册发现 + 配置中心 |
| 响应式 | Project Reactor | Flux 流式响应 |
| 日志链路 | TLog | 链路追踪 |

---

## ⚙️ 配置

### 基础配置 (`application.yml`)

```yaml
server:
  port: 1011
  servlet:
    context-path: /sandbox-agent
spring:
  application:
    name: sandbox-agent
```

### AI 模型 + MCP (`application-dev.yml`)

```yaml
spring:
  ai:
    openai:
      api-key: sk-xxx
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-chat
          temperature: 0.7
    mcp:
      client:
        enabled: true
        type: sync
        sse:
          connections:
            server1:
              url: http://localhost:1012    # sandbox-mcp-server
```

### Skill 提示词 (`application-dev.yml`)

```yaml
business-config:
  skills:
    chat:
      prompt: "你是一个友好的AI助手，请用中文回答用户的问题"
    db:
      prompt: "你是一个数据库专家，擅长编写SQL查询并解释查询结果"
    weather:
      prompt: "你可以帮助用户查询天气信息，使用可用的天气工具获取数据"
```

提示词支持通过 Nacos 动态刷新（`refresh-enabled: true`），修改后无需重启。

---

## 📁 目录结构

```
sandbox-agent/src/main/java/com/sandbox/ai/agent/
├── AgentApplication.java          # Spring Boot 入口
├── annotations/
│   └── AiAgentType.java           # Agent 类型标记注解 (@Qualifier)
├── config/
│   ├── AiConfig.java              # ChatClient / 策略映射 Bean
│   └── BusinessConfig.java        # Skill 提示词配置绑定
├── core/
│   ├── AiAgent.java               # Agent 接口
│   └── impl/
│       ├── ChatAgent.java         # 对话 Agent (ChatSkill + DBSkill + MCP)
│       ├── DBAgent.java           # 数据库 Agent (DBSkill)
│       └── WeatherAgent.java      # 天气 Agent (WeatherSkill + MCP)
├── enumeration/
│   └── AgentTypeEnum.java         # Agent 业务类型枚举
├── facade/
│   └── AiAgentDispatcher.java     # Agent 调度门面
├── model/
│   ├── request/
│   │   └── AiMessageRequest.java  # 请求 DTO
│   └── response/
│       ├── AiMessageResponse.java # 响应 DTO
│       └── AiStreamChunk.java     # 流式片段 DTO
├── skill/
│   ├── Skill.java                 # Skill 接口 (prompt + tools)
│   └── impl/
│       ├── ChatSkill.java         # 对话技能
│       ├── DBSkill.java           # 数据库技能 (@Tool queryDatabase)
│       └── WeatherSkill.java      # 天气技能 (MCP 工具)
└── tlog/
    ├── LogConfig.java
    ├── LogPathProperty.java
    ├── TLogFilter.java
    └── TLogWebCommon.java
```

---

## 🚀 快速开始

### 前置条件

- JDK 17+
- Nacos（配置中心 + 服务发现）
- sandbox-mcp-server（如使用 MCP 工具）

### 启动

```bash
cd sandbox-ai/sandbox-agent
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 测试

```java
// ChatMessageTest 覆盖：
//  - 基本对话    (test01_basicConversation)
//  - 多轮记忆    (test02_multiTurnConversation)
//  - 流式响应    (test03_streamConversation)
//  - 记忆隔离    (test04_memoryIsolation)
//  - 参数校验    (test05_parameterValidation)
```

```bash
mvn test -Dtest=ChatMessageTest
```

---

## 🔧 扩展指南

### 新增一个 Skill

1. 实现 `Skill` 接口：

```java
@Component("codeReviewSkill")
public class CodeReviewSkill implements Skill {
    @Override public String getName() { return "code-review"; }
    @Override public String getPrompt() { return "你是代码审查专家..."; }
}
```

2. 配置提示词（`application-dev.yml`）：

```yaml
business-config:
  skills:
    code-review:
      prompt: "你是一个资深代码审查专家，请审查以下代码"
```

3. 在 Agent 中注入使用（**同一个 Skill 可注入到多个 Agent**）：

```java
// ChatAgent 注入
public ChatAgent(..., @Qualifier("codeReviewSkill") Skill codeReviewSkill) { ... }

// 任何其他 Agent 也可以同时注入，无需重复编写 Skill
public DBAgent(..., @Qualifier("codeReviewSkill") Skill codeReviewSkill) { ... }
```

### 新增一个 Agent

1. 实现 `AiAgent` 接口 + 标注 `@AiAgentType`
2. 在 `AgentTypeEnum` 中添加枚举值
3. 在 `AiConfig.chatMessageStrategyMap()` 中自动注册（无需手动配置）

---

## 🔗 关联模块

| 模块 | 关系 |
|:---|:---|
| [sandbox-mcp-server](../sandbox-mcp-server) | MCP 工具服务端，Agent 通过 MCP Client 调用 |
