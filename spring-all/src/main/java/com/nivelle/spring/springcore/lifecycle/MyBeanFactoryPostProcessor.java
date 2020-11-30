package com.nivelle.spring.springcore.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义 BeanFactoryPostProcessor
 *
 * @Author nivelle
 */
@Component
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    public MyBeanFactoryPostProcessor() {
        super();
        System.out.println("构造函数:BeanFactoryPostProcessor ");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory arg0)
            throws BeansException {
        System.out.println("工厂类后置处理器,BeanFactoryPostProcessor:调用postProcessBeanFactory方法,修改beanDefinition属性:");
        BeanDefinition bd = null;
        try {
            bd = arg0.getBeanDefinition("person");
        } catch (NoSuchBeanDefinitionException e) {
            System.out.println("指定bean不存在");
            return;
        }
        bd.getPropertyValues().addPropertyValue("phone", "110");
    }

}
