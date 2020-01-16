package com.nivelle.spring.springcore.beanlifecycle;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author fuxinzhong
 * @date 2019/09/27
 */
public class TestBeanLifeApplocation {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.register(Config.class);
        annotationConfigApplicationContext.refresh();
        Animal animal = (Animal) annotationConfigApplicationContext.getBean("animal");
        System.out.println(animal);
    }
}
