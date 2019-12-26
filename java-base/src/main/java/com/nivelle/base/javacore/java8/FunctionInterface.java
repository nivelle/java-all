package com.nivelle.base.javacore.java8;


@FunctionalInterface
public interface FunctionInterface<S,T> {

    //@FunctionalInterface标记在接口上，“函数式接口”是指仅仅只包含一个抽象方法的接口。
    //将一段功能逻辑赋给FunctionTest.convert,这个方法是具体功能的抽象
    T convert(S str);

}
