package com.nivelle.bigdata.clickhouse;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/03/02
 */
@Data
@PropertySource("classpath:config/clickhouse.properties")
@Configuration
public class ClickhouseConfig {

    @Value("${click.house.driver.class.name}")
    private String driverClassName;
    @Value("${click.house.url}")
    private String url;
}
