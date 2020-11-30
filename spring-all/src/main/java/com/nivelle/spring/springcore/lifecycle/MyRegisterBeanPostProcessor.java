package com.nivelle.spring.springcore.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

/**
 * 自定义 BeanDefinitionRegistryPostProcessor 在 InstantiationAwareBeanPostProcessor 创建实例之前执行
 *
 * @author nivelle
 * @date 2019/09/26
 */

@Component
public class MyRegisterBeanPostProcessor implements BeanDefinitionRegistryPostProcessor {


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    /**
     * 直接注册一个bean定义
     *
     * @param registry
     * @throws BeansException
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        BeanDefinition myBeanDefinition = new RootBeanDefinition(Animal.class);
        registry.registerBeanDefinition("beanDefinition's_animal", myBeanDefinition);
        return;
    }

}
