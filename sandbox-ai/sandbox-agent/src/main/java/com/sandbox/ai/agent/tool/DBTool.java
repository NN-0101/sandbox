package com.sandbox.ai.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

/**
 * 数据库查询工具 本地工具调用
 * <p>
 * 通过 @Tool 注解暴露给 AI 模型，使模型能够生成并执行 SELECT 查询。
 * 仅支持 SELECT，不允许更新或修改操作。
 *
 * @author 0101
 * @since 2026/03/18
 */
@Slf4j
public class DBTool {

    @Tool(description = "执行SQL查询并返回结果，仅支持SELECT查询")
    public String queryDatabase(String sql) {
        log.info("准备执行sql语句：{}", sql);
        return null;
    }
}