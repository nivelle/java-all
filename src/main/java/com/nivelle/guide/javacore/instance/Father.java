package com.nivelle.guide.javacore.instance;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Father {

    private int age;

    private String name;

    public Father(int age, String name) {
        this.age = age;
        this.name = name;
    }
}
