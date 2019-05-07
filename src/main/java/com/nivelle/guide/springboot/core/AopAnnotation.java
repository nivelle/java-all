package com.nivelle.guide.springboot.core;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AopAnnotation {

    String value() default "";
}
