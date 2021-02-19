package com.nivelle.base.jdk.java8;

@FunctionalInterface
public interface FunctionMethodFactory<M extends MethodFactoryImpl> {

    M create(String name, Integer age);
}
