package com.nivelle.spring.springboot.core;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Component
public class MyAdvice {

    @Pointcut(value = "@annotation(com.nivelle.spring.springboot.core.AopAnnotation)")
    public void cutService() {
    }

    @Around("cutService()")
    public Object writeLog(ProceedingJoinPoint point) throws Throwable {

        System.out.println("AOP方法开始执行！！！");
        Object result = "";
        Signature sig = point.getSignature();
        MethodSignature msig = null;
        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        msig = (MethodSignature) sig;
        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
        AopAnnotation annotation = currentMethod.getAnnotation(AopAnnotation.class);
        if (Objects.nonNull(annotation)) {
            System.out.println("对原来逻辑完成了织入");
            result = point.proceed();
        }
        System.out.println("Aop方法执行完了！！！");
        return result;
    }

}
