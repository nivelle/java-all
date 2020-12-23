package com.nivelle.rpc;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.nivelle.rpc.dubbo.model.Cat;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@SpringBootApplication
@EnableDubbo
//@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.nivelle.rpc.dubbo.model",  "com.nivelle.rpc.**"}, includeFilters = {})
@EnableAsync
@EnableSwagger2 //swagger2支持
public class RpcBaseApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(RpcBaseApplication.class);
        springApplication.run(args);
    }


    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            //System.out.println("springBoot 初始化时所有bean定义:");
            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }
            //System.out.println("springBoot 初始化时所有bean实例定义:");
            String[] objectNames = ctx.getBeanNamesForType(Cat.class);
            for (String objectName : objectNames) {
                System.out.println(objectName);
            }
        };
    }

}
