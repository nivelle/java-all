package com.nivelle.container.servlet;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * 继承Spring为我们提供的AbstractAnnotationConfigDispatcherServletInitializer,不需要添加@Configuration等注解，
 * 因为Servlet容器会自动将我们自定义的MyCustomWebApplicationInitializer class传入SpringServletContainerInitializer#onStartup，
 * 而SpringServletContainerInitializer会为我们实例化这个类并调用它。
 *
 * @author fuxinzhong
 * @date 2020/08/25
 */
public class MyCustomWebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    /**
     * 为{@linkPlan#createRootApplicationContext()根应用程序上下文}
     * 指定{@code@Configuration}和/或{@code@Component}类
     *
     * @ 返回根应用上下文的配置，如果不需要创建和注册根上下文，则返回{@code null
     */
    @Nullable
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }


    /**
     * 为{@linkPlan#createServletApplicationContext()Servlet应用程序上下文}指定
     * {@code@Configuration}和/或{@code@Component}类
     *
     * @返回Servlet应用程序上下文的配置，或者如果所有配置都通过根配置类指定，则为{@code null}
     */
    @Nullable
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebConfig.class};
    }

    /**
     * 指定{@code DispatcherServlet}的Servlet映射
     * 例如{@code“/”}、{@code“/app”}等
     */
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    /**
     * 返回注册{@link DispatcherServlet}的名称
     * 默认为{@link#Default_Servlet_Name} (public static final String DEFAULT_SERVLET_NAME = "dispatcher";)
     */
    @Override
    protected String getServletName() {
        return "myServlet";
    }
}