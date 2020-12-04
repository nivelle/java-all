package com.nivelle.spring.springmvc;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import java.util.List;


/**
 * The auto-configuration adds the following features on top of Spring’s defaults:
 * <p>
 * Inclusion of ContentNegotiatingViewResolver and BeanNameViewResolver beans.
 * Support for serving static resources, including support for WebJars (covered later in this document)).
 * Automatic registration of Converter, GenericConverter, and Formatter beans.
 * Support for HttpMessageConverters (covered later in this document).
 * Automatic registration of MessageCodesResolver (covered later in this document).
 * Static index.html support.
 * Custom Favicon support (covered later in this document).
 * Automatic use of a ConfigurableWebBindingInitializer bean (covered later in this document).
 */

/**
 * @EnableWebMvc 完全自定义控制
 * 1. SpringMVC;If you want to take complete control of Spring MVC, you can add your own @Configuration annotated with @EnableWebMvc
 * <p>
 * 如果既想保留自动配置的SpringMVC又想使用自己自定义的MVC属性，需要使用实现了WebMvcConfigurer的配置类。该配置类不能加 @EnableWebMvc
 **/
//@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolverList) {
        argumentResolverList.add(new MyHandlerMethodArgumentResolver());
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        returnValueHandlers.add(new MyHandlerMethodReturnValueHandler());
    }

    /**
     * 和 MyCrossFilter 一样解决跨域问题。 第三种方式是直接在Controller上 @CrossOrigin
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").
                allowedMethods("POST", "GET", "PUT", "DELETE").allowCredentials(true).allowedHeaders("*").maxAge(3600);
    }

    /**
     * 方法拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MyHandlerInterceptor()).addPathPatterns("/test/config").excludePathPatterns("/test/return");
    }

    /**
     * 修改已经注册的 HttpMessageConverter
     * A hook for extending or modifying the list of converters after it has been configured.
     * <p>
     * 消息转换器,主要处理 requestBody 和 responseBody 的数据。Configure the {@link HttpMessageConverter HttpMessageConverters} to use for reading or writing to the body of the request or response.
     *
     * @param converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter fjc = new FastJsonHttpMessageConverter();
        FastJsonConfig fj = new FastJsonConfig();
        //忽略循环引用
        fj.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect);
        fjc.setFastJsonConfig(fj);
        //设置的index越靠前越优先尝试。 若设置为尾部则优先生效的是 MappingJackson2HttpMessageConverter
        //converters.add(0,fjc);
        converters.add(fjc);
        converters.add(converter1());
        converters.add(converter2());
        //converters 注入有三种方式：
        //1.  @Bean
        //2.  configureMessageConverters
        //3.  extendMessageConverters

    }

    /**
     * 自定义消息转换器
     *
     * @return
     */
    @Bean
    public MyHttpMessageConverter converter1() {
        return new MyHttpMessageConverter();
    }

    /**
     * 自定义消息转换器
     *
     * @return
     */
    @Bean
    public MyHttpMessageConverter2 converter2() {
        return new MyHttpMessageConverter2();
    }

    /**
     * 对控制进行路由规则配置
     *
     * @param configurer
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 是否存在尾\来进行匹配  /user和/user/等效的，同样可以进行匹配
        configurer.setUseTrailingSlashMatch(true);
        // 这个配置需要传入一个UrlPathHelper对象，UrlPathHelper是一个处理url地址的帮助类，
        // 他里面有一些优化url的方法
        // 比如：getSanitizedPath，就是将// 换成/  所以我们在输入地址栏的时候，//也是没有问题的，
        // 这里使用springmvc默认的就可以了，如果想要深入了解，那么我们后续在深入
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        configurer.setUrlPathHelper(urlPathHelper);

        // 路径匹配器 PathMatcher是一个接口，springmvc默认使用的是AntPathMatcher
        // 这里也就不深入了，使用springmvc默认的就可以，如果想要深入了解，那么我们后续在深入，查看AntPathMatcher的源码
        // configurer.setPathMatcher();

        // 配置路径前缀
        // 下面这样写的意思是：对含有 MyAnnotationImportBeanDefinitionRegistrar 注解的controller添加/test前缀
        //configurer.addPathPrefix("test", c -> c.isAnnotationPresent(MyAnnotationImportBeanDefinitionRegistrar.class));
        configurer.addPathPrefix("test", c -> c.getPackage().getName().contains("com.nivelle.spring.test"));
    }

    @Override
    public  void  addFormatters(FormatterRegistry registry){
        registry.addConverter(new MyParamsConverter());
    }

}



