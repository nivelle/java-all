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

@MapperScan(basePackages = "com.nivelle.bigdata.clickhouse.mapper", sqlSessionFactoryRef = "clickHouseSqlSessionFactory")
@Configuration
public class MyBatisConfing {
    @Autowired
    ClickhouseConfing clickhouseConfing;
    static final String DRIVER_CLASS_NAME = "ru.yandex.clickhouse.ClickHouseDriver";
    static final String CLICKHOUSE_URL = "jdbc:clickhouse://39.105.201.242:8123/nd_bi_data?characterEncoding=UTF-8&useSSL=false";
    static final String LOCATION = "classpath:/*.xml";

    /**
     * 数据源
     *
     * @return
     */
    @Bean(name = "clickHouseDataSource")
    public DataSource clickHouseDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(clickhouseConfing.getDriverClassName());
        dataSource.setUrl(clickhouseConfing.getUrl());
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

