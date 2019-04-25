package com.nivelle.programming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ServletComponentScan
@EnableCaching
public class SpringBootAllApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAllApplication.class, args);
        System.out.print("启动成功！！");

    }


}

