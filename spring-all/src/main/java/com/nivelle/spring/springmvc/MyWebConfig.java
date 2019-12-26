package com.nivelle.spring.springmvc;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.nivelle.spring.springboot.filter.CorsFilter;
import com.nivelle.spring.springboot.filter.MyFilter1;
import com.nivelle.spring.springboot.filter.MyFilter2;
import com.nivelle.spring.springboot.interceptor.MyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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
@Configuration
/**
 *
 * @EnableWebMvc 完全自定义控制SpringMVC;If you want to take complete control of Spring MVC, you can add your own @Configuration annotated with @EnableWebMvc
 *
 * 如果既想保留自动配置的SpringMVC又想使用自己自定义的MVC属性，需要使用实现了WebMvcConfigurer的配置类。该配置类不能加 @EnableWebMvc
 *
 * **/
public class MyWebConfig implements WebMvcConfigurer {


    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;


    @PostConstruct
    public void init() {
        // 获取当前 RequestMappingHandlerAdapter 所有的 ArgumentResolver对象
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
        List<HandlerMethodArgumentResolver> newArgumentResolvers = new ArrayList<>(argumentResolvers.size() + 1);
        // 添加 PropertiesHandlerMethodArgumentResolver 到集合第一个位置
        newArgumentResolvers.add(0, new PropertiesHandlerMethodArgumentResolver());

        // 将原 ArgumentResolver 添加到集合中
        newArgumentResolvers.addAll(argumentResolvers);
        // 重新设置 ArgumentResolver对象集合
        requestMappingHandlerAdapter.setArgumentResolvers(newArgumentResolvers);

        // 获取当前 RequestMappingHandlerAdapter 所有的 returnValueHandlers对象
        List<HandlerMethodReturnValueHandler> returnValueHandlers = requestMappingHandlerAdapter.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> newReturnValueHandlers = new ArrayList<>(returnValueHandlers.size() + 1);
        // 添加 PropertiesHandlerMethodReturnValueHandler 到集合第一个位置
        newReturnValueHandlers.add(0, new PropertiesHandlerMethodReturnValueHandler());
        // 将原 returnValueHandlers 添加到集合中
        newReturnValueHandlers.addAll(returnValueHandlers);
        // 重新设置 ReturnValueHandlers对象集合
        requestMappingHandlerAdapter.setReturnValueHandlers(newReturnValueHandlers);
    }

    /**
     * 请求过滤器
     *
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    @DependsOn("myFilter2")//初始化顺序控制
    public FilterRegistrationBean filterRegist() {
        FilterRegistrationBean frBean = new FilterRegistrationBean();
        frBean.setFilter(new MyFilter1());
        frBean.addUrlPatterns("/*");
        //数字越小，优先级越高，指的是执行顺序，加载顺序按照先后，若需要修改，可使用注解 @DependsOn
        frBean.setOrder(2);
        System.out.println("过滤器1注册完成");
        return frBean;
    }

    /**
     * 请求过滤器2先与过滤器1执行
     *
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean(name = "myFilter2")
    public FilterRegistrationBean filterRegist2() {
        FilterRegistrationBean frBean = new FilterRegistrationBean();
        frBean.setFilter(new MyFilter2());
        frBean.addUrlPatterns("/*");
        frBean.setOrder(1);
        System.out.println("过滤器2注册完成");
        return frBean;
    }

    /**
     * 请求过滤器2先与过滤器1执行
     *
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean(name = "corsFilter")
    public FilterRegistrationBean filterCors() {
        FilterRegistrationBean frBean = new FilterRegistrationBean();
        frBean.setFilter(new CorsFilter());
        frBean.addUrlPatterns("/*");
        frBean.setOrder(1);
        System.out.println("跨域过滤器");
        return frBean;
    }

    /**
     * 方法拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MyInterceptor()).addPathPatterns("/config/**");
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
        converters.add(0, fjc);
//        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.xml();
//        builder.indentOutput(true);
//        converters.add(1, new MappingJackson2XmlHttpMessageConverter(builder.build()));
//        converters.add(2, converter());
    }

    /**
     * 自定义消息转换器
     *
     * @return
     */
    @Bean
    public PropertiesHttpMessageConverter converter() {
        return new PropertiesHttpMessageConverter();
    }

}



