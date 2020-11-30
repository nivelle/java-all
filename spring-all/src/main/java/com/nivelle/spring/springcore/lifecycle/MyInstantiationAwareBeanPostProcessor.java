package com.nivelle.spring.springcore.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;

/**
 * InstantiationAwareBeanPostProcessorAdapter 初始化方法集成器
 *
 * @Author
 */
@Component
public class MyInstantiationAwareBeanPostProcessor extends
        InstantiationAwareBeanPostProcessorAdapter {

    public MyInstantiationAwareBeanPostProcessor() {
        super();
        System.out.println("InstantiationAwareBeanPostProcessorAdapter构造函数");
    }


    /**
     * 接口方法:实例化 Bean之前调用
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInstantiation(Class beanClass,
                                                 String beanName) throws BeansException {
        System.out.println(beanName + ":" + "调用构造函数前,before,Instantiation:改变类定义,InstantiationAwareBeanPostProcessor=》postProcessBeforeInstantiation方法");
        return null;
    }

    /**
     * 接口方法:初始化 Bean之后调用
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        System.out.println(beanName + ":" + "调用构造函数后,改变实例定义,InstantiationAwareBeanPostProcessor=》postProcessAfterInitialization方法");
        return bean;
    }


    /**
     * 接口方法、设置某个属性时调用
     * @param pvs
     * @param pds
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs,
                                                    PropertyDescriptor[] pds, Object bean, String beanName)
            throws BeansException {
        System.out.println(beanName + ":" + "调用构造函数后,设置某个属性,InstantiationAwareBeanPostProcessor=》postProcessPropertyValues()方法");
        return pvs;
    }
}
