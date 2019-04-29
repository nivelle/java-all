package com.nivelle.guide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ServletComponentScan
@EnableCaching//开启缓存注解,mybatis使用redis作为缓存
public class SpringBootAllApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAllApplication.class, args);
        System.out.print("启动成功！！");

    }


}

