package com.sandbox.demo.mysql.config;

/**
 * @description: ShardingSphere 5.3.2 核心配置
 * @author: 0101
 * @create: 2026/05/02
 */

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ShardingSphere 5.3.2 核心配置
 * <p>
 * 整合 4 个物理数据源，配置读写分离（两主两从，轮询负载均衡），
 * 创建统一 DataSource 对上层透明。
 *
 * @author 0101
 * @since 2026-05-02
 */
@Slf4j
@Configuration
public class ShardingSphereConfig {

    /**
     * 创建 ShardingSphere 管理的统一数据源
     * <p>
     * 读写分离架构：
     * datasource0 → 写 ds0 / 读 ds0slave0
     * datasource1 → 写 ds1 / 读 ds1slave0
     * 从库负载均衡算法：ROUND_ROBIN（轮询）
     */
    @Bean
    @Primary
    public DataSource shardingSphereDataSource(DataSource ds0DataSource,
                                               DataSource ds1DataSource,
                                               DataSource ds0slave0DataSource,
                                               DataSource ds1slave0DataSource) throws SQLException {
        // 1. 注册所有物理数据源
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
        dataSourceMap.put("ds0", ds0DataSource);
        dataSourceMap.put("ds1", ds1DataSource);
        dataSourceMap.put("ds0slave0", ds0slave0DataSource);
        dataSourceMap.put("ds1slave0", ds1slave0DataSource);

        // 2. 配置读写分离规则
        Collection<RuleConfiguration> ruleConfigs = new ArrayList<>();
        ruleConfigs.add(createReadwriteSplittingRule());

        // 3. 配置属性（打印 SQL，方便调试）
        Properties props = new Properties();
        props.setProperty("sql-show", Boolean.TRUE.toString());

        // 4. 创建 ShardingSphere 数据源
        DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, ruleConfigs, props);
        log.info("ShardingSphere 数据源创建成功，管理 {} 个物理数据源", dataSourceMap.size());
        return dataSource;
    }

    /**
     * 构建读写分离规则
     */
    private ReadwriteSplittingRuleConfiguration createReadwriteSplittingRule() {
        // 逻辑数据源 datasource0：写 ds0，读 ds0slave0
        ReadwriteSplittingDataSourceRuleConfiguration ds0Config =
                new ReadwriteSplittingDataSourceRuleConfiguration(
                        "datasource0",
                        "",
                        "ds0",
                        Collections.singletonList("ds0slave0"),
                        "ROUND_ROBIN"
                );

        // 逻辑数据源 datasource1：写 ds1，读 ds1slave0
        ReadwriteSplittingDataSourceRuleConfiguration ds1Config =
                new ReadwriteSplittingDataSourceRuleConfiguration(
                        "datasource1",
                        "",
                        "ds1",
                        Collections.singletonList("ds1slave0"),
                        "ROUND_ROBIN"
                );

        List<ReadwriteSplittingDataSourceRuleConfiguration> configs = Arrays.asList(ds0Config, ds1Config);

        // 负载均衡器配置（可选，这里为空 Map）
        return new ReadwriteSplittingRuleConfiguration(configs, new HashMap<>());
    }
}

