package com.nivelle.spring.springboot.hock;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

/**
 * 拿到的应该是DefaultListableBeanFactory，
 * 因为这个BeanFactory是BeanFactory一族的最底层的BeanFactory实现类，拥有所有父BeanFactory
 */
@Component
public class MyBeanFactory implements BeanFactoryAware {
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.err.println("spring 钩子方法BeanFactoryAware");
        this.beanFactory = beanFactory;
    }
}
