package com.nivelle.spring.springboot.hock;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 1. 当一个Bean实现InitializingBean，#afterPropertiesSet方法里面可以添加自定义的初始化方法或者做一些资源初始化操作
 * (Invoked by a BeanFactory after it has set all bean properties supplied ==> "当BeanFactory
 * 设置完所有的Bean属性之后才会调用#afterPropertiesSet方法")。
 * <p>
 * 2. destroy可以添加自定义的一些销毁方法或者资源释放操作(Invoked by a BeanFactory on destruction of a singleton
 * ==>"单例销毁时由BeanFactory调用#destroy")
 */
@Component
public class MyInitializingAndDisposable implements InitializingBean, DisposableBean {

    @Override
    public void destroy() throws Exception {
        System.err.println("DisposableBean=>destroy:释放资源");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.err.println("InitializingBean=>afterPropertiesSet:初始化资源");
    }
}




