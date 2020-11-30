package com.nivelle.spring;

import com.nivelle.spring.springcore.lifecycle.PersonBeanLife;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 运行看懂spring启动过程
 */
public class TestXmlApplicationBeanLifeCycle {

    public static void main(String[] args) {

        System.out.println("现在开始初始化容器");

        ApplicationContext context = new ClassPathXmlApplicationContext("beanLife.xml");
        System.out.println("容器初始化成功");
        //得到Preson，并使用
        PersonBeanLife person = context.getBean("person", PersonBeanLife.class);
        System.out.println(person);

        System.out.println("现在开始关闭容器！");
        ((ClassPathXmlApplicationContext) context).registerShutdownHook();
    }
}
