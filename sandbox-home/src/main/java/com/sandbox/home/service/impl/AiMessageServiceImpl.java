package com.sandbox.home.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sandbox.home.entity.AiMessageDO;
import com.sandbox.home.mapper.custom.AiMessageRepository;
import com.sandbox.home.model.bo.ai.AIMessageBO;
import com.sandbox.home.service.AiConversationService;
import com.sandbox.home.service.AiMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: AI聊天消息表(AiMessage)表服务实现类
 * @author: 0101
 * @create: 2026-04-30 14:25:11
 */
@Slf4j
@Service("aiMessageService")
public class AiMessageServiceImpl extends ServiceImpl<AiMessageRepository, AiMessageDO> implements AiMessageService {

    @Autowired
    private AiConversationService aiConversationService;

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<AIMessageBO> entities = messages.stream().map(m -> new AIMessageBO(conversationId, m)).toList();
        if (CollectionUtils.isNotEmpty(entities)) {
            List<AiMessageDO> collect = entities.stream().map(m -> BeanUtil.copyProperties(m, AiMessageDO.class)).toList();
            getBaseMapper().insertBatch(collect);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        LambdaQueryWrapper<AiMessageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiMessageDO::getConversationId, conversationId).orderByAsc(AiMessageDO::getCreateDate);

        Page<AiMessageDO> page = new Page<>(0, Integer.MAX_VALUE);
        Page<AiMessageDO> pageList = getBaseMapper().selectPage(page, queryWrapper);

        List<AiMessageDO> records = pageList.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        // 转换为Message列表
        List<Message> messageList = new ArrayList<>();
        for (AiMessageDO record : records) {
            //消息类型
            MessageType messageType = MessageType.valueOf(record.getMessageType().toUpperCase());
            // 创建空元数据（因ChatMemoryDO未存储metadata）
            Map metadata = JSONObject.parseObject(record.getMetadata(), Map.class);
            // 根据类型创建具体Message实现类
            Message message;
            switch (messageType) {
                case USER:
                    message = new UserMessage(record.getContent());
                    break;
                case ASSISTANT:
                    message = new AssistantMessage(record.getContent());
                    break;
                case SYSTEM:
                    message = new SystemMessage(record.getContent());
                    break;
                /*case TOOL:
                    message = new ToolResponseMessage(record.getContent(), metadata);
                    break;*/
                default:
                    log.warn("Unsupported message type: {}, treated as USER", messageType);
                    continue;

            }
            messageList.add(message);
        }
        return messageList;
    }

    @Override
    public void clear(String conversationId) {
        //删除用户会话
        aiConversationService.getBaseMapper().deleteById(conversationId);

        LambdaQueryWrapper<AiMessageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiMessageDO::getConversationId, conversationId);
        getBaseMapper().delete(queryWrapper);
    }
}

