package com.sandbox.home.config.algorithm;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * 分表算法
 * <p>
 * 根据分片键的值取模，路由到对应的物理表。
 * 数字类型直接取模，字符串类型按 hashCode 取模。
 * <p>
 * 配置项（通过 AlgorithmConfiguration 的 props 传入）：
 * - table-count：分表数量，默认 2
 * - table-name：逻辑表名（必填）
 *
 * @author 0101
 * @since 2026-05-06
 */
public final class TableShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

    private Properties props;

    private static final String TABLE_COUNT_KEY = "table-count";
    private static final String TABLE_NAME_KEY = "table-name";
    private static final int DEFAULT_TABLE_COUNT = 2;

    /**
     * 精确分片：根据分片键值计算目标物理表
     *
     * @param availableTargetNames 可用的物理表名称列表
     * @param shardingValue        分片值，getValue() 即 SQL 中分片键的实际值，如 id = 1001 或 device_sn = 'DEV-001'
     * @return 目标物理表名，如 "t_user_1"
     */
    @Override
    public String doSharding(final Collection<String> availableTargetNames,
                             final PreciseShardingValue<Comparable<?>> shardingValue) {
        int tableCount = getTableCount();
        String tableName = getTableName();

        long hashValue = toLong(shardingValue.getValue());
        long index = hashValue % tableCount;
        String target = tableName + "_" + index;

        if (availableTargetNames.contains(target)) {
            return target;
        }
        throw new UnsupportedOperationException("Cannot find target: " + target + " in " + availableTargetNames);
    }

    /**
     * 范围分片：暂不支持精确路由，返回所有可用表
     */
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