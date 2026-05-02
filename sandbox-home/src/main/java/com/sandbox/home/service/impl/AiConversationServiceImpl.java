package com.sandbox.home.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sandbox.home.entity.AiConversationDO;
import com.sandbox.home.mapper.custom.AiConversationRepository;
import com.sandbox.home.service.AiConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @description: AI聊天会话表(AiConversation)表服务实现类
 * @author: 0101
 * @create: 2026-04-30 15:26:07
 */
@Slf4j
@Service("aiConversationService")
public class AiConversationServiceImpl extends ServiceImpl<AiConversationRepository, AiConversationDO> implements AiConversationService {

}

