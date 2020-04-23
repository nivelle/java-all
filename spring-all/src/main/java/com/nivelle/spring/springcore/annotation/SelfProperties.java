package com.nivelle.spring.springcore.annotation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * 该注解声明如果该类被定义为一个bean,则对应的bean实例的属性值将来自配置文件中前缀为myConfig的同名属性但是这个注解本身并不会导致该类被作为一个bean注册
 * 使用 @EnableConfigurationProperties则会将
 *
 * application.properties 配置默认的key实现原理
 *
 * @author nivell
 * @date 2020/01/16
 */
@ConfigurationProperties(prefix = "myConfig")
public class SelfProperties {

    /**
     * 要用全key名字
     */
    @Value("${myConfig.desc}")
    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void printDesc() {
        System.out.println(this.desc);
    }
}
