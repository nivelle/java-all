# SpringBoot 之SpringMVC

## ServletWebServerFactoryAutoConfiguration

- @ConditionalOnClass(ServletRequest.class)//类 ServletRequest 存在于 classpath 上时才生效,也就是要求javax.servlet-api包必须被引用;

- @ConditionalOnWebApplication(type = Type.SERVLET);//当前应用必须是Spring MVC应用才生效;

- @EnableConfigurationProperties(ServerProperties.class);//读取server开头的配置属性

- @Import({ ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class,
  		ServletWebServerFactoryConfiguration.EmbeddedTomcat.class,
  		ServletWebServerFactoryConfiguration.EmbeddedJetty.class,
  		ServletWebServerFactoryConfiguration.EmbeddedUndertow.class });
```		
   
   1. 导入 ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar 以注册BeanPostProcessor : WebServerFactoryCustomizerBeanPostProcessor 和 ErrorPageRegistrarBeanPostProcessor
   
   2. 导入 EmbeddedTomcat/EmbeddedJetty/EmbeddedUndertow 这三个属于ServletWebServerFactoryConfiguration 的嵌套配置类，这三个配置类会分别检测classpath上存在的类，从而判断当前应用使用的是 Tomcat/Jetty/Undertow,从而决定定义哪一个 Servlet Web服务器的工厂 bean :TomcatServletWebServerFactory/JettyServletWebServerFactory/UndertowServletWebServerFactory
                                                                       
```

## SpringBoot注册DispatcherServlet

### DispatcherServletAutoConfiguration

#### 主要提供两个bean

- DispatcherServlet

- DispatcherServletRegistrationBean

 ```
 该bean的主要功能是将 DispatcherServlet 注册到 Servlet容器
 
 ```

#### 使用到的配置参数

- spring.http 的配置参数被加载到 bean HttpProperties

- spring.mvc 的配置参数被加载到 bean WebMvcProperties




