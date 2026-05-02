package com.sandbox.demo.mysql.algorithm;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * 按 id 分表算法
 */
public final class IdTableShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

    private Properties props;

    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        long id = ((Number) shardingValue.getValue()).longValue();
        long index = id % 2;
        String target = "t_user_" + index;
        if (availableTargetNames.contains(target)) {
            return target;
        }
        throw new UnsupportedOperationException("Cannot find target: " + target);
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        return availableTargetNames;
    }

    @Override
    public void init(final Properties props) {
        this.props = props;
    }

    @Override
    public String getType() {
        return "ID_TABLE_MOD";
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(final Properties props) {
        this.props = props;
    }
}