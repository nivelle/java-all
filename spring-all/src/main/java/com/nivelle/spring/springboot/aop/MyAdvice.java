package com.nivelle.spring.springboot.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 自定义AOP
 *
 * @author nivelle
 */
@Aspect
@Component
public class MyAdvice {

    @Pointcut(value = "@annotation(com.nivelle.spring.springboot.aop.AopAnnotation)")
    public void cutService() {
    }

    @Around("cutService()")
    public Object writeLog(ProceedingJoinPoint point) throws Throwable {

        System.out.println("AOP方法开始执行！！！");
        Object result = "";
        Signature sig = point.getSignature();
        MethodSignature methodSignature = null;
        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        methodSignature = (MethodSignature) sig;
        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        AopAnnotation annotation = currentMethod.getAnnotation(AopAnnotation.class);
        if (Objects.nonNull(annotation)) {
            System.out.println("对原来逻辑完成了织入");
            result = point.proceed();
        }
        System.out.println("Aop方法执行完了！！！");
        return result;
    }

}
