package com.nivelle.spring.springcore.annotation;

import com.nivelle.spring.springcore.annotation.bean.ConditionBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 条件注解
 *
 * @author nivelle
 * @date 2020/01/16
 */
@Configuration
public class ConditionConfig {

    /**
     * 结合使用注解@ConditionalOnClass和@Bean,可以仅当某些类存在于 classpath 上时候才创建某个Bean
     * <p>
     * name : 不确定指定类在classpath 上
     * <p>
     * value: 确定指定类在 classpath 上
     */
    @Bean(name = "myCondition")
    @ConditionalOnClass(name = "com.nivelle.spring.springcore.lifecycle.Animal")
    //checks if the specified properties have a specific value,只有指定属性的值是指定值才满足条件，注入当前bean
    //matchIfMissing= true 则如果指定key的配置不存在，也认为匹配，注入如下bean
    @ConditionalOnProperty(prefix = "test", name = "name", havingValue="condition1",matchIfMissing = true)
    public ConditionBean getConditionBean() {
        return new ConditionBean("我是条件注解加载进来的");
    }

}
