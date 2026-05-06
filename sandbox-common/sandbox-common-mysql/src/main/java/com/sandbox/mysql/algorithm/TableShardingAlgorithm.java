package com.sandbox.mysql.algorithm;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * 分表算法
 * <p>
 * 根据分片键的值取模，路由到对应的物理表。
 * 分片键可以是数字（直接取模）或字符串（按 hashCode 取模）。
 * <p>
 * 配置项：
 * - table-count：分表数量，默认 2
 * - table-name：逻辑表名（必填）
 */
public final class TableShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

    private Properties props;

    private static final String TABLE_COUNT_KEY = "table-count";
    private static final String TABLE_NAME_KEY = "table-name";
    private static final int DEFAULT_TABLE_COUNT = 2;

    @Override
    public String doSharding(final Collection<String> availableTargetNames,
                             final PreciseShardingValue<Comparable<?>> shardingValue) {
        int tableCount = getTableCount();
        String tableName = getTableName();
        // shardingValue.getValue() 即 SQL 中分片键的实际值，如 id = 1001 或 device_sn = 'DEV-001'
        long hashValue = toLong(shardingValue.getValue());
        long index = hashValue % tableCount;
        String target = tableName + "_" + index;

        if (availableTargetNames.contains(target)) {
            return target;
        }
        throw new UnsupportedOperationException("Cannot find target: " + target
                + " in " + availableTargetNames);
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames,
                                         final RangeShardingValue<Comparable<?>> shardingValue) {
        return availableTargetNames;
    }

    /**
     * 将分片键转为 long：数字直接取值，字符串取非负 hashCode
     */
    private long toLong(Comparable<?> value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return value.hashCode() & Integer.MAX_VALUE;
    }

    @Override
    public void init(final Properties props) {
        this.props = props;
    }

    @Override
    public String getType() {
        return "TABLE_MOD";
    }

    private int getTableCount() {
        if (props != null && props.containsKey(TABLE_COUNT_KEY)) {
            try {
                return Integer.parseInt(props.getProperty(TABLE_COUNT_KEY));
            } catch (NumberFormatException ignored) {
            }
        }
        return DEFAULT_TABLE_COUNT;
    }

    private String getTableName() {
        if (props != null && props.containsKey(TABLE_NAME_KEY)) {
            return props.getProperty(TABLE_NAME_KEY);
        }
        throw new UnsupportedOperationException("Table Algorithm error: table-name not configured");
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(final Properties props) {
        this.props = props;
    }
}