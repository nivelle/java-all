package com.nivelle.guide.springboot.hock;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 实现ApplicationContextAware接口可以获取ApplicationContext
 */
@Component
public class SpringContextAssisor implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextAssisor.applicationContext = applicationContext;
    }

    public  Object getBeanDefinition(String name) {
        return applicationContext.getBean(name);
    }

    public <T> T getBeanDefinition(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

}
