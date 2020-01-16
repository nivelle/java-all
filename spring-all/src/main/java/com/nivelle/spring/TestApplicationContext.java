package com.nivelle.spring;

import com.nivelle.spring.pojo.Dog;
import com.nivelle.spring.springcore.annotation.ProfileConfig;
import com.nivelle.spring.springcore.annotation.SelfProperties;
import com.nivelle.spring.springcore.annotation.SpringCoreConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 自定义测试
 *
 * @author fuxinzhong
 * @date 2019/09/25
 */
public class TestApplicationContext {

    /**
     * spring源码学习
     *
     * @param args
     */
    public static void main(String args[]) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        ConfigurableEnvironment configurableEnvironment = annotationConfigApplicationContext.getEnvironment();
        configurableEnvironment.setActiveProfiles("dev");
        System.err.println("====================");
        annotationConfigApplicationContext.register(ProfileConfig.class, SpringCoreConfig.class, SelfProperties.class);

        //必须要刷新一下
        annotationConfigApplicationContext.refresh();
        String[] beans = annotationConfigApplicationContext.getBeanDefinitionNames();
        for (int i = 0; i < beans.length; i++) {
            System.err.println("当前扫描到的bean定义2:" + beans[i]);
        }
        System.err.println("====================");
        Dog dog = (Dog) annotationConfigApplicationContext.getBean("devDog");
        System.err.println(dog.getName());

        ProfileConfig profileConfig = (ProfileConfig) annotationConfigApplicationContext.getBean("profileConfig");
        System.err.println(profileConfig.getApplicationName());

        SelfProperties selfProperties = (SelfProperties) annotationConfigApplicationContext.getBean("selfProperties");
        selfProperties.printDesc();

        System.out.println("启动成了");
    }


}
