package com.nivelle.spring.springcore.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 自定义 BeanPostProcessor
 *
 * @Author nivelle
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    public MyBeanPostProcessor() {
        super();
        System.out.println("构造函数:BeanPostProcessor");
    }


    /**
     * 初始化方法:
     * <p>
     * 1. 通过InitializingBean接口实现的afterPropertiesSet()方法;
     * <p>
     * 2. xml方式指定的bean的 init-method 初始化方法;
     * <p>
     * 3. JSR-250 注解 @PostConstruct 注解的初始化方法;
     * <p>
     * 4.Java 配置类中 @Bean(initMethod = "init") 指定的初始化方法;
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        System.out.println(beanName + ":" + "MyBeanPostProcessor 后置处理器在Initialization或init方法之前调用 -》postProcessBeforeInitialization");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        System.out.println(beanName + ":" + "MyBeanPostProcessor 后置处理器在Initialization或init方法之后调用-》postProcessAfterInitialization");
        return bean;
    }

}

