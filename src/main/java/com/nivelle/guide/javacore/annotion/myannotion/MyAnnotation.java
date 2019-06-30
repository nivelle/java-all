package com.nivelle.guide.javacore.annotion.myannotion;


import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotation {

    String name() default "";

    String sex() default "男";

    String nation() default "ChineseUserImpl";
}

