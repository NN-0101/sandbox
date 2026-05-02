package com.sandbox.home.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sandbox.home.entity.AiMessageDO;
import org.apache.ibatis.annotations.Param;

/**
 * @description: AI聊天消息表(AiMessage)表数据库访问层
 * @author: 0101
 * @create: 2026-04-30 14:25:11
 */
public interface AiMessageMapper extends BaseMapper<AiMessageDO> {

     /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<User> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AiMessageDO> entities);
}

