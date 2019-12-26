package com.nivelle.spring.springlife.initdemo;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class InitSpringBean implements InitializingBean {

    private String name;
    private int age;


    public InitSpringBean() {
        System.err.println("----> InitSequenceBean: constructor!!! ");
    }

    public InitSpringBean(String name, int age) {
        this.age = 10;
        this.name = "nivelle";
        System.err.println("----> InitSequenceBean: constructor!!! name=" + name + ":" + "age=" + age);
    }

    @PostConstruct
    public void postConstruct() {
        System.err.println("----> InitSequenceBean: postConstruct!!! before constructor");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.name = "nivelle2";
        System.err.println("----> InitSequenceBean: afterPropertiesSet!!! after constructor");
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

}
