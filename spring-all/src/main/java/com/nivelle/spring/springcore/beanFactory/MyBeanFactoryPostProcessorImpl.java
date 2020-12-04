package com.nivelle.spring.springcore.beanFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * 忽略自动装配
 *
 * @author fuxinzhong
 * @date 2020/12/04
 */
public class MyBeanFactoryPostProcessorImpl implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        //忽略对Person的自动装配 方法的确是忽略给定接口的自动装配。但是这个自动装配不是 @Autowired
        configurableListableBeanFactory.ignoreDependencyInterface(Person.class);
    }
}
