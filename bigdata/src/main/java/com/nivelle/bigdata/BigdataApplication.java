package com.nivelle.bigdata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.nivelle.bigdata.clickhouse.mapper")
public class BigdataApplication {

    public static void main(String[] args) {
        SpringApplication.run(BigdataApplication.class, args);
    }

}
