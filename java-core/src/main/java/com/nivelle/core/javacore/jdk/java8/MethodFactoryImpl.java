package com.nivelle.core.javacore.jdk.java8;

import lombok.Data;

@Data
public class MethodFactoryImpl {

    private String name;

    private Integer age;

    String startsWith(String s) {
        return String.valueOf(s.charAt(0));
    }

    public MethodFactoryImpl(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public MethodFactoryImpl() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
