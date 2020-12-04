package com.nivelle.spring.springcore.beanFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/12/03
 */
public class MyRegisterResolvableDependency implements BeanFactoryPostProcessor {


    /**
     * 当有其他类要注入 Component类型的对象时，就给他注入我们这里自己创建的 ComponentAImple对象
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerResolvableDependency(Component.class,new ComponentAImpl());
    }
}
