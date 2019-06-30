package com.nivelle.guide.javacore.classloader;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestBean {

    private String message = "fuck";


    public TestBean(String message) {
        this.message = message;
    }
}
