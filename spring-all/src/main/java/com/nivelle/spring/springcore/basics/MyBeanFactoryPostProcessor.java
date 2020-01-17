package com.nivelle.spring.springcore.basics;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * 自定义 BeanFactoryPostProcessor
 *
 * @Author nivelle
 */
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    public MyBeanFactoryPostProcessor() {
        super();
        System.err.println("工厂类后置处理器本身构造器:BeanFactoryPostProcessor！");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory arg0)
            throws BeansException {
        System.err.println("工厂类后置处理器,BeanFactoryPostProcessor:调用postProcessBeanFactory方法,修改beanDefinition生产线");
        BeanDefinition bd = arg0.getBeanDefinition("person");
        bd.getPropertyValues().addPropertyValue("phone", "110");
    }

}
