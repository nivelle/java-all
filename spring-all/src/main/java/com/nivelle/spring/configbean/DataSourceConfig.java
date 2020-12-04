package com.nivelle.spring.configbean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 数据源配置
 *
 * @author nivelle
 * @date 2019/08/09
 */
@Configuration
public class DataSourceConfig {


    /**
     * @return
     */
    @Bean(name = "slaveDataSource")
    @Qualifier("slaveDataSource")
    //当一个接口有多个实现类注入到spring容器中，使用@AutoWired 是 byType的，而这些实现类类型都相同，此时就需要@Qualifirer明确指定使用那个实现类。
    //@Qualifier是byName的
    @Primary //当有多个实例时，用该注解表明先于未加该注解的实例注入，并不是表示它是主实例
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }
    /**
     * 数据源
     *
     * @return
     */
    @Bean(name = "masterDataSource")
    @Qualifier("masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }


    /**
     * 事物管理器
     *
     * @param dataSource
     * @return
     */
    @Bean(name = "masterTransactionManager")
    @Primary
    public DataSourceTransactionManager masterTransactionManager(@Qualifier("masterDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
