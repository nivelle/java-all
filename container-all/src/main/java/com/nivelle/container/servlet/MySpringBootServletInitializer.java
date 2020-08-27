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
    //使用内嵌容器时，main方法入口在这里，启动初始化的某个时间段我也启动了我的内嵌容器使用外部容器时，忽略我的存在
    public static void main(String[] args) {
        SpringApplication.run(MySpringBootServletInitializer.class, args);
    }

    //使用内嵌容器时，我不会被调用
    //外部容器时，外部容器检测到 SpringServletContainerInitializer，然后又检测到继承自WebApplicationInitializer的我，然后我被调用了，初始化也开始了
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MySpringBootServletInitializer.class);
    }

    /**
     * 它是一个 WebApplicationInitializer，它的启动靠的是 SpringServletContainerInitializer，而 SpringServletContainerInitializer靠的是Servlet容器。所以它的启动靠的就是外部容器
     *
     * 内嵌的tomcat不会以spi方式加载ServletContainerInitializer，而是用TomcatStarter的onStartup，间接启动ServletContextInitializers，来达到ServletContainerInitializer的效果
     */
}
