package com.nivelle.spring.springcore.annotation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * 1. 该注解声明如果该类被定义为一个bean,则对应的bean实例的属性值将来自配置文件中前缀为spring.my.config的同名属性
 *
 * 2. 但是这个注解本身并不会导致该类被作为一个bean注册使用, 需要搭配 @EnableConfigurationProperties则会将
 *
 * 3. 可以在Class 或者 @Bean 注解的方法上
 *
 * application.properties 配置默认的key实现原理
 *
 * @author nivelle
 * @date 2020/01/16
 */

/**
 * 三种使用方法：
 *
 * 1. @Component + @ConfigurationProperties
 *
 * 2. 配置类中 @Bean + @ConfigurationProperties
 *
 * 3. @EnableConfigurationProperties + @ConfigurationProperties;Pojo类上注解@ConfigurationProperties，在启动类上注解@EnableConfigurationProperties
 */
@ConfigurationProperties(prefix = "spring.my.config")
public class SelfProperties {

    /**
     * 要用全key名字
     */
    @Value("${spring.my.config.desc}")
    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "SelfProperties{" +
                "desc='" + desc + '\'' +
                '}';
    }

    public void printDesc() {
        System.out.println(this.desc);
    }
}
