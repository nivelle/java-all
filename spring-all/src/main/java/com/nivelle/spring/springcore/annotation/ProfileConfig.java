package com.nivelle.spring.springcore.annotation;

import com.nivelle.spring.pojo.Dog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.util.StringValueResolver;

/**
 * 启动配置类
 * <p>
 * 1. 通过@Value 解析配置文件和类对应的属性值
 * <p>
 * 2. 通过实现 EmbeddedValueResolverAware 接口 解析配置文件和类对应的属性值
 *
 * @author nivelle
 * @date 2019/09/25
 */
@Configuration
@PropertySource(value = "classpath:config/application.properties")
public class ProfileConfig implements EmbeddedValueResolverAware, EnvironmentAware {


    private String applicationName;

    private StringValueResolver resolver;

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Value("${test.name}")
    private String testName;


    /**
     * 1. VM 配置设置启动参数来指定:-Dspring.profiles.active=dev
     * 2. 启动类 - configurableEnvironment.setActiveProfiles("dev"); -> refresh()
     */
    @Bean
    @Profile(value = "dev")
    public Dog devDog() {
        return new Dog(applicationName, 1, "red");
    }

    @Bean
    @Profile(value = "prod")
    public Dog prodDog() {
        return new Dog("prodDog", 1, "red");
    }

    @Bean
    @Profile(value = "default")
    public Dog defaultDog() {
        return new Dog(testName, 1, "red");
    }


    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
        this.applicationName = this.resolver.resolveStringValue("${application.name}");
    }

    public String getApplicationName() {
        int length = environment.getActiveProfiles().length;
        for (int i = 0; i < length; i++) {
            System.out.println(environment.getActiveProfiles()[i]);
        }
        return this.applicationName;
    }


}
