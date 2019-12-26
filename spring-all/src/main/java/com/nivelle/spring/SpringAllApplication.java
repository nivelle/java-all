package com.nivelle.spring;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.nivelle.spring.springboot.listener.context.MyContextClosedEventListenerListener;
import com.nivelle.spring.springboot.listener.context.MyContextRefreshedEventListener;
import com.nivelle.spring.springboot.listener.context.MyContextStartedEventListener;
import com.nivelle.spring.springboot.listener.context.MyContextStopedEventEventListener;
import com.nivelle.spring.springboot.listener.springApplicationRunListeners.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * nivelle
 */
@SpringBootApplication
@ServletComponentScan//扫描servlet组件注解
@EnableCaching//开启缓存注解,mybatis使用redis作为缓存
@EnableScheduling//开启定时任务注解
@EnableRabbit//开启rabbitMQ注解支持
@EnableDubbo//开启dubbo
@EnableKafka
@ComponentScan(basePackages = {"com.nivelle.spring","com.nivelle.base"})
public class SpringAllApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(SpringAllApplication.class);

        /**
         * ApplicationListener 接口监听类
         */
        springApplication.addListeners(new MyApplicationStartingEventListener());
        springApplication.addListeners(new MyApplicationEnvironmentPreparedEventListener());
        springApplication.addListeners(new MyApplicationReadyEventListener());
        //与MyApplicationStartedEventListener互斥
        springApplication.addListeners(new MyApplicationFailedEventListener());
        springApplication.addListeners(new MyApplicationStartedEventListener());

        /**
         * Spring内置的事件
         */
        springApplication.addListeners(new MyContextStartedEventListener());
        springApplication.addListeners(new MyContextRefreshedEventListener());
        springApplication.addListeners(new MyContextClosedEventListenerListener());
        springApplication.addListeners(new MyContextStopedEventEventListener());


        springApplication.run(args);
        System.err.println("启动成功！！");
    }
}

