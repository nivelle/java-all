package com.nivelle.spring.springcore.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.context.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * ApplicationContextAwareProcessor 是一个Spring内部工具，它实现了接口BeanPostProcessor,
 * 用于向实现了如下某种Aware接口的bean设置ApplicationContext中相应的属性:
 * <p>
 * 1. EnvironmentAware
 * 2. EmbeddedValueResolverAware
 * 3. ResourceLoaderAware
 * 4. ApplicationEventPublisherAware
 * 5. MessageSourceAware
 * 6. ApplicationContextAware
 *
 * @author nivelle
 * @date 2020/01/19
 */
@Component
public class SpringAllAware implements EnvironmentAware,
        EmbeddedValueResolverAware,
        ResourceLoaderAware,
        ApplicationEventPublisherAware,
        MessageSourceAware,
        ApplicationContextAware, ServletContextAware {


    private ApplicationContext applicationContext;

    private ApplicationEventPublisher applicationEventPublisher;

    private StringValueResolver stringValueResolver;


    private Environment environment;

    private MessageSource messageSource;

    private ResourceLoader resourceLoader;

    private ServletContext servletContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;

    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }


}
