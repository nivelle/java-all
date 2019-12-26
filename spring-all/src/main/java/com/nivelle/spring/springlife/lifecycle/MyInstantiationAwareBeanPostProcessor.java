package com.nivelle.spring.springlife.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import java.beans.PropertyDescriptor;

public class MyInstantiationAwareBeanPostProcessor extends
        InstantiationAwareBeanPostProcessorAdapter {

    public MyInstantiationAwareBeanPostProcessor() {
        super();
        System.err.println("后置处理器通过继承InstantiationAwareBeanPostProcessorAdapter,构造器本身:这是InstantiationAwareBeanPostProcessorAdapter实现类构造器！！");
    }

    // 接口方法、实例化Bean之前调用
    @Override
    public Object postProcessBeforeInstantiation(Class beanClass,
                                                 String beanName) throws BeansException {
        System.err.println("实例化后=》后置处理器=》before,Instantiation:改变类定义,InstantiationAwareBeanPostProcessor=》postProcessBeforeInstantiation方法");
        return null;
    }

    // 接口方法、实例化Bean之后调用
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        System.err.println("实例化后=>后置处理器=>after,Instantiation:改变实例定义,InstantiationAwareBeanPostProcessor=》postProcessAfterInitialization方法");
        return bean;
    }

    // 接口方法、设置某个属性时调用
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs,
                                                    PropertyDescriptor[] pds, Object bean, String beanName)
            throws BeansException {
        System.err.println("实例化=>后置处理器=>Instantiation property:设置某个属性，InstantiationAwareBeanPostProcessor=》postProcessPropertyValues方法");
        return pvs;
    }
}
