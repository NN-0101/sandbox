package com.sandbox.home.config;

import com.google.common.collect.Lists;
import com.sandbox.home.enumeration.DataSourceEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ShardingSphere 核心配置（5.0.0）
 * <p>
 * 整合多数据源，配置读写分离（主库 ds0/ds1，从库 ds0slave0/ds1slave0，轮询负载均衡）
 * 和字段加密（通过 EncryptConfig），创建统一 DataSource 对上层透明。
 *
 * @author 0101
 * @since 2026-03-13
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({DataSourceConfig.class})
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan(basePackages = {"com.baomidou.mybatisplus.core.mapper", "com.sandbox.home.mapper"},
        sqlSessionTemplateRef = "sqlSessionTemplate")
public class ShardingSphereConfig {

    private static final String RULE_CONFIG_LOAD_BALANCER_NAME = "load_balancer";

    @Resource
    private DataSourceConfig dataSourceConfig;

    @Autowired
    private EncryptConfig encryptConfig;

    /** 注册所有数据源（主库 + 从库） */
    @Bean("dataSourceMap")
    public Map<String, DataSource> dataSourceMap() {
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
        dataSourceMap.put(DataSourceEnum.DS0.getValue(), dataSourceConfig.ds0());
        dataSourceMap.put(DataSourceEnum.DS1.getValue(), dataSourceConfig.ds1());
        dataSourceMap.put(DataSourceEnum.DS0SLAVE0.getValue(), dataSourceConfig.ds0slave0());
        dataSourceMap.put(DataSourceEnum.DS1SLAVE0.getValue(), dataSourceConfig.ds1slave0());
        return dataSourceMap;
    }

    /** 创建集成读写分离和加密的 ShardingSphere 数据源 */
    @Bean("dataSource")
    public DataSource dataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = dataSourceMap();

        Collection<RuleConfiguration> ruleConfigurations = new ArrayList<>();
        ruleConfigurations.add(readWriteConfig());

        EncryptRuleConfiguration encryptRuleConfiguration = encryptConfig.buildEncryptRule();
        if (encryptRuleConfiguration != null) {
            ruleConfigurations.add(encryptRuleConfiguration);
        }

        Properties p = new Properties();
        p.setProperty("sql-show", Boolean.TRUE.toString());

        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, ruleConfigurations, p);
    }

    /** 读写分离规则：datasource0(ds0+ds0slave0) 和 datasource1(ds1+ds1slave0)，轮询负载均衡 */
    public ReadwriteSplittingRuleConfiguration readWriteConfig() {
        ReadwriteSplittingDataSourceRuleConfiguration configuration1 =
                new ReadwriteSplittingDataSourceRuleConfiguration(
                        "datasource0", "", "ds0", List.of("ds0slave0"), "ROUND_ROBIN");

        ReadwriteSplittingDataSourceRuleConfiguration configuration2 =
                new ReadwriteSplittingDataSourceRuleConfiguration(
                        "datasource1", "", "ds1", List.of("ds1slave0"), "ROUND_ROBIN");

        ArrayList<ReadwriteSplittingDataSourceRuleConfiguration> configs =
                Lists.newArrayList(configuration1, configuration2);

        Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers = new HashMap<>(1);
        loadBalancers.put(RULE_CONFIG_LOAD_BALANCER_NAME,
                new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()));

        return new ReadwriteSplittingRuleConfiguration(configs, loadBalancers);
    }

    @Bean
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean("sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}