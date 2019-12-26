package com.nivelle.spring.springlife.lifecycle;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author fuxinzhong
 * @date 2019/09/27
 */
public class BeanLifeApplocation {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.register(Config.class);
        annotationConfigApplicationContext.refresh();
        Animal animal = (Animal) annotationConfigApplicationContext.getBean("animal");
        System.out.println(animal);
    }
}
