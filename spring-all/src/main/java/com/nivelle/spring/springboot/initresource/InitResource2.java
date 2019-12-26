package com.nivelle.spring.springboot.initresource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class InitResource2 implements CommandLineRunner {
    @Override
    public void run(String... args) {
        System.err.println("CommandLineRunner 初始化资源 2");
    }
}