package com.nivelle.rpc;

import com.nivelle.rpc.config.MyProfileConfig;
import com.nivelle.rpc.model.Dog;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 自定义测试
 *
 * @author fuxinzhong
 * @date 2019/09/25
 */
public class JunitApplicationContext {

    /**
     * spring源码学习
     *
     * @param args
     */
    public static void mainTest(String args[]) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        ConfigurableEnvironment configurableEnvironment = annotationConfigApplicationContext.getEnvironment();
        configurableEnvironment.setActiveProfiles("dev");
        System.err.println("====================");
        annotationConfigApplicationContext.register(MyProfileConfig.class);
        //todo 必须要刷新一下
        annotationConfigApplicationContext.refresh();
        String[] beans = annotationConfigApplicationContext.getBeanDefinitionNames();
        for (int i = 0; i < beans.length; i++) {
            System.err.println("当前扫描到的bean定义2:" + beans[i]);
        }
        System.err.println("====================");
        Dog dog = (Dog) annotationConfigApplicationContext.getBean("devDog");
        System.err.println(dog.getName());

        MyProfileConfig myProfileConfig = (MyProfileConfig) annotationConfigApplicationContext.getBean("myProfileConfig");
        System.err.println(myProfileConfig.getApplicationName());
        System.out.println("启动成了");
    }


}
