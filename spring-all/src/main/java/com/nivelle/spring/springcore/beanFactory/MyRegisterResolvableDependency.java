package com.nivelle.spring.springcore.beanFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * 解决按类型装配的时候，多个实现类导致的异常
 *
 * @author fuxinzhong
 * @date 2020/12/03
 */
@Component
public class MyRegisterResolvableDependency implements BeanFactoryPostProcessor {


    /**
     * 当有其他类要注入MyComponent 类型的对象时，就给他注入我们这里自己创建的 MyComponentAImpl 对象
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerResolvableDependency(MyComponent.class, new MyComponentAImpl());
    }
}
