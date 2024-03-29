package com.nivelle.spring.springcore.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 自定义AOP
 * <p>
 * 自动配置 AopAutoConfiguration 类的主要任务是根据配置参数使用注解 @EnableAspectJAutoProxy。
 * <p>
 * 该自动配置类通过注解声明了自己生效的条件是 :
 * <p>
 * 1. 以下类必须存在于classpath :
 * <p>
 * 1. EnableAspectJAutoProxy
 * 2. Aspect
 * 3. Advice
 * 4. AnnotatedElement
 * 2. 配置参数:spring.aop.auto值不为false. 默认为true
 */
@Aspect
@Component
public class MyAdvice {

    @Pointcut(value = "@annotation(com.nivelle.spring.springcore.aop.AopAnnotation)")
    public void cutService() {
    }

    @Before(value = "cutService()")
    public void logBefore() {
        System.out.println("logBefore............");
    }

    @After(value = "cutService()")
    public void logAfter() {
        System.out.println("logAfter..............");
    }

    @AfterReturning(value = "cutService()", returning = "result")
    public void logReturn(Object result) {
        System.out.println("logReturn............." + result);
    }
    @AfterThrowing(value = "cutService()",throwing = "exception")
    public void logException(JoinPoint joinPoint,Exception exception){
        System.out.println(joinPoint.getSignature().getName()+"logException........"+exception);
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
            System.out.println("环绕方法-》对原来逻辑完成了织入");
            result = point.proceed();
        }
        System.out.println("Aop方法执行完了！！！");
        return result;
    }

}
