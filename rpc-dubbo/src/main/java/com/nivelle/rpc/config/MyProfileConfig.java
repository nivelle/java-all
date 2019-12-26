package com.nivelle.rpc.config;

import com.nivelle.rpc.model.Dog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringValueResolver;

/**
 * 数据源
 *
 * @author fuxinzhong
 * @date 2019/09/25
 */
@Configuration
@PropertySource(value = "classpath:config/application.properties")
public class MyProfileConfig implements EmbeddedValueResolverAware {


    private String applicationName;


    private StringValueResolver resolver;

    @Value("${test.name}")
    private String testName;


    /**
     * 1. VM 配置设置启动参数来指定:-Dspring.profiles.active=dev
     * 2. 启动类 - configurableEnvironment.setActiveProfiles("dev"); - refresh()
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
        this.applicationName = this.resolver.resolveStringValue("${dubbo.application.name}");

    }

    public String getApplicationName() {
        return this.applicationName;
    }

}
