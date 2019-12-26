package com.nivelle.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.nivelle.container.sci")
public class JavaContainerAllApplication {

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            System.err.println("启动参数:" + args[i]);
        }
        SpringApplication.run(JavaContainerAllApplication.class, args);
    }

}
