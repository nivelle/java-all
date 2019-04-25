package com.nivelle.programming.java2e.annotion.myannotion;

/**
 * 注解测试
 **/
public class MyAnnotationTest {

    public static void main(String[] args) {
        UserLogin test = Container.getBean();
        test.loginTest();
    }
}

