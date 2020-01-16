package com.nivelle.spring.springcore.annotation;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * 开启 @ConfigurationProperties 自动注解功能
 *
 * @author fuxinzhong
 * @date 2020/01/16
 */
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(value = SelfProperties.class)
@Import({ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class,})
public class EnableSelfProperties {

}
