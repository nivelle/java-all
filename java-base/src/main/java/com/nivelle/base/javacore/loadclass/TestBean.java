package com.nivelle.base.javacore.loadclass;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestBean {

    private String message = "fuck";


    public TestBean(String message) {
        this.message = message;
    }
}
