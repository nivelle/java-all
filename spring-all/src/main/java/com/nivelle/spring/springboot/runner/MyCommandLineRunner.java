package com.nivelle.spring.springboot.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * If you need to run some specific code once the SpringApplication has started, you can implement the ApplicationRunner or CommandLineRunner interfaces.
 * Both interfaces work in the same way and offer a single run method, which is called just before SpringApplication.run(…​) completes.
 */

/**
 * 1. 如果想在SpringApplication启动完成时，执行一些初始化的操作，可以实现 ApplicationRunner或者CommanLinerRunner接口，
 * 2. 他们是以同样的方式提供服务，他们在SpringApplication.run()方法执行完之前执行。
 * 3. 可以有多个实现，通过@Order 排序
 */
@Component
@Order(1)
public class MyCommandLineRunner implements CommandLineRunner {
    @Override
    public void run(String... args) {
        System.out.println("CommandLineRunner 初始化资源 args=" + args);
    }
}