package com.nivelle.spring.springcore.beanFactory;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;

/**
 * 自动装配
 *
 * @author fuxinzhong
 * @date 2020/12/04
 */
public class AutowiringConfiguration {

    @Bean(autowire = Autowire.BY_TYPE)
    public Person person() {
        return new Person();
    }

    @Bean
    public BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return new MyBeanFactoryPostProcessorImpl();
    }
}
