package com.sandbox.home.controller;

import com.alibaba.fastjson2.JSONObject;
import com.sandbox.home.ai.AiChatFacade;
import com.sandbox.home.ai.enumeration.AiChatBizTypeEnum;
import com.sandbox.home.enumeration.IdentityTypeEnum;
import com.sandbox.home.model.request.ai.AiMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @description:
 * @author: 0101
 * @create: 2026/05/06
 */
@RestController
@RequestMapping("/chat")
public class AIChatController {

    @Autowired
    private AiChatFacade aiChatFacade;

    @PostMapping("/message/{business}")
    public Flux<String> message(@PathVariable(value = "business") String business,
                                @RequestBody JSONObject param) {
        AiMessageRequest build = AiMessageRequest.builder()
                .chatType(AiChatBizTypeEnum.USER_TALK)
                .identifier("18607205429")
                .identityType(IdentityTypeEnum.USER.getValue())
                .message("你是谁")
                .build();
        return aiChatFacade.sendMessageStream(build);
    }
}
