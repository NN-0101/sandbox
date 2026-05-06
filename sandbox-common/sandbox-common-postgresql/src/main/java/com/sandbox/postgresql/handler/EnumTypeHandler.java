package com.sandbox.postgresql.handler;

/**
 * @description:
 * @author: 0101
 * @create: 2026/05/06
 */

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * PostgreSQL 枚举类型通用处理器
 *
 * @author 0101
 * @since 2026-05-06
 */
public class EnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private final Class<E> type;

    public EnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        // PostgreSQL 枚举可以直接用字符串
        ps.setObject(i, parameter.name(), Types.OTHER);
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : Enum.valueOf(type, value);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : Enum.valueOf(type, value);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : Enum.valueOf(type, value);
    }
}
