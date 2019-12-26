package com.nivelle.spring.springlife.annotation;

import com.nivelle.spring.springlife.hock.MyBeanAutoConfiguredRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 自定义注解@Import的用法
 *
 * @author fuxinzhong
 * @date 2019/08/25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(value = {MyBeanAutoConfiguredRegistrar.class})
/**
 * 当处理Java编程式配置类(使用了@Configuration的类)的时候，ImportBeanDefinitionRegistrar接口的实现类可以注册额外的bean definitions
 */
public @interface ImportBeanAnnotation {

    Class<?>[] targets() default {};

}
