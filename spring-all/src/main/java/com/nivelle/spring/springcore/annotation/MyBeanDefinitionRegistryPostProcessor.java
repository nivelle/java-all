package com.nivelle.spring.springcore.annotation;

import com.nivelle.base.pojo.UserInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

/**
 * BeanDefinitionRegistryPostProcessor 接口可以看作是 BeanFactoryPostProcessor 和 ImportBeanDefinitionRegistrar的功能集合，
 * 既可以获取和修改BeanDefinition的元数据，也可以实现BeanDefinition的注册、移除等操作。
 *
 * 执行时机 : 在BeanDefinitionRegistry的标准初始化之后所有其他一般的BeanFactoryPostProcessor执行之前执行，此时所有的bean定义已经加载但是还没有bean实例被创建。
 *
 * @author fuxinzhong
 * @date 2019/08/25
 */
@Component
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final String beanName = "userInfo";

    /**
     * 在应用上下文内部的bean definition registry的标准初始化之后修改对其进行修改
     *
     * 此时所有常规的bean定义已经被加载，但是还没有bean被实例化。
     *
     * @param registry
     * @throws BeansException
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        System.err.println("postProcessBeanDefinitionRegistry");
        BeanDefinition beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(UserInfo.class)
                .getBeanDefinition();

        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.err.println("postProcessBeanFactory");
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        propertyValues.addPropertyValue("userName", "fuck you !!!");
    }
}
