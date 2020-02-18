# SpringBoot 之SpringMVC

## 1. ServletWebServerFactoryAutoConfiguration

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

- public ServletWebServerFactoryCustomizer servletWebServerFactoryCustomizer(ServerProperties serverProperties);//自定义Server工厂定制设置server参数

- @ConditionalOnClass(name = "org.apache.catalina.startup.Tomcat");//仅在类 org.apache.catalina.startup.Tomcat 存在于 classpath 上时才生效

- public TomcatServletWebServerFactoryCustomizer tomcatServletWebServerFactoryCustomizer(ServerProperties serverProperties);

### 静态内部类

public static class BeanPostProcessorsRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware


- public void setBeanFactory(BeanFactory beanFactory) throws BeansException

- public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,BeanDefinitionRegistry registry）

  - registerSyntheticBeanIfMissing(registry, "webServerFactoryCustomizerBeanPostProcessor",WebServerFactoryCustomizerBeanPostProcessor.class);
  
  - registerSyntheticBeanIfMissing(registry, "errorPageRegistrarBeanPostProcessor",ErrorPageRegistrarBeanPostProcessor.class);

### ServletWebServerFactoryConfiguration

- ServletWebServerFactoryConfiguration

 - 	@ConditionalOnClass({ Servlet.class, Tomcat.class, UpgradeProtocol.class })

 - 	@ConditionalOnMissingBean(value = ServletWebServerFactory.class, search = SearchStrategy.CURRENT)

 - 	public static class EmbeddedTomcat 
 
    - public TomcatServletWebServerFactory tomcatServletWebServerFactory();


## 2. DispatcherServletAutoConfiguration

#### 主要提供两个bean

- DispatcherServlet

- DispatcherServletRegistrationBean

  ```
   该bean的主要功能是将 DispatcherServlet 注册到 Servlet容器:  
 
    1. spring.http 的配置参数被加载到 bean HttpProperties
 
    2. spring.mvc 的配置参数被加载到 bean WebMvcProperties
 
  ```

### public class DispatcherServletAutoConfiguration {}

- @Configuration

- @ConditionalOnWebApplication(type = Type.SERVLET);//仅在当前应用是一个 Servlet Web 应用时才生效

- @ConditionalOnClass(DispatcherServlet.class);//仅在类 DispatcherServlet 存在于 classpath 上时才生效

- @AutoConfigureAfter(ServletWebServerFactoryAutoConfiguration.class);//在自动配置类 ServletWebServerFactoryAutoConfiguration 应用之后再应用

### protected static class DispatcherServletConfiguration{}

- @Configuration //定义 bean DispatcherServlet dispatcherServlet;//如果类型为 MultipartResolver 的 bean 存在，为其创建一个别名 multipartResolver

- @Conditional(DefaultDispatcherServletCondition.class);//DefaultDispatcherServletCondition 在类型为 DispatcherServlet 或者名称为 dispatcherServlet 的 bean **不存在**时才被满足

- @ConditionalOnClass(ServletRegistration.class);//仅在类 ServletRegistration 存在于 classpath 上时才生效

- @EnableConfigurationProperties({ HttpProperties.class, WebMvcProperties.class });//确保前缀为 spring.http 的配置参数被加载到 bean HttpProperties;确保前缀为 spring.mvc 的配置参数被加载到 bean WebMvcProperties

#### public DispatcherServlet dispatcherServlet(HttpProperties httpProperties, WebMvcProperties webMvcProperties) TODO 

- DispatcherServlet dispatcherServlet = new DispatcherServlet();

- dispatcherServlet.setDispatchOptionsRequest(webMvcProperties.isDispatchOptionsRequest());

- dispatcherServlet.setDispatchTraceRequest(webMvcProperties.isDispatchTraceRequest());

- dispatcherServlet.setThrowExceptionIfNoHandlerFound(webMvcProperties.isThrowExceptionIfNoHandlerFound());

- dispatcherServlet.setPublishEvents(webMvcProperties.isPublishRequestHandledEvents());

- dispatcherServlet.setEnableLoggingRequestDetails(httpProperties.isLogRequestDetails());


#### public MultipartResolver multipartResolver(MultipartResolver resolver)

- @ConditionalOnBean(MultipartResolver.class);

- @ConditionalOnMissingBean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)


### protected static class DispatcherServletRegistrationConfiguration

- @Configuration(proxyBeanMethods = false)

- @Conditional(DispatcherServletRegistrationCondition.class)

- @ConditionalOnClass(ServletRegistration.class)

- @EnableConfigurationProperties(WebMvcProperties.class)

- @Import(DispatcherServletConfiguration.class)

#### public DispatcherServletRegistrationBean dispatcherServletRegistration(DispatcherServlet dispatcherServlet,WebMvcProperties webMvcProperties, ObjectProvider<MultipartConfigElement> multipartConfig)

```
它不是个普通意义上的Bean,它实现了Spring SCI 接口,而对于此类实现了Spring SCI(ServletContextInitializer)接口的bean定义,在内置的Tomcat servlet容器启动阶段,严格地讲，是其中相当于web app的StarndartContext的启动阶段，会被逐个实例化并调用其onStartup()方法
```
- @ConditionalOnBean(value = DispatcherServlet.class, name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)

- DispatcherServletRegistrationBean registration = new DispatcherServletRegistrationBean(dispatcherServlet,webMvcProperties.getServlet().getPath());

- registration.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());

- multipartConfig.ifAvailable(registration::setMultipartConfig);