package com.nivelle.programming.java2e.java8;

public interface MethodTestFactory<M extends MethodTest> {

    M create(String name,Integer age);
}
