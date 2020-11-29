#### Spring容器的 refresh() 方法，synchronized (this.startupShutdownMonitor)实现同步

##### 第一步:prepareRefresh():刷新前的预处理：(Prepare this context for refreshing.)

   (1) initPropertySources():初始化一些属性设置,默认不做任何处理,留给子类自定义属性设置方法;//servletContextInitParams 和 servletConfigInitParams
    
       - AbstractRefreshableWebApplicationContext
       
         - ConfigurableEnvironment env = getEnvironment();
         
         - ((ConfigurableWebEnvironment) env).initPropertySources(this.servletContext, this.servletConfig);
         
           - WebApplicationContextUtils.initServletPropertySources(getPropertySources(), servletContext, servletConfig);
           
           - 解析 servletContextInitParams 和 servletConfigInitParams 参数
   
   (2) getEnvironment().validateRequiredProperties():校验非空属性是否设置了值，没有设置的话抛出异常(MissingRequiredPropertiesException);//Validate that all properties marked as required are resolvable:see ConfigurablePropertyResolver#setRequiredProperties
   
       - systemEnvironment
      
       - systemProperties
       
   (3) earlyApplicationEvents= new LinkedHashSet<ApplicationEvent>():保存容器中的一些早期的事件,如果存在则先清理掉旧的监听器
  
##### 第二步:ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory():获取beanFactory （Tell the subclass to refresh the internal bean factory.）


   #### 子类实现: AbstractRefreshableApplicationContext
   
   (1) refreshBeanFactory(): 刷新(CAS刷新容器状态)并创建，this.beanFactory = new DefaultListableBeanFactory()并设置id;
     
       - destroyBeans()&closeBeanFactory()
       
       - DefaultListableBeanFactory beanFactory = createBeanFactory();
       
         - new DefaultListableBeanFactory(getInternalParentBeanFactory());//Return the internal bean factory of the parent context if it implements ConfigurableApplicationContext; else, return the parent context itself.
         
       - beanFactory.setSerializationId(getId());//设置Id
       
       - customizeBeanFactory(beanFactory);//让子类实现
       
         - beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
         
         - beanFactory.setAllowCircularReferences(this.allowCircularReferences);
         
       - loadBeanDefinitions(beanFactory);//让子类实现
       
         ### 子类实现:AnnotationConfigWebApplicationContext 
         
         - AnnotatedBeanDefinitionReader reader = getAnnotatedBeanDefinitionReader(beanFactory);
		
         - ClassPathBeanDefinitionScanner scanner = getClassPathBeanDefinitionScanner(beanFactory);
         		
         - BeanNameGenerator beanNameGenerator = getBeanNameGenerator();
 		
         - ScopeMetadataResolver scopeMetadataResolver = getScopeMetadataResolver();

         - reader.register(ClassUtils.toClassArray(this.annotatedClasses));

	     - scanner.scan(StringUtils.toStringArray(this.basePackages));
	     
	     - String[] configLocations = getConfigLocations();//获得 XmlWebApplicationContext 的资源路径

         - 先reader.register(clazz);出现异常则 scanner.scan(configLocation);

   (2) getBeanFactory(): 返回刚才创建的BeanFactory对象 DefaultListableBeanFactory；
	
##### 第三步:prepareBeanFactory(beanFactory):BeanFactory的预准备工作（BeanFactory进行一些设置;

       ```
       // 准备当前上下文使用的Bean容器 BeanFactory,设置其标准上下文特征，比如类加载器等
	   // 1. BeanFactory 的类加载器设置为当前上下文的类加载器
	   // 2. BeanFactory 的Bean表达式解析器设置为 new StandardBeanExpressionResolver()
	   // 3. BeanFactory 增加 BeanPostProcessror new ApplicationListenerDetector(this)
	   // 4.三个单例Bean被注册 : environment,systemProperties,systemEnvironment
	    	
       ```
	  
   (1) 设置BeanFactory的类加载器(setBeanClassLoader)、设置表达式解析器(setBeanExpressionResolver)、添加属性注册器(addPropertyEditorRegistrar)
	
   (2) 添加部分BeanPostProcessor【ApplicationContextAwareProcessor,设置EmbeddedValueResolver值解析器;设置忽略的自动装配的接口EnvironmentAware;
	
   (3) 注册可以解析的自动装配;我们能直接在任何组件中自动注入:BeanFactory、ResourceLoader、ApplicationEventPublisher、ApplicationContext
   
   ```
        beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
   		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
   		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
   		beanFactory.registerResolvableDependency(ApplicationContext.class, this);
   		
   		   --> this.resolvableDependencies.put(dependencyType, autowiredValue);
	
   ```

   (4) 添加BeanPostProcessor【ApplicationListenerDetector】//Register early post-processor for detecting inner beans as ApplicationListeners.
	
   (5) 添加编译时的AspectJ; beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory)) & 	beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
	
   (6) 给BeanFactory中注册一些能用的组件: environment,systemProperties,systemEnvironment

##### 第四步:postProcessBeanFactory(beanFactory);
     
   - 子类通过重写这个方法来在BeanFactory创建并预准备完成以后做进一步的设置;
   
    
        /**
     	 * Modify the application context's internal bean factory after its standard
     	 * initialization. All bean definitions will have been loaded, but no beans
     	 * will have been instantiated yet. This allows for registering special
     	 * BeanPostProcessors etc in certain ApplicationContext implementations.
     	 * @param beanFactory the bean factory used by the application context
     	 */
    
   
    子类实现: AnnotationConfigServletWebServerApplicationContext
            
            - super.postProcessBeanFactory(beanFactory);
              
              - beanFactory.addBeanPostProcessor(new WebApplicationContextServletContextAwareProcessor(this));
              
              - this.scanner.scan(this.basePackages);
              
              - this.reader.register(ClassUtils.toClassArray(this.annotatedClasses));

##### 第五步:invokeBeanFactoryPostProcessors(beanFactory):Instantiate and invoke all registered BeanFactoryPostProcessor beans,respecting explicit order if given.Must be called before singleton instantiation.
      
   ### 委托给:PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());来实现功能
                                                 	
   ### 两个接口:接口BeanDefinitionRegistryPostProcessor 继承自 BeanFactoryPostProcessor
	
   - 如果beanFactory是 BeanDefinitionRegistry 先执行 BeanDefinitionRegistryPostProcessor的 postProcessBeanDefinitionRegistry方法:
	    
     (1) 获取所有的BeanDefinitionRegistryPostProcessor;
		
     (2) 看先执行实现了PriorityOrdered优先级接口的BeanDefinitionRegistryPostProcessor,执行 invokeBeanDefinitionRegistryPostProcessors=>postProcessBeanDefinitionRegistry
			
     (3) 在执行实现了Ordered顺序接口的BeanDefinitionRegistryPostProcessor,执行 invokeBeanDefinitionRegistryPostProcessors=>postProcessBeanDefinitionRegistry
			
     (4) 最后执行没有实现任何优先级或者是顺序接口的BeanDefinitionRegistryPostProcessors,执行 invokeBeanDefinitionRegistryPostProcessors=>postProcessBeanDefinitionRegistry
	
   - 否则直接执行 invokeBeanFactoryPostProcessors(postProcessors,beanFactory)=>postProcessBeanFactory
		
   - 最后执行非参数传递的而是以bean形式存在的 BeanFactoryPostProcessor 的方法:
   
     (1) 获取所有的BeanFactoryPostProcessor
		
     (2) 看先执行实现了PriorityOrdered优先级接口的BeanFactoryPostProcessor,执行postProcessor.postProcessBeanFactory(beanFactory)
			
     (3) 在执行实现了Ordered顺序接口的BeanFactoryPostProcessor,执行postProcessor.postProcessBeanFactory(beanFactory)
			
     (4) 最后执行没有实现任何优先级或者是顺序接口的BeanFactoryPostProcessor,执行postProcessor.postProcessBeanFactory(beanFactory)
     
   ### beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
   
   ### beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
     
			
##### 第六步:registerBeanPostProcessors,注册BeanPostProcessor(注册到beanFactory)

   ### 委托给:PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this) 来实现功能
		
   ### BeanPostProcessor类型:DestructionAwareBeanPostProcessor、InstantiationAwareBeanPostProcessor、SmartInstantiationAwareBeanPostProcessor、MergedBeanDefinitionPostProcessor
		
   (1) 获取所有的 BeanPostProcessor;后置处理器都默认可以通过PriorityOrdered、Ordered接口来执行优先级
		
   (2) 先注册PriorityOrdered级别的BeanPostProcessor=>registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);
   
   (3) 再注册Ordered接口级别的BeanPostProcessor=>registerBeanPostProcessors(beanFactory, orderedPostProcessors);
   
   (4) 最后注册没有实现任何优先级接口的 BeanPostProcessor=>registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);
		
   (5) 最终注册实现MergedBeanDefinitionPostProcessor接口的BeanPostProcessor =>registerBeanPostProcessors(beanFactory, internalPostProcessors);

   (6) 注册一个ApplicationListenerDetector在beanPostProcessor chain 尾部
   
##### 第七步:initMessageSource();在SpringMVC中做初始化MessageSource组件（做国际化功能,消息绑定，消息解析):

   （1）获取BeanFactory=>getBeanFactory()
		
   （2）看容器中是否有id为messageSource的,类型是MessageSource的组件如果有赋值给messageSource,如果没有自己创建一个DelegatingMessageSource的空的MessageSource,所有请求都请求到了父MessageSources;
      //If no parent is available, it simply won't resolve any message.
      
     ```
        MessageSource:取出国际化配置文件中的某个key的值,能按照区域信息获取;
          
     ```		
   (3) 把创建好的MessageSource注册在容器中，以后获取国际化配置文件的值的时候，可以自动注入MessageSource;
       
     ```
       beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);	
	   MessageSource.getMessage(String code, Object[] args, String defaultMessage, Locale locale);
	  
     ```
##### 第八步:initApplicationEventMulticaster();初始化事件派发器；
		
   (1) 获取BeanFactory=>	ConfigurableListableBeanFactory beanFactory = getBeanFactory();

   (2) 从BeanFactory中获取name = "applicationEventMulticaster"的applicationEventMulticaster;
   
   (3) 如果上一步没有配置则创建一个SimpleApplicationEventMulticaster;
   
   (4) 将创建的ApplicationEventMulticaster添加到BeanFactory中，以后其他组件直接自动注入=>	beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
   
   		
##### 第九步:onRefresh() 模版方法让子类实现

	// Called on initialization of special beans, before instantiation of singletons.
   ### 子类重写这个方法,默认不做任何操作在容器刷新的时候可以自定义逻辑; 内嵌tomcat在这个地方实例化;
   ### springMVC 中 DispatcherServlet里的initStrategies()初始化九大组件也在这里实现
   
#### 转为 AbstractApplicationContext  

#### 子类 GenericApplicationContext

#### 具体类型子类 ServletWebServerApplicationContext;ReactiveWebServerApplicationContext;StaticWebApplicationContext
   
   - createWebServer() [springBoot tomcat启动](../springboot/SpringBoot源码解析之tomcat启动过程.md)
     
     - ServletContext servletContext = getServletContext();//获取Servlet容器
     
     - ServletWebServerFactory factory = getWebServerFactory();//获取WebServerFactory工厂方法
     
       - getSelfInitializer(); // 获取 **ServletContextInitializer** 接口的实现类实现 onStartup(ServletContext servletContext)
       
         - selfInitialize(ServletContext servletContext)；
       
           - prepareWebApplicationContext(servletContext); //Initializing Spring embedded WebApplicationContext 初始化 WebApplicationContext;创建一个 WebApplicationContext.class.getName() + ".ROOT"
         
           - registerApplicationScope(servletContext);
         
           - WebApplicationContextUtils.registerEnvironmentBeans(getBeanFactory(),servletContext);//Register web-specific environment beans ("contextParameters", "contextAttributes") with the given BeanFactory, as used by the WebApplicationContext
           
   - initPropertySources();
               
##### 第十步:registerListeners();
	
   （1）从容器中获取静态的ApplicationListener; 然后直接触发 => getApplicationEventMulticaster().addApplicationListener(listener);

   （2）将非静态监听器名字添加到事件派发器中 =》 getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
 
   （3）派发器派发一些早起的事件 =>getApplicationEventMulticaster().multicastEvent(earlyEvent);		

##### 第十一步:finishBeanFactoryInitialization(beanFactory);//初始化所有剩下的单实例bean；

   - 设置conversionService方法=>beanFactory.setConversionService(beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));//Initialize conversion service for this context.

   - 注册一个字符解析器=>beanFactory.addEmbeddedValueResolver(strVal => getEnvironment().resolvePlaceholders(strVal));//Register a default embedded value resolver if no bean post-processor (such as a PropertyPlaceholderConfigurer bean) registered any before:at this point, primarily for resolution in annotation attribute values.

   - **getBean(weaverAwareName);** // Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
   
   - beanFactory.setTempClassLoader(null);// Stop using the temporary ClassLoader for type matching.
   
   - beanFactory.freezeConfiguration();//冻结所有bean定义，注册的bean定义不会被修改或进一步后处理，因为马上要创建 Bean 实例对象了;Allow for caching all bean definition metadata, not expecting further changes.

   - beanFactory.preInstantiateSingletons();初始化后剩下的单实例非懒加载的bean;// Instantiate all remaining (non-lazy-init) singletons.
   
     #### 子类实现: DefaultListableBeanFactory

     - List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);//获取容器中的所有的 beanDefinitionNames

     - RootBeanDefinition = getMergedLocalBeanDefinition(beanName); //获取Bean的定义信息,Bean不是抽象的,是单实例的,不是懒加载;
      
     ##### 判断是否是FactoryBean => isFactoryBean(beanName)
            
     ##### 是工厂bean,则利用工厂方法创建bean
            
      //是 A standard FactoryBean is not expected to initialize eagerly,工厂方法获取bean
      - Object bean = getBean(FACTORY_BEAN_PREFIX + beanName); [spring getBean()方法](./Spring源码解析之createBean()方法.md)

     ###### 赋值之前使用后置处理器:

          - 拿到InstantiationAwareBeanPostProcessor后置处理器,postProcessAfterInstantiation()；

          - 拿到InstantiationAwareBeanPostProcessor后置处理器,postProcessPropertyValues()；

          - 应用Bean属性的值；为属性利用setter方法等进行赋值=> applyPropertyValues(beanName, mbd, bw, pvs);

          - Bean初始化=>initializeBean(beanName, exposedObject, mbd);

            - 执行Aware接口方法=>invokeAwareMethods(beanName, bean);执行xxxAware接口的方法BeanNameAware、BeanClassLoaderAware、BeanFactoryAware

            - 执行后置处理器初始化之前=>applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName)=>BeanPostProcessor.postProcessBeforeInitialization();

            - 执行初始化方法=>invokeInitMethods(beanName, wrappedBean, mbd):
   
              - 是否是InitializingBean接口的实现；执行接口规定的初始化；
   
              - 是否自定义初始化方法；

          - 执行后置处理器初始化之后=>pplyBeanPostProcessorsAfterInitialization=>BeanPostProcessor.postProcessAfterInitialization();

             - 注册Bean的销毁方法=>registerDisposableBeanIfNecessary

             - 将创建的Bean添加到缓存中singletonObjects;
					
     ##### 不是工厂Bean,利用getBean(beanName)直接创建对象
            
   - 遍历所有的bean实现了 SmartInitializingSingleton接口的执行=>smartSingleton.afterSingletonsInstantiated()
                 
##### 第十二步:finishRefresh() => 完成BeanFactory的初始化创建工作；IOC容器就创建完成；[Tomcat真正启动](SpringBoot源码解析之Tomcat启动过程.md)
	
   - clearResourceCaches()
  
   - initLifecycleProcessor();初始化和生命周期有关的后置处理器；LifecycleProcessor 默认从容器中找是否有lifecycleProcessor的组件【LifecycleProcessor】；如果没有new DefaultLifecycleProcessor();加入到容器；写一个LifecycleProcessor的实现类，可以在BeanFactory void onRefresh();void onClose();	
		
   - getLifecycleProcessor().onRefresh();拿到前面定义的生命周期处理器（BeanFactory）；回调onRefresh()；
   
   - publishEvent(new ContextRefreshedEvent(this));发布容器刷新完成事件；
		
   - liveBeansView.registerApplicationContext(this);
   
#### 子类: ServletWebServerApplicationContext

   - WebServer webServer = startWebServer();//子类启动tomcat容器，发布事件(Tomcat started on port(s): XX (http) with context path '/XX')
     		
## root容器启动成功,监听到时间后创建 DispatcherServlet->FrameworkServlet(initServletBean())->HttpServletBean->HttpServlet
  
     