package com.sandbox.agentscope.controller;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEventType;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ModelCreationContext;
import io.agentscope.core.model.ModelRegistry;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AgentScope QuickStart 示例控制器
 * 演示如何使用 OpenAI 风格（兼容）的模型服务
 *
 * @author 0101
 * @create 2026/08/19
 */
@RestController
@RequestMapping("quick-start")
public class QuickStartController {

    // ============================================================
    // 从 application-dev.yml 读取配置
    // ============================================================
    @Value("${agentscope.openai.api-key}")
    private String apiKey;

    @Value("${agentscope.openai.base-url}")
    private String baseUrl;

    @Value("${agentscope.openai.model-name}")
    private String modelName;

    @Value("${agentscope.openai.stream:true}")
    private Boolean stream;

    /**
     * 方式一：通过模型 ID 加载模型（SPI 服务发现机制）
     * <p>
     * 加载方式：通过字符串 ID 方式加载模型，底层依赖 Java SPI 机制自动发现并匹配 ModelProvider。
     * 详见：docs/AgentScope模型ID加载机制-SPI服务发现流程.html
     * <p>
     * 配置来源：环境变量（如 DEEPSEEK_API_KEY）
     * 适用场景：快速原型、简单应用、配置固定
     * <p>
     * 扩展说明：如需接入公司自建模型（非 OpenAI 兼容格式），需自行实现 ModelProvider 接口，
     * 并在 src/main/resources/META-INF/services/ 下创建 SPI 配置文件。
     *
     * @return 模型返回的文本内容
     */
    @GetMapping("/demo")
    public String demo() {
        // 构建 Agent，通过模型 ID 字符串指定使用哪个模型
        // 底层由 ModelRegistry.resolve(modelId) 自动解析
        HarnessAgent agent = HarnessAgent.builder()
                .name("quickStart")
                // 模型 ID 格式：提供商:模型名称
                // DeepSeek 示例，需要配置环境变量 DEEPSEEK_API_KEY=sk-xxx
                .model("deepseek:deepseek-v4-flash")
                .build();

        // 调用 Agent，发送消息 "你是谁"，阻塞等待响应
        Msg block = agent.call(Msg.builder().textContent("你是谁").build()).block();
        assert block != null;
        return block.getTextContent();
    }

    /**
     * 流式返回
     *
     * @return 结果
     */
    @GetMapping("/demo-stream")
    public Flux<String> demoStream() {
        // 构建 Agent，通过模型 ID 字符串指定使用哪个模型
        // 底层由 ModelRegistry.resolve(modelId) 自动解析
        HarnessAgent agent = HarnessAgent.builder()
                .name("quickStart")
                // 模型 ID 格式：提供商:模型名称
                // DeepSeek 示例，需要配置环境变量 DEEPSEEK_API_KEY=sk-xxx
                .model("deepseek:deepseek-v4-flash")
                .build();

        return agent.streamEvents("你是谁", RuntimeContext.empty())
                // doOnNext：在流中每个事件发出时执行，但不影响事件本身
                // 作用：在服务端控制台打印调试信息，方便开发时观察 Agent 行为
                .doOnNext(event -> {
                    // 判断事件类型：文本块增量事件（即模型输出的流式文本片段）
                    if (event.getType() == AgentEventType.TEXT_BLOCK_DELTA) {
                        // 模型返回的流式文本片段 —— 追加到界面或标准输出
                        System.out.print(((TextBlockDeltaEvent) event).getDelta());
                    }
                    // 判断事件类型：工具调用开始事件
                    else if (event.getType() == AgentEventType.TOOL_CALL_START) {
                        // 智能体即将调用工具 —— 展示调用信息
                        System.out.println("\n[tool] " + ((ToolCallStartEvent) event).getToolCallName());
                    }
                })
                // filter：过滤流中的事件，只允许满足条件的事件通过
                // 作用：只保留 TEXT_BLOCK_DELTA 类型的事件，过滤掉工具调用等其他事件
                // 结果：前端只会收到文本内容，不会收到工具调用等信息
                .filter(event -> event.getType() == AgentEventType.TEXT_BLOCK_DELTA)
                // map：将每个事件转换为另一种类型
                // 作用：从 TEXT_BLOCK_DELTA 事件中提取出实际的文本增量内容（String）
                // 结果：Flux<AgentEvent> 变成 Flux<String>，前端直接收到文本片段
                .map(event -> ((TextBlockDeltaEvent) event).getDelta());
    }


    /**
     * 方式二：显式 Model builder（从 YAML 读取配置）
     * <p>
     * 加载方式：通过 OpenAIChatModel.builder() 显式构建 Model 实例，
     * 直接传入 Agent，不经过 SPI 服务发现。
     * <p>
     * 配置来源：application-dev.yml 中的 agentscope.openai.* 配置
     * 适用场景：需要精细控制参数、自定义 base-url/formatter/timeout 等
     * <p>
     * 优势：
     * - 所有配置集中在 YAML 文件中，便于管理
     * - 支持自定义 base-url（如公司内部代理地址）
     * - 可设置超时时间、生成参数等专属配置
     *
     * @return 模型返回的文本内容
     */
    @GetMapping("/demo1")
    public String demo1() {
        // 显式构建 Model，从 YAML 读取配置
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(apiKey)                         // 从 YAML 读取
                .baseUrl(baseUrl)                       // 从 YAML 读取
                .modelName(modelName)                   // 从 YAML 读取
                .stream(stream)                         // 从 YAML 读取
                // 可选：自定义生成参数
                // .defaultOptions(GenerateOptions.builder()
                //         .temperature(0.7)
                //         .maxTokens(2048)
                //         .build())
                // 可选：自定义超时时间
                // .timeout(Duration.ofSeconds(60))
                .build();

        // 将 Model 实例直接传入 Agent
        HarnessAgent agent = HarnessAgent.builder()
                .name("quickStart")
                .model(model)  // 直接传入 Model 对象，不经过 SPI
                .build();

        // 调用 Agent，发送消息 "你是谁"，阻塞等待响应
        Msg block = agent.call(Msg.builder().textContent("你是谁").build()).block();
        assert block != null;
        return block.getTextContent();
    }

    /**
     * 方式三：ModelCreationContext 高级集成上下文（动态传参 + SPI）
     * <p>
     * 加载方式：通过 ModelRegistry.resolve(modelId, context) 解析，
     * 底层仍走 SPI 服务发现，但配置从 ModelCreationContext 中动态传入。
     * <p>
     * 配置来源：代码中动态构建 ModelCreationContext
     * 适用场景：多租户系统、API 网关、插件系统、框架适配层
     * <p>
     * 核心特点：
     * - 每次解析可传入不同的 API Key、Base URL（如按租户隔离）
     * - 无需依赖具体 Model 类（如 OpenAIChatModel）
     * - 不读取环境变量或 YAML，完全由代码控制
     * <p>
     * 注意：ModelCreationContext 默认不缓存，每次都会创建新 Model 实例，
     * 如需缓存需显式设置 CachePolicy.ENABLED 并提供 cacheId。
     *
     * @return 模型返回的文本内容
     */
    @GetMapping("/demo2")
    public String demo2() {
        // 1. 构建 ModelCreationContext，动态传入配置
        ModelCreationContext context = ModelCreationContext.builder()
                // 动态传入 API Key（可来自租户、用户等上下文）
                .apiKey(apiKey)                         // 从 YAML 读取，实际场景可动态传入
                // 动态传入 Base URL（可指向公司内部代理）
                .baseUrl(baseUrl)                       // 从 YAML 读取，实际场景可动态传入
                // 是否启用流式输出
                .stream(stream)                         // 从 YAML 读取
                // 扩展模块定义的标量配置（key 由具体模型提供商文档约定）
                .option("contextWindowSize", 128000)
                // 以类型为 key 的组件对象，用于传入更复杂的提供商配置
                // .component(
                //         GenerateOptions.class,
                //         GenerateOptions.builder()
                //                 .temperature(0.7)
                //                 .parallelToolCalls(false)
                //                 .build())
                .build();

        // 2. 通过 ModelRegistry.resolve() 解析模型
        //    底层走 SPI：解析 "openai:deepseek-v4-flash" → 匹配 OpenAIModelProvider
        //    然后 OpenAIModelProvider.create() 会使用 context 中的配置
        Model model = ModelRegistry.resolve("openai:deepseek-v4-flash", context);

        // 3. 将 Model 实例传入 Agent
        //    注意：这里 model 是 Model 接口类型，不是具体的 OpenAIChatModel
        HarnessAgent agent = HarnessAgent.builder()
                .name("quickStart")
                .model(model)
                .build();

        // 4. 调用 Agent
        Msg block = agent.call(Msg.builder().textContent("你是谁").build()).block();
        assert block != null;
        return block.getTextContent();
    }
}