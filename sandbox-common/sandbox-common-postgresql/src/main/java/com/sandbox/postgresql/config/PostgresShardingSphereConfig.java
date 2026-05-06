package com.sandbox.postgresql.config;

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

/**
 * ShardingSphere 5.5.1 PostgreSQL 数据源配置
 * <p>
 * 整合读写分离 + 分库分表 + 数据加密，创建统一的数据源。
 *
 * @author 0101
 * @since 2026-05-06
 */
@Slf4j
@Configuration
public class PostgresShardingSphereConfig {

    @Resource
    private EncryptRuleConfiguration encryptRuleConfiguration;

    @Resource
    private PostgresDataSourceConfig dataSourceConfig;

    /**
     * 创建 ShardingSphere 数据源，整合读写分离、分片、加密规则
     */
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

        // PostgreSQL特有属性
        props.setProperty("sql-simple", Boolean.FALSE.toString());
        props.setProperty("max-connections-size-per-query", "1");

        DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, ruleConfigs, props);
        log.info("PostgreSQL ShardingSphere 数据源创建成功（读写分离 + 分库分表）");
        return dataSource;
    }

    /**
     * 读写分离规则
     */
    private ReadwriteSplittingRuleConfiguration createReadwriteSplittingRule() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration ds0Group =
                new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                        "datasource0", "ds0",
                        Collections.singletonList("ds0slave0"), "");

        ReadwriteSplittingDataSourceGroupRuleConfiguration ds1Group =
                new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                        "datasource1", "ds1",
                        Collections.singletonList("ds1slave0"), "");

        return new ReadwriteSplittingRuleConfiguration(Arrays.asList(ds0Group, ds1Group), new HashMap<>());
    }

    /**
     * 分库分表规则（与MySQL版本相同逻辑）
     */
    private ShardingRuleConfiguration createShardingRule() {
        ShardingRuleConfiguration config = new ShardingRuleConfiguration();

        List<PostgresDataSourceConfig.ShardingRuleItem> rules = dataSourceConfig.getRules();
        if (rules == null || rules.isEmpty()) {
            log.warn("未配置PostgreSQL分库分表规则");
            return config;
        }

        Set<String> registeredAlgorithms = new HashSet<>();

        for (PostgresDataSourceConfig.ShardingRuleItem rule : rules) {
            String tableName = rule.getTableName();
            PostgresDataSourceConfig.ShardingItem dbSharding = rule.getDatabaseSharding();
            PostgresDataSourceConfig.ShardingItem tblSharding = rule.getTableSharding();

            boolean needDbSharding = needSharding(dbSharding);
            boolean needTblSharding = needSharding(tblSharding);

            String actualDataNodes = buildActualDataNodes(tableName, dbSharding, tblSharding);
            ShardingTableRuleConfiguration tableRuleConfig =
                    new ShardingTableRuleConfiguration(tableName, actualDataNodes);

            if (needDbSharding) {
                tableRuleConfig.setDatabaseShardingStrategy(
                        new StandardShardingStrategyConfiguration(
                                dbSharding.getShardingColumn(), dbSharding.getAlgorithmType()));
                registerDbAlgorithm(config, dbSharding, registeredAlgorithms);
            }

            if (needTblSharding) {
                tableRuleConfig.setTableShardingStrategy(
                        new StandardShardingStrategyConfiguration(
                                tblSharding.getShardingColumn(), tblSharding.getAlgorithmType()));
                registerTblAlgorithm(config, tblSharding, tableName, registeredAlgorithms);
            }

            config.getTables().add(tableRuleConfig);

            log.info("配置PostgreSQL分片规则: {} → 库:{} / 表:{}",
                    tableName,
                    needDbSharding ? "[" + dbSharding.getShardingColumn() + "](" + dbSharding.getCount() + "个)" : "不分库",
                    needTblSharding ? "[" + tblSharding.getShardingColumn() + "](" + tblSharding.getCount() + "个)" : "不分表");
        }

        return config;
    }

    private boolean needSharding(PostgresDataSourceConfig.ShardingItem item) {
        return item != null
                && item.getCount() > 1
                && item.getShardingColumn() != null
                && !item.getShardingColumn().isEmpty();
    }

    private String buildActualDataNodes(String tableName,
                                        PostgresDataSourceConfig.ShardingItem dbSharding,
                                        PostgresDataSourceConfig.ShardingItem tblSharding) {
        boolean needDb = needSharding(dbSharding);
        boolean needTbl = needSharding(tblSharding);

        String dbPart = needDb
                ? "datasource${0.." + (dbSharding.getCount() - 1) + "}"
                : "datasource0";

        String tblPart = needTbl
                ? tableName + "_${0.." + (tblSharding.getCount() - 1) + "}"
                : tableName;

        return dbPart + "." + tblPart;
    }

    private void registerDbAlgorithm(ShardingRuleConfiguration config,
                                     PostgresDataSourceConfig.ShardingItem dbSharding,
                                     Set<String> registeredAlgorithms) {
        String algoType = dbSharding.getAlgorithmType();
        if (!registeredAlgorithms.contains(algoType)) {
            Properties props = new Properties();
            props.setProperty("database-count", String.valueOf(dbSharding.getCount()));
            config.getShardingAlgorithms().put(algoType, new AlgorithmConfiguration(algoType, props));
            registeredAlgorithms.add(algoType);
        }
    }

    private void registerTblAlgorithm(ShardingRuleConfiguration config,
                                      PostgresDataSourceConfig.ShardingItem tblSharding,
                                      String tableName,
                                      Set<String> registeredAlgorithms) {
        String algoType = tblSharding.getAlgorithmType();
        if (!registeredAlgorithms.contains(algoType)) {
            Properties props = new Properties();
            props.setProperty("table-count", String.valueOf(tblSharding.getCount()));
            props.setProperty("table-name", tableName);
            config.getShardingAlgorithms().put(algoType, new AlgorithmConfiguration(algoType, props));
            registeredAlgorithms.add(algoType);
        }
    }
}