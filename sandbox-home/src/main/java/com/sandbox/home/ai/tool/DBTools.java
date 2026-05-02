package com.sandbox.home.ai.tool;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * 数据库查询工具
 * <p>
 * 通过 @Tool 注解暴露给 AI 模型，使模型能够生成并执行 SELECT 查询。
 * 仅支持 SELECT，不允许更新或修改操作。
 *
 * @author 0101
 * @since 2026/03/18
 */
@Slf4j
public class DBTools {

    private final JdbcTemplate jdbcTemplate;

    public DBTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = "执行SQL查询并返回结果，仅支持SELECT查询")
    public String queryDatabase(String sql) {
        log.info("准备执行sql语句：{}", sql);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        log.info("执行结果：{}", JSONObject.toJSONString(result));
        return result.isEmpty() ? "未找到匹配的记录" : result.toString();
    }
}