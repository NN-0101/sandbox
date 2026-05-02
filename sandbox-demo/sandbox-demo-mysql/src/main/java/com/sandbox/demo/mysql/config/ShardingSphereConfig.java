package com.sandbox.demo.mysql.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Configuration
public class ShardingSphereConfig {

    @Resource
    private EncryptRuleConfiguration encryptRuleConfiguration;

    @Bean
    @Primary
    public DataSource shardingSphereDataSource(DataSource ds0DataSource,
                                               DataSource ds1DataSource,
                                               DataSource ds0slave0DataSource,
                                               DataSource ds1slave0DataSource) throws SQLException {
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
        dataSourceMap.put("ds0", ds0DataSource);
        dataSourceMap.put("ds1", ds1DataSource);
        dataSourceMap.put("ds0slave0", ds0slave0DataSource);
        dataSourceMap.put("ds1slave0", ds1slave0DataSource);

        Collection<RuleConfiguration> ruleConfigs = new ArrayList<>();
        ruleConfigs.add(createReadwriteSplittingRule());
        ruleConfigs.add(createShardingRule());
        ruleConfigs.add(encryptRuleConfiguration);

        Properties props = new Properties();
        props.setProperty("sql-show", Boolean.TRUE.toString());

        DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(
                dataSourceMap, ruleConfigs, props);
        log.info("ShardingSphere 数据源创建成功（读写分离 + 分库分表）");
        return dataSource;
    }

    private ReadwriteSplittingRuleConfiguration createReadwriteSplittingRule() {
        // datasource0：写 ds0，读 ds0slave0
        ReadwriteSplittingDataSourceGroupRuleConfiguration ds0Group =
                new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                        "datasource0",              // name
                        "ds0",                      // writeDataSourceName
                        Collections.singletonList("ds0slave0"), // readDataSourceNames
                        ""                          // loadBalancerName（空串 = 默认轮询）
                );

        // datasource1：写 ds1，读 ds1slave0
        ReadwriteSplittingDataSourceGroupRuleConfiguration ds1Group =
                new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                        "datasource1",
                        "ds1",
                        Collections.singletonList("ds1slave0"),
                        ""
                );

        return new ReadwriteSplittingRuleConfiguration(
                Arrays.asList(ds0Group, ds1Group),
                new HashMap<>()
        );
    }

    private ShardingRuleConfiguration createShardingRule() {
        ShardingRuleConfiguration config = new ShardingRuleConfiguration();

        ShardingTableRuleConfiguration tableRuleConfig =
                new ShardingTableRuleConfiguration("t_user",
                        "datasource${0..1}.t_user_${0..1}");

        tableRuleConfig.setDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration("phone", "PHONE_DB_MOD"));
        tableRuleConfig.setTableShardingStrategy(
                new StandardShardingStrategyConfiguration("id", "ID_TABLE_MOD"));

        config.getTables().add(tableRuleConfig);

        // 算法只需要注册空的 AlgorithmConfiguration，因为我们在 SPI 中已经实现了
        config.getShardingAlgorithms().put("PHONE_DB_MOD",
                new AlgorithmConfiguration("PHONE_DB_MOD", new Properties()));
        config.getShardingAlgorithms().put("ID_TABLE_MOD",
                new AlgorithmConfiguration("ID_TABLE_MOD", new Properties()));

        return config;
    }
}