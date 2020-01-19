BeanPostProcessor类 |  介绍
--- | ---
o.sf.context.support.ApplicationContextAwareProcessor |	功能:bean创建时调用bean所实现的各种Aware接口方法设置相应的属性
o.sf.boot.web.servlet.context.WebApplicationContextServletContextAwareProcessor |	功能:Springboot Servlet Web应用中bean创建时调用bean实现的ServletContextAware或者ServletConfigAware接口为bean设置ServletContext或者ServletConfig属性 引入时机:在ServletWebServerApplicationContext #postProcessBeanFactory中登记到应用上下文
o.sf.context.annotation.ConfigurationClassPostProcessor$ImportAwareBeanPostProcessor |	TBD
o.sf.context.support.PostProcessorRegistrationDelegate$BeanPostProcessorChecker	| TBD
o.sf.boot.context.properties.ConfigurationPropertiesBindingPostProcessor	| 功能: 绑定配置文件中的配置属性项到配置属性对象,比如server开头的配置项设置到配置属性bean对象ServerProperties上
o.sf.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator	| 功能: 如果某个bean匹配了某些定义的切面advise或者Spring Advisor,则为这个bean创建AOP代理对象
o.sf.boot.autoconfigure.jdbc.DataSourceInitializerPostProcessor	| 功能: 一旦检测到数据源DataSource bean被初始化，执行数据源的初始化:创建相应的表格(create schema)和填充相应的数据(init schema)
o.sf.validation.beanvalidation.MethodValidationPostProcessor	| 功能: 处理bean中的JSR-303方法验证注解，创建相应的方法验证AOP advise关联到符合条件的bean
o.sf.dao.annotation.PersistenceExceptionTranslationPostProcessor	| TBD
o.sf.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor	| TBD
o.sf.boot.web.server.ErrorPageRegistrarBeanPostProcessor	| 功能: 在ErrorPageRegistry bean创建时初始化前将容器中的所有ErrorPageRegistrar bean注册进来。
o.sf.boot.autoconfigure.orm.jpa.DataSourceInitializedPublisher	| TBD
o.sf.data.web.config.ProjectingArgumentResolverRegistrar$ProjectingArgumentResolverBeanPostProcessor	| TBD
o.sf.orm.jpa.support.PersistenceAnnotationBeanPostProcessor	| 功能: 识别bean上的持久化注解@PersistenceUnit/@PersistenceContext,并完成相应的属性EntityManagerFactory/EntityManager注入。
o.sf.context.annotation.CommonAnnotationBeanPostProcessor	| 功能: 对JSR-250 @Resource、@PostConstruct 、@PreDestroy等注解的处理
o.sf.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor	| 功能: 对每个bean执行真正的依赖"注入",缺省支持三种自动装配注解@Autowired,@Value,JSR-330 @Inject
o.sf.context.support.ApplicationListenerDetector	| 功能: 检测单例ApplicationListener bean将它们注册到应用上下文的事件多播器上，并在这些bean销毁之前将它们从事件多播器上移除
org.sf.beans.factory.annotation.RequiredAnnotationBeanPostProcessor	| 功能: 对 @Required的处理
