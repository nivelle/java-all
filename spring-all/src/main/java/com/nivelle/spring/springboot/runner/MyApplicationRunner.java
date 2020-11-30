package com.nivelle.spring.springboot.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * ApplicationRunner
 *
 * @author nivelle
 * @date 2019/10/16
 */
@Component
public class MyApplicationRunner implements ApplicationRunner {


    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("ApplicationRunner 初始化资源，ApplicationArguments=" + args);
    }

}
