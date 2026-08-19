package com.sandbox.agentscope.controller;

import io.agentscope.core.message.Msg;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 通过模型 ID 加载模型（SPI 服务发现机制）
     * <p>
     * 加载方式：通过字符串 ID 方式加载模型，底层依赖 Java SPI 机制自动发现并匹配 ModelProvider。
     * 详见：docs/AgentScope模型ID加载机制-SPI服务发现流程.html
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
}