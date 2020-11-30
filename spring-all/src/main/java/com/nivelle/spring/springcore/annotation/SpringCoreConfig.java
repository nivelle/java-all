package com.nivelle.spring.springcore.annotation;

import com.nivelle.spring.pojo.Car;
import com.nivelle.spring.pojo.Dog;
import com.nivelle.spring.springcore.annotation.bean.BeanImported;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;

/**
 * 注解类学习
 *
 * @author nivelle
 * @date 2019/09/23
 */

/**
 * 采用自定义的过滤方式，必须使用:useDefaultFilters=false
 */
@Configuration
//返回true时将此类作为配置类，系统条件注解
@Conditional(MyCondition.class)
/**
 * @Import 导入另外一个配置类的方式注册bean, 也可以将一些普通java类注册成一个bean
 *
 * 1. 导入某个配置类（导入某个加了@Configuration的配置类或者某个类）
 *
 * 2. 导入某个 ImportSelector 接口的实现类
 *
 * 3. 导入某个 ImportBeanDefinitionRegistrar 接口的实现类
 */
@Import({Car.class, ImportSelector.class, MySelfImportBeanDefinitionRegistrar.class})
//此注解为自定义注解,目的是注入一个bean
@MyAnnotationImportBeanDefinitionRegistrar(targets = {BeanImported.class})
@ImportResource(value = {"classpath*:beanLife.xml"})
public class SpringCoreConfig {

    /**
     * 默认实例名为方法名字
     *
     * @return
     */
    @Bean
    @Lazy
    public Dog bigDog() {
        return new Dog("wangwang", 1, "black");
    }

    /**
     * 默认单例,饿汉模式，通过@Lazy @Scope指定为多实例和懒汉模式
     *
     * @return
     */
    @Bean(value = "buDingDog")
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Dog littleDog() {
        return new Dog("buding", 1, "yellow");
    }
}
