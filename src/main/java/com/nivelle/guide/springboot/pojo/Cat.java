package com.nivelle.guide.springboot.pojo;

public class Cat implements Animal {
    private String name;
    public void say() {
        System.out.println("I am " + name + "!");
    }
    public void setName(String name) {
        this.name = name;
    }
}

