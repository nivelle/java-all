package com.nivelle.spring.springcore.lifecycle;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * spring Bean 初始化接口实现
 *
 * @Author nivelle
 */
@Component
public class InitSpringBean implements InitializingBean {

    private String name;
    private int age;


    public InitSpringBean() {
        System.out.println("----> InitSpringBean: constructor!!! ");
    }

    public InitSpringBean(String name, int age) {
        this.age = 10;
        this.name = "nivelle";
        System.err.println("----> InitSpringBean: constructor!!! name=" + name + ":" + "age=" + age);
    }

    @PostConstruct
    public void postConstruct() {
        System.err.println("----> InitSpringBean: postConstruct!!! before constructor");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.name = "nivelle2";
        System.err.println("----> InitSpringBean: afterPropertiesSet!!! after constructor");
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

}
