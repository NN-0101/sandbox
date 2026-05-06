package com.sandbox.postgresql.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.*;

/**
 * PostgreSQL JSON/JSONB 类型处理器
 * <p>
 * 支持序列化为 List<Map> 或自定义对象
 *
 * @author 0101
 * @since 2026-05-06
 */
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.OTHER)
public class JsonTypeHandler<T> extends BaseTypeHandler<T> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> type;

    public JsonTypeHandler(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = objectMapper.writeValueAsString(parameter);
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(json);
            ps.setObject(i, pgObject);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting object to JSON", e);
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private T parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON: " + json, e);
        }
    }

    /**
     * 获取用于 MyBatis 配置的原始类型处理器
     */
    public static class RawTypeHandler extends BaseTypeHandler<Object> {
        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
            try {
                String json = objectMapper.writeValueAsString(parameter);
                PGobject pgObject = new PGobject();
                pgObject.setType("jsonb");
                pgObject.setValue(json);
                ps.setObject(i, pgObject);
            } catch (JsonProcessingException e) {
                throw new SQLException("Error converting object to JSON", e);
            }
        }

        @Override
        public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
            return parseJsonToObject(rs.getString(columnName));
        }

        @Override
        public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
            return parseJsonToObject(rs.getString(columnIndex));
        }

        @Override
        public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
            return parseJsonToObject(cs.getString(columnIndex));
        }

        private Object parseJsonToObject(String json) {
            if (json == null || json.isEmpty()) return null;
            try {
                return objectMapper.readValue(json, Object.class);
            } catch (JsonProcessingException e) {
                return json;
            }
        }
    }
}