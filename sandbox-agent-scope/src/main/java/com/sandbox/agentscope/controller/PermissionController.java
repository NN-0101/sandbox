package com.sandbox.agentscope.controller;

import com.sandbox.agentscope.tool.TimeTools;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEventType;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.permission.AdditionalWorkingDirectory;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.tool.Toolkit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @description:
 * @author: 0101
 * @create: 2026/08/20
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @GetMapping("/demo")
    public Flux<String> demo(String message) {

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new TimeTools());

        PermissionContextState permCtx = PermissionContextState.builder()
                .mode(PermissionMode.DEFAULT)
                .build();


        ReActAgent agent = ReActAgent.builder()
                .name("permission demo")
                .model("deepseek:deepseek-v4-flash")
                .permissionContext(permCtx)
                .toolkit(toolkit)
                .build();

        return agent.streamEvents(message, RuntimeContext.empty())
                .doOnNext(event -> {
                    if (event.getType() == AgentEventType.TEXT_BLOCK_DELTA) {
                        System.out.print(((TextBlockDeltaEvent) event).getDelta());
                    } else if (event.getType() == AgentEventType.TOOL_CALL_START) {
                        System.out.println("\n[tool] " + ((ToolCallStartEvent) event).getToolCallName());
                    }
                })
                .filter(event -> event.getType() == AgentEventType.TEXT_BLOCK_DELTA)
                .map(event -> ((TextBlockDeltaEvent) event).getDelta());
    }
}
