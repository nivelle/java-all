package com.nivelle.guide.springboot.initresource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class initResource2 implements CommandLineRunner {
    @Override
    public void run(String... args) {
        System.out.println("初始化资源 2");
    }
}