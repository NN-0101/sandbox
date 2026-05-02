package com.sandbox.home.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sandbox.home.entity.AiConversationDO;
import org.apache.ibatis.annotations.Param;

/**
 * @description: AI聊天会话表(AiConversation)表数据库访问层
 * @author: 0101
 * @create: 2026-04-30 15:26:07
 */
public interface AiConversationMapper extends BaseMapper<AiConversationDO> {

     /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<User> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AiConversationDO> entities);
}

