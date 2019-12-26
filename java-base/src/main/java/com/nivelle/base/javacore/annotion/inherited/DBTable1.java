package com.nivelle.base.javacore.annotion.inherited;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited //该注解表示这个注解如果用到父类上，子类也是可以获得该注解的，否则该注解不能被子类感知
public @interface DBTable1 {
    String name() default "table1";
}
