package com.nivelle.bigdata.clickhouse;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/02/22
 */
@Configuration
@Data
public class ClickHouseConfig {

    @Value("spring.datasource.click.driverClassName")
    private String driverClassName;
    @Value("spring.datasource.click.url")
    private String url;
//    @Value("spring.datasource.click.initialSize")
//    private int initialSize;
//    @Value("spring.datasource.click.maxActive")
//    private int maxActive;
//    @Value("spring.datasource.click.minIdle")
//    private int minIdle;
//    @Value("spring.datasource.click.maxWait")
//    private int maxWait;


    @Bean
    public DataSource dataSource() {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(url);
        datasource.setDriverClassName(driverClassName);
//        datasource.setInitialSize(initialSize);
//        datasource.setMinIdle(maxActive);
//        datasource.setMaxActive(minIdle);
//        datasource.setMaxWait(maxWait);
        return datasource;
    }

}
