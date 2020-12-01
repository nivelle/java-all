package com.nivelle.spring;

import com.nivelle.spring.pojo.Dog;
import com.nivelle.spring.springcore.annotation.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 自定义测试
 *
 * @author nivelle
 * @date 2019/09/25
 */
public class TestAnnotationConfigApplicationContext {

    /**
     * spring源码学习
     *
     * @param args
     */
    public static void main(String [] args) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        ConfigurableEnvironment configurableEnvironment = annotationConfigApplicationContext.getEnvironment();
        configurableEnvironment.setActiveProfiles("dev");
        System.out.println("====================加载注册文件========================");

        annotationConfigApplicationContext.register(ProfileConfig.class,
                SpringCoreConfig.class, ConditionConfig.class, SelfProperties.class, EnableSelfProperties.class);

        System.out.println("==================== scan 扫描加载需要加载的文件========================");

        /**
         * 扫描 @Service @Repository @Controller @Component 注解标注的类
         */
        annotationConfigApplicationContext.scan("com.nivelle.spring.springcore.*.*");
        //必须要刷新一下
        annotationConfigApplicationContext.refresh();
        System.out.println("扫描加载类完成==================================");

        Dog dog = (Dog) annotationConfigApplicationContext.getBean("devDog");
        System.out.println("从容器获取实例dog:" + dog.getName());
        ProfileConfig profileConfig = (ProfileConfig) annotationConfigApplicationContext.getBean("profileConfig");
        System.out.println("从容器获取实例 profileConfig:" + profileConfig.getApplicationName());
        SelfProperties selfProperties = (SelfProperties) annotationConfigApplicationContext.getBean("selfProperties");
        selfProperties.printDesc();
        //获取容器中的bean定义
        String[] beans = annotationConfigApplicationContext.getBeanDefinitionNames();
        for (int i = 0; i < beans.length; i++) {
            System.out.println("当前扫描到的bean定义:" + beans[i]);
        }
        System.out.println("====================");
        System.out.println("启动成了");
    }


}
