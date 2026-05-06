package com.sandbox.home.config.algorithm;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * 分库算法
 * <p>
 * 根据分片键的值取模，路由到对应的数据源。
 * 数字类型直接取模，字符串类型按 hashCode 取模。
 * <p>
 * 配置项（通过 AlgorithmConfiguration 的 props 传入）：
 * - database-count：分库数量，默认 2
 * - datasource-prefix：数据源名称前缀，默认 "datasource"
 *
 * @author 0101
 * @since 2026-05-06
 */
public final class DatabaseShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

    private Properties props;

    private static final String DATABASE_COUNT_KEY = "database-count";
    private static final String DATASOURCE_PREFIX_KEY = "datasource-prefix";
    private static final int DEFAULT_DATABASE_COUNT = 2;
    private static final String DEFAULT_DATASOURCE_PREFIX = "datasource";

    /**
     * 精确分片：根据分片键值计算目标数据源
     *
     * @param availableTargetNames 可用的数据源名称列表
     * @param shardingValue        分片值，getValue() 即 SQL 中分片键的实际值，如 phone = '13800138000'
     * @return 目标数据源名称，如 "datasource0"
     */
    @Override
    public String doSharding(final Collection<String> availableTargetNames,
                             final PreciseShardingValue<Comparable<?>> shardingValue) {
        int databaseCount = getDatabaseCount();
        String datasourcePrefix = getDatasourcePrefix();

        long hashValue = toLong(shardingValue.getValue());
        long index = hashValue % databaseCount;
        String target = datasourcePrefix + index;

        if (availableTargetNames.contains(target)) {
            return target;
        }
        throw new UnsupportedOperationException("Cannot find target: " + target + " in " + availableTargetNames);
    }

    /**
     * 范围分片：暂不支持精确路由，返回所有可用数据源
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
        // & Integer.MAX_VALUE 确保正数，避免 Math.abs(Integer.MIN_VALUE) 溢出问题
        return value.hashCode() & Integer.MAX_VALUE;
    }

    @Override
    public void init(final Properties props) {
        this.props = props;
    }

    @Override
    public String getType() {
        return "DB_MOD";
    }

    private int getDatabaseCount() {
        if (props != null && props.containsKey(DATABASE_COUNT_KEY)) {
            try {
                return Integer.parseInt(props.getProperty(DATABASE_COUNT_KEY));
            } catch (NumberFormatException ignored) {
            }
        }
        return DEFAULT_DATABASE_COUNT;
    }

    private String getDatasourcePrefix() {
        if (props != null && props.containsKey(DATASOURCE_PREFIX_KEY)) {
            return props.getProperty(DATASOURCE_PREFIX_KEY);
        }
        return DEFAULT_DATASOURCE_PREFIX;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(final Properties props) {
        this.props = props;
    }
}