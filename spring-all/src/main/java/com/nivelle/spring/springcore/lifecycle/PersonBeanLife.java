package com.nivelle.spring.springcore.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 实现了常用扩展的接口
 * <p>
 * InitializingBean:当一个Bean实现InitializingBean，#afterPropertiesSet方法里面可以添加自定义的初始化方法或者做一些资源初始化操作
 * (Invoked by a BeanFactory after it has set all bean properties supplied ==> "当BeanFactory
 * 设置完所有的Bean属性之后才会调用#afterPropertiesSet方法")。
 * <p>
 * DisposableBean:destroy可以添加自定义的一些销毁方法或者资源释放操作(Invoked by a BeanFactory on destruction of a singleton
 * ==>"单例销毁时由BeanFactory调用#destroy")
 * <p>
 * BeanFactoryAware:拿到的应该是DefaultListableBeanFactory，
 * 因为这个BeanFactory是BeanFactory一族的最底层的BeanFactory实现类，拥有所有父BeanFactory
 *
 * @author nivelle
 */
@Component
public class PersonBeanLife implements BeanFactoryAware, BeanNameAware,
        InitializingBean, DisposableBean, ApplicationContextAware {

    private String name;
    private String address;
    private int phone;

    private BeanFactory beanFactory;
    private String beanName;

    private static ApplicationContext applicationContext;


    public PersonBeanLife() {
        System.out.println("【构造器】调用Person的构造实例化");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        System.out.println("【注入属性】注入属性name");
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        System.out.println("【注入属性】注入属性address");
        this.address = address;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        System.out.println("【注入属性】注入属性phone");
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "PersonBeanLife [address=" + address + ", name=" + name + ", phone="
                + phone + "]";
    }

    /**
     * 这是BeanFactoryAware接口方法
     */
    @Override
    public void setBeanFactory(BeanFactory arg0) throws BeansException {
        System.out.println("【BeanFactoryAware接口】调用BeanFactoryAware.setBeanFactory()");
        this.beanFactory = arg0;
    }

    /**
     * 这是BeanNameAware接口方法
     *
     * @param arg0
     */
    @Override
    public void setBeanName(String arg0) {
        System.out.println("【BeanNameAware接口】调用BeanNameAware.setBeanName()");
        this.beanName = arg0;
    }

    /**
     * 这是 InitializingBean 接口方法
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("【InitializingBean接口】调用InitializingBean.afterPropertiesSet()");
    }

    /**
     * 这是DiposibleBean接口方法
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        System.out.println("【DiposibleBean接口】调用DiposibleBean.destory()");
    }

    /**
     * 通过<bean>的init-method属性指定的初始化方法
     */
    public void myInit() {
        System.out.println("【init-method】调用<bean>的init-method属性指定的初始化方法");
    }

    /**
     * 通过<bean>的destroy-method属性指定的初始化方法
     */
    public void myDestory() {
        System.out.println("【destroy-method】调用<bean>的destroy-method属性指定的初始化方法");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        PersonBeanLife.applicationContext = applicationContext;
    }
}