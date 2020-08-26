package com.nivelle.container.servlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * war包方式启动
 *
 * @author nivelle
 * @date 2020/01/06
 */

/**
 *  WebApplicationInitializer 让我们可以使用传统的WAR包的方式部署运行 SpringApplication，可以将 servlet、filter 和 ServletContextInitializer 从应用程序上下文绑定到服务器。
 *
 *  如果要配置应用程序，要么覆盖 configure(SpringApplicationBuilder) 方法(调用 SpringApplicationBuilder#Sources(Class.))，要么使初始化式本身成为 @configuration。
 *
 *  如果将SpringBootServletInitializer与其他 WebApplicationInitializer 结合使用，则可能还需要添加@Ordered注解来配置特定的启动顺序。
 */

public class MySpringBootServletInitializer extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(MySpringBootServletInitializer.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MySpringBootServletInitializer.class);
    }

    /**
     * 它是一个 WebApplicationInitializer，它的启动靠的是 SpringServletContainerInitializer，而 SpringServletContainerInitializer靠的是Servlet容器。所以它的启动靠的就是外部容器
     */
}
