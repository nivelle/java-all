package com.nivelle.base.jdk.java8;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class MethodFactoryImpl {

    private String name;

    private Integer age;

    String startsWith(String s) {
        return String.valueOf(s.charAt(0));
    }

    public MethodFactoryImpl(){}


}
