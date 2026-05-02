package com.sandbox.demo.mysql.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * ShardingSphere 5.3.2 核心配置
 * <p>
 * 读写分离 + 分库分表：
 *   分库：phone hash 取模
 *   分表：id 取模
 *
 * @author 0101
 * @since 2026-05-02
 */
@Slf4j
@Configuration
public class ShardingSphereConfig {

    @Bean
    @Primary
    public DataSource shardingSphereDataSource(DataSource ds0DataSource,
                                               DataSource ds1DataSource,
                                               DataSource ds0slave0DataSource,
                                               DataSource ds1slave0DataSource) throws SQLException {
        // 1. 注册物理数据源
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
        dataSourceMap.put("ds0", ds0DataSource);
        dataSourceMap.put("ds1", ds1DataSource);
        dataSourceMap.put("ds0slave0", ds0slave0DataSource);
        dataSourceMap.put("ds1slave0", ds1slave0DataSource);

        // 2. 规则集合
        Collection<RuleConfiguration> ruleConfigs = new ArrayList<>();
        ruleConfigs.add(createReadwriteSplittingRule());
        ruleConfigs.add(createShardingRule());

        // 3. 配置属性
        Properties props = new Properties();
        props.setProperty("sql-show", Boolean.TRUE.toString());

        // 4. 创建数据源
        DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(
                dataSourceMap, ruleConfigs, props);
        log.info("ShardingSphere 数据源创建成功（读写分离 + 分库分表）");
        return dataSource;
    }

    /**
     * 读写分离规则
     */
    private ReadwriteSplittingRuleConfiguration createReadwriteSplittingRule() {
        ReadwriteSplittingDataSourceRuleConfiguration ds0Config =
                new ReadwriteSplittingDataSourceRuleConfiguration(
                        "datasource0", "", "ds0",
                        Collections.singletonList("ds0slave0"), "ROUND_ROBIN");

        ReadwriteSplittingDataSourceRuleConfiguration ds1Config =
                new ReadwriteSplittingDataSourceRuleConfiguration(
                        "datasource1", "", "ds1",
                        Collections.singletonList("ds1slave0"), "ROUND_ROBIN");

        return new ReadwriteSplittingRuleConfiguration(
                Arrays.asList(ds0Config, ds1Config), new HashMap<>());
    }

    /**
     * 分库分表规则
     */
    private ShardingRuleConfiguration createShardingRule() {
        ShardingRuleConfiguration config = new ShardingRuleConfiguration();

        // ========== t_user 表分片规则 ==========
        ShardingTableRuleConfiguration tableRuleConfig =
                new ShardingTableRuleConfiguration("t_user",
                        "datasource${0..1}.t_user_${0..1}");  // 4 张物理表

        // 分库策略：phone 取模 → datasource0 / datasource1
        tableRuleConfig.setDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration("phone", "phone_db_mod"));

        // 分表策略：id 取模 → t_user_0 / t_user_1
        tableRuleConfig.setTableShardingStrategy(
                new StandardShardingStrategyConfiguration("id", "id_table_mod"));

        config.getTables().add(tableRuleConfig);

        // ========== 注册分片算法 ==========

        // 分库算法：phone hash 取模 2
        Properties dbProps = new Properties();
        dbProps.setProperty("sharding-count", "2");
        config.getShardingAlgorithms().put("phone_db_mod",
                new ShardingSphereAlgorithmConfiguration("HASH_MOD", dbProps));

        // 分表算法：id 取模 2
        Properties tableProps = new Properties();
        tableProps.setProperty("sharding-count", "2");
        config.getShardingAlgorithms().put("id_table_mod",
                new ShardingSphereAlgorithmConfiguration("HASH_MOD", tableProps));

        return config;
    }
}