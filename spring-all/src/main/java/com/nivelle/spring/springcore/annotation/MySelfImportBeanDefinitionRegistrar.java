package com.nivelle.spring.springcore.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * ImportBeanDefinitionRegistrar 向容器注册 beanDefinition
 *
 * @author nivelle
 * @date 2019/08/25
 */
public class MySelfImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {


    /**
     * 在registerBeanDefinitions方法之前执行
     *
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("My JAVA_HOME:" + environment.getProperty("JAVA_HOME"));
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(MyAnnotationImportBeanDefinitionRegistrar.class.getCanonicalName());
        Class<?>[] targets = (Class<?>[]) annotationAttributes.get("targets");
        System.err.println("额外的 beanDefinition targets is:" + targets[0].getName());

        if (null != targets && targets.length > 0) {
            for (Class<?> target : targets) {
                BeanDefinition beanDefinition = BeanDefinitionBuilder
                        //将某个类的定义注册到spring容器中
                        .genericBeanDefinition(target)
                        .getBeanDefinition();
                registry.registerBeanDefinition(beanDefinition.getBeanClassName(),
                        beanDefinition);
            }
        }
    }
}
