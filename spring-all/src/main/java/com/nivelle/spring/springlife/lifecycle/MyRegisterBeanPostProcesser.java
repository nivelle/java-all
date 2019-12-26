package com.nivelle.spring.springlife.lifecycle;

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
 * @author fuxinzhong
 * @date 2019/09/26
 */

@Component
public class MyRegisterBeanPostProcesser implements BeanDefinitionRegistryPostProcessor {


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        BeanDefinition myBeanDefinition = new RootBeanDefinition(Animal.class);
        registry.registerBeanDefinition("animal", myBeanDefinition);
        return;
    }

}
