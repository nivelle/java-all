package com.nivelle.spring.springcore.annotation;

import com.nivelle.spring.springcore.annotation.bean.ConditionBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 条件注解
 *
 * @author fuxinzhong
 * @date 2020/01/16
 */
@Configuration
public class ConditionConfig {

    /**
     * 结合使用注解@ConditionalOnClass和@Bean,可以仅当某些类存在于 classpath 上时候才创建某个Bean
     * <p>
     * name : 不确定指定类在classpath 上
     * <p>
     * value : 确定指定类在 classpath 上
     */
    @Bean(name = "myCondition")
    @ConditionalOnClass(name = "com.nivelle.spring.springcore.basics.Animal")
    @ConditionalOnProperty(prefix = "test", name = "name", havingValue="condition",matchIfMissing = false)
    public ConditionBean getConditionBean() {
        return new ConditionBean("我是条件注解加载进来的");
    }

}
