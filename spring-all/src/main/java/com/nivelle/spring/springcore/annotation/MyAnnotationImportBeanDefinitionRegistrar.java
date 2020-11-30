package com.nivelle.spring.springcore.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 自定义注解 @Import 的用法
 *
 * @author nivelle
 * @date 2019/08/25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(value = {MySelfImportBeanDefinitionRegistrar.class})
/**
 * 当处理Java编程式配置类(使用了@Configuration的类)的时候，
 * ImportBeanDefinitionRegistrar 接口的实现类可以注册额外的bean definitions
 */
public @interface MyAnnotationImportBeanDefinitionRegistrar {

    Class<?>[] targets() default {};

}
