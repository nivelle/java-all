package com.nivelle.spring;

import com.nivelle.spring.pojo.Dog;
import com.nivelle.spring.springcore.annotation.ConditionConfig;
import com.nivelle.spring.springcore.annotation.ProfileConfig;
import com.nivelle.spring.springcore.annotation.SelfProperties;
import com.nivelle.spring.springcore.annotation.SpringCoreConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Repository;

/**
 * 自定义测试
 *
 * @author fuxinzhong
 * @date 2019/09/25
 */
@Repository
public class TestAnnotationConfigApplicationContext {

    /**
     * spring源码学习
     *
     * @param args
     */
    public static void main(String args[]) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        ConfigurableEnvironment configurableEnvironment = annotationConfigApplicationContext.getEnvironment();
        configurableEnvironment.setActiveProfiles("dev");
        System.err.println("====================加载注册文件========================");
        annotationConfigApplicationContext.register(ProfileConfig.class,
                SpringCoreConfig.class,
                SelfProperties.class, ConditionConfig.class);

        System.err.println("====================scan 扫描加载需要加载的文件========================");

        /**
         * 扫描 @Service @Repository @Controller @Component 注解标注的类
         */
        annotationConfigApplicationContext.scan("com.nivelle.spring.springcore.basics");

        annotationConfigApplicationContext.refresh();


        //必须要刷新一下
        String[] beans = annotationConfigApplicationContext.getBeanDefinitionNames();

        Dog dog = (Dog) annotationConfigApplicationContext.getBean("devDog");
        System.err.println(dog.getName());

        ProfileConfig profileConfig = (ProfileConfig) annotationConfigApplicationContext.getBean("profileConfig");
        System.err.println(profileConfig.getApplicationName());

        SelfProperties selfProperties = (SelfProperties) annotationConfigApplicationContext.getBean("selfProperties");
        selfProperties.printDesc();


        for (int i = 0; i < beans.length; i++) {
            System.err.println("当前扫描到的bean定义2:" + beans[i]);
        }
        System.err.println("====================");

        System.out.println("启动成了");
    }


}
