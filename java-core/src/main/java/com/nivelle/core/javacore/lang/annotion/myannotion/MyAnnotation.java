package com.nivelle.core.javacore.lang.annotion.myannotion;


import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotation {

    String name() default "";

    String sex() default "default sex 男";

    String nation() default "default nation ChineseUserImpl";
}

