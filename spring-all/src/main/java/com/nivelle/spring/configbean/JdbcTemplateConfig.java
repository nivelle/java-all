package com.nivelle.spring.configbean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 数据源(jdbcTemplate配置主从两个数据源)
 *
 * @author nivelle
 * @date 2019/08/08
 */
@Configuration
public class JdbcTemplateConfig {

    /**
     * jdbcTemplate配置
     *
     * @param dataSource
     * @return
     */
    @Bean(name = "masterJdbcTemplate")
    public JdbcTemplate masterdbcTemplate(
            @Qualifier("masterDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 从数据库
     * @param dataSource
     * @return
     */
    /**
     * TODO @Bean 方法参数注入的时候, 先按照名字注入，若名字没匹配到则按照类型注入
     */
    @Bean(name = "slaveJdbcTemplate")
    public JdbcTemplate slaveJdbcTemplate(
            @Qualifier("slaveDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


}
