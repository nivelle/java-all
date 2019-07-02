package com.nivelle.guide;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ServletComponentScan//扫描servlet组件注解
@EnableCaching//开启缓存注解,mybatis使用redis作为缓存
@EnableScheduling//开启定时任务注解
@EnableRabbit//开启rabbitMQ注解支持
public class SpringBootAllApplication {


    public static void main(String[] args) {
        SpringApplication.run(SpringBootAllApplication.class, args);
        System.out.print("启动成功！！");
    }
}

