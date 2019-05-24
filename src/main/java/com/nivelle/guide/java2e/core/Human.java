package com.nivelle.guide.java2e.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Human {

    private int age;

    private String name;

    public Human(int age, String name) {
        this.age = age;
        this.name = name;
    }
}
