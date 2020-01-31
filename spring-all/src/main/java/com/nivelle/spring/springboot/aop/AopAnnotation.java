package com.nivelle.spring.springboot.aop;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AopAnnotation {

    String value() default "";
}