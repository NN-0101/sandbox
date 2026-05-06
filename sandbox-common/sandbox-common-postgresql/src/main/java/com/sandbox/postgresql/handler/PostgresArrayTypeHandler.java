package com.sandbox.postgresql.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * PostgreSQL 数组类型处理器
 *
 * @author 0101
 * @since 2026-05-06
 */
public class PostgresArrayTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        Array array = ps.getConnection().createArrayOf("text", parameter.toArray());
        ps.setArray(i, array);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseArray(rs.getArray(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseArray(rs.getArray(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseArray(cs.getArray(columnIndex));
    }

    private List<String> parseArray(Array array) throws SQLException {
        if (array == null) {
            return null;
        }
        String[] arr = (String[]) array.getArray();
        return Arrays.asList(arr);
    }
}