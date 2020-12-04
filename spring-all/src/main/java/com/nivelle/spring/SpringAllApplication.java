package com.nivelle.spring;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.nivelle.spring.springcore.annotation.MyTypeFilter;
import com.nivelle.spring.springcore.event.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
/**
 * 扫描servlet组件注解
 */
//@ServletComponentScan
/**
 * 开启缓存注解,mybatis使用redis作为缓存
 * @author nivelle
 */
//@EnableCaching
@EnableScheduling
//@EnableDubbo
//@EnableAsync
/**
 * @ComponentScan.Filter 使用过滤器来指定要自动扫描
 */
//@EnableConfigurationProperties
//@ComponentScan(includeFilters ={@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,classes = {MyTypeFilter.class})})
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

