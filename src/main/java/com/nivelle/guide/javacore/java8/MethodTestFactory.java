package com.nivelle.guide.javacore.java8;

public interface MethodTestFactory<M extends MethodTest> {

    M create(String name,Integer age);
}
