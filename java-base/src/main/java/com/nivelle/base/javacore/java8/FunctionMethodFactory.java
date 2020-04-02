package com.nivelle.base.javacore.java8;

@FunctionalInterface
public interface FunctionMethodFactory<M extends MethodFactoryImpl> {

    M create(String name,Integer age);
}
