package com.nivelle.base.javacore.java8;

public interface MethodTestFactory<M extends MethodTest> {

    M create(String name,Integer age);
}
