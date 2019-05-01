package com.nivelle.guide.springboot.configbean;

import com.nivelle.guide.springboot.filter.MyFilter1;
import com.nivelle.guide.springboot.filter.MyFilter2;
import com.nivelle.guide.springboot.interceptor.MyInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    /**
     * 请求过滤器
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    @DependsOn("myFilter2")//初始化顺序控制
    public FilterRegistrationBean filterRegist() {
        FilterRegistrationBean frBean = new FilterRegistrationBean();
        frBean.setFilter(new MyFilter1());
        frBean.addUrlPatterns("/*");
        frBean.setOrder(2);//数字越小，优先级越高，指的是执行顺序，加载顺序按照先后，若需要修改，可使用注解 @DependsOn
        System.out.println("过滤器1注册完成");
        return frBean;
    }
    /**
     * 请求过滤器2先与过滤器1执行
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
     * 方法拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new MyInterceptor()).addPathPatterns("/config/**");
    }


}
