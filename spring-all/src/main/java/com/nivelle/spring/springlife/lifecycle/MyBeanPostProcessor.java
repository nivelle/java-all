package com.nivelle.spring.springlife.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 *
 */
public class MyBeanPostProcessor implements BeanPostProcessor {

    public MyBeanPostProcessor() {
        super();
        System.err.println("后置处理器本身构造器:BeanPostProcessor构造器！！");
    }

    @Override
    public Object postProcessBeforeInitialization(Object arg0, String arg1)
            throws BeansException {
        System.err.println("后置处理器,初始化,before,Initialization:改变实例,BeanPostProcessor接口方法postProcessBeforeInitialization对属性进行更改！");
        return arg0;
    }

    @Override
    public Object postProcessAfterInitialization(Object arg0, String arg1)
            throws BeansException {
        System.err.println("后置处理器,初始化,after,Initialization:改变实例,BeanPostProcessor接口方法postProcessAfterInitialization对属性进行更改！");
        return arg0;
    }

}

