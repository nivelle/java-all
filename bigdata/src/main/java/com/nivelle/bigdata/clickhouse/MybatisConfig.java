package com.nivelle.bigdata.clickhouse;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/03/02
 */

@Configuration
@MapperScan(basePackages = "com.nivelle.bigdata.clickhouse.mapper", sqlSessionFactoryRef = "clickHouseSqlSessionFactory")
public class MybatisConfig {

    @Autowired
    ClickhouseConfig clickhouseConfig;

    static final String LOCATION="classpath:/mapper/*.xml";


    /**
     * 数据源
     *
     * @return
     */
    @Bean(name = "clickHouseDataSource")
    public DataSource clickHouseDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(clickhouseConfig.getDriverClassName());
        dataSource.setUrl(clickhouseConfig.getUrl());

        return dataSource;
    }

    @Bean(name = "clickHouseSqlSessionFactory")
    @DependsOn(value = "clickHouseDataSource")
    public SqlSessionFactory clickHouseSqlSessionFactory(@Qualifier("clickHouseDataSource") DataSource clickHouseDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(clickHouseDataSource);

        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(LOCATION));
        return sessionFactory.getObject();
    }

}

