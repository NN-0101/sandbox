package com.sandbox.home.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sandbox.home.entity.AiMessageDO;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * @description: AI聊天消息表(AiMessage)表服务接口
 * @author: 0101
 * @create: 2026-04-30 14:25:11
 */
public interface AiMessageService extends IService<AiMessageDO> {

    /**
     * 添加会话记录
     *
     * @param conversationId 会话id
     * @param messages       消息呢荣
     */
    void add(String conversationId, List<Message> messages);

    /**
     * 获取会话记录
     *
     * @param conversationId 会话id
     * @return 结果
     */
    List<Message> get(String conversationId);

    /**
     * 删除会话
     *
     * @param conversationId 会话id
     */
    void clear(String conversationId);
}

