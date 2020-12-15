#### Spring容器的 refresh() 方法，synchronized (this.startupShutdownMonitor)实现同步

#### 第一步:prepareRefresh(); 刷新前的预处理:(Prepare this context for refreshing.)

   (1) initPropertySources();//初始化一些属性设置,默认不做任何处理,留给子类自定义属性设置方法;//servletContextInitParams 和 servletConfigInitParams
    
       - AbstractRefreshableWebApplicationContext
       
         - ConfigurableEnvironment env = getEnvironment();
         
         - ((ConfigurableWebEnvironment) env).initPropertySources(this.servletContext, this.servletConfig);
         
           - WebApplicationContextUtils.initServletPropertySources(getPropertySources(), servletContext, servletConfig);//解析 servletContextInitParams 和 servletConfigInitParams 参数
   
   (2) getEnvironment().validateRequiredProperties();//校验非空属性是否设置了值，没有设置的话抛出异常(MissingRequiredPropertiesException);//Validate that all properties marked as required are resolvable:see ConfigurablePropertyResolver#setRequiredProperties
   
       - systemEnvironment
      
       - systemProperties
       
   (3) earlyApplicationEvents= new LinkedHashSet<ApplicationEvent>(); //保存容器中的一些早期的事件,如果存在则先清理掉旧的监听器
  
#### 第二步:ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory(): 获得一个刷新的bean容器 （Tell the subclass to refresh the internal bean factory.）

   ##### 子类实现: AbstractRefreshableApplicationContext //子类真正的执行配置加载操作,在其他初始化工作之前就先执行完毕。返回一个单例的BeanFactory,这个操作是非线程安全的，会抛出IllegalStateException异常
   
   (1) refreshBeanFactory(): 刷新(CAS刷新容器状态)并创建，this.beanFactory = new DefaultListableBeanFactory()并设置id;
     
       - destroyBeans()&closeBeanFactory() //如果已经存在beanFactory则先销毁旧的容器
       
       - DefaultListableBeanFactory beanFactory = createBeanFactory();
       
         - new DefaultListableBeanFactory(getInternalParentBeanFactory());
         
         ````
         Return the internal bean factory of the parent context if it implements ConfigurableApplicationContext; else, return the parent context itself.
         返回当前容器父容器的底层容器 beanFactory,如果没有则返回当前容器的父容器本身。
         
         ````
         
       - beanFactory.setSerializationId(getId());//设置beanFactory 全局唯一 Id
       
       - customizeBeanFactory(beanFactory);//让子类实现,设置bean定义是否允许复写和是否允许循环引用
       
         - beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);//设置是否允许通过注册具有相同名称的不同定义来覆盖bean定义，并自动替换前者。否则，将引发异常。默认值为“true”。
                                                                                                     
         - beanFactory.setAllowCircularReferences(this.allowCircularReferences);//设置是否允许bean之间的循环引用并自动尝试解析它们。默认值为“true”。关闭此选项可在遇到循环引用时引发异常，完全不允许循环引用
         
       - loadBeanDefinitions(beanFactory);//让子类实现
       
   ##### 子类实现:AnnotationConfigWebApplicationContext 
         
         - AnnotatedBeanDefinitionReader reader = getAnnotatedBeanDefinitionReader(beanFactory);
		
         - ClassPathBeanDefinitionScanner scanner = getClassPathBeanDefinitionScanner(beanFactory);
         		
         - BeanNameGenerator beanNameGenerator = getBeanNameGenerator();
 		
         - ScopeMetadataResolver scopeMetadataResolver = getScopeMetadataResolver();

         - reader.register(ClassUtils.toClassArray(this.annotatedClasses));//Registering component classes

	     - scanner.scan(StringUtils.toStringArray(this.basePackages));//Scanning base packages
	     
	     - String[] configLocations = getConfigLocations();//获得 XmlWebApplicationContext 的资源路径,先reader.register(new Class[]{clazz}) 若异常 scanner.scan(new String[]{configLocation})

  
   (2) getBeanFactory(): 返回刚才创建的BeanFactory对象 DefaultListableBeanFactory;
	
#### 第三步:prepareBeanFactory(beanFactory):BeanFactory的预准备工作（BeanFactory进行一些设置;
  
```
        // Tell the internal bean factory to use the context's class loader etc.
        //设置类加载器：存在则直接设置/不存在则新建一个默认类加载器
		beanFactory.setBeanClassLoader(getClassLoader());
        //设置EL表达式解析器（Bean初始化完成后填充属性时会用到）
		beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
        //设置属性注册解析器PropertyEditor
		beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

		// Configure the bean factory with context callbacks.
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
        //设置忽略自动装配的接口
		beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
		beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
		beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
		beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

		// BeanFactory interface not registered as resolvable type in a plain factory.
		// MessageSource registered (and found for autowiring) as a bean.
        // 当有其他类要注入指定类型的对象时，就给他注入指定的 beanFactory,resourceLoader,ApplicationEventPublisher,ApplicationContext
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);

		// Register early post-processor for detecting inner beans as ApplicationListeners.
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

		// Detect a LoadTimeWeaver and prepare for weaving, if found.
        //如果当前BeanFactory包含loadTimeWeaver Bean,说明存在类加载期织入AspectJ,则把当前BeanFactory交给类加载期BeanPostProcessor实现类LoadTimeWeaverAwareProcessor来处理，从而实现类加载期织入AspectJ的目的。
		if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			// Set a temporary ClassLoader for type matching.
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}

		// Register default environment beans.
        //注册当前容器环境environment组件Bean
		if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
		}
        //注册系统配置systemProperties组件Bean
		if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
		}
        //注册系统环境systemEnvironment组件Bean
		if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
		}
```

#### 第四步:postProcessBeanFactory(beanFactory);
     
   - 子类通过重写这个方法来在BeanFactory创建并预准备完成以后做进一步的设置;
   
   ````  
    它在spring容器加载了bean的定义文件之后，在bean实例化之前执行的。也就是说，Spring允许BeanFactoryPostProcessor在容器创建bean之前读取bean配置元数据，并可进行修改
   ````
      
   ###### 子类实现: AnnotationConfigServletWebServerApplicationContext
            
   - super.postProcessBeanFactory(beanFactory);
              
     - beanFactory.addBeanPostProcessor(new WebApplicationContextServletContextAwareProcessor(this));
              
     - this.scanner.scan(this.basePackages);
              
     - this.reader.register(ClassUtils.toClassArray(this.annotatedClasses));

#### 第五步:invokeBeanFactoryPostProcessors(beanFactory):
  
   - Spring Boot调用AbstractApplicationContext的refresh方法，通过refresh的invokeBeanFactoryPostProcessors处理了 DeferredImportSelector 类型，完成了自动装配

   ````
   Instantiate and invoke all registered BeanFactoryPostProcessor beans,respecting explicit order if given.Must be called before singleton instantiation.

   在实例化之前,执行所有的BeanFactoryPostProcessor
   ````
   ##### 委托给:PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());来实现功能
                                                 	
   ##### 两个接口:接口 BeanDefinitionRegistryPostProcessor 继承自 BeanFactoryPostProcessor
	
   ````
   final class PostProcessorRegistrationDelegate {
       public static void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
           Set<String> processedBeans = new HashSet<>();
           //判断我们的beanFactory是否实现了BeanDefinitionRegistry
           if (beanFactory instanceof BeanDefinitionRegistry) {
               //强行把beanFactory转为BeanDefinitionRegistry
               BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

               //保存 BeanFactoryPostProcessor 类型的后置处理器
               List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();

               //保存BeanDefinitionRegistryPostProcessor类型的后置处理器
               List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
   
               //循环我们传递进来的beanFactoryPostProcessors
               for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
                   //判断我们的后置处理器是不是BeanDefinitionRegistryPostProcessor
                   if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
                       //如果是的化进行强制转化
                       BeanDefinitionRegistryPostProcessor registryProcessor =(BeanDefinitionRegistryPostProcessor) postProcessor;
                       //调用它的后置方法
                       registryProcessor.postProcessBeanDefinitionRegistry(registry);
                       //添加到我们用于保存的BeanDefinitionRegistryPostProcessor的集合中
                       registryProcessors.add(registryProcessor);
                   }
                   else {//若没有实现BeanDefinitionRegistryPostProcessor接口，那么他就是 BeanFactoryPostProcessor 把当前的后置处理器加入到regularPostProcessors中，没有执行
                       regularPostProcessors.add(postProcessor);
                   }
               }
   
               //定义一个集合用户保存当前准备创建的 BeanDefinitionRegistryPostProcessor
               List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();
   
               //第一步:去容器中获取BeanDefinitionRegistryPostProcessor的bean的处理器名称
               String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
               //循环上一步获取的BeanDefinitionRegistryPostProcessor的类型名称
               for (String ppName : postProcessorNames) {
                   //判断是否实现了PriorityOrdered接口的
                   if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                       //显示的调用getBean()的方式获取出该对象然后加入到currentRegistryProcessors集合中去
                       currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                       //同时也加入到processedBeans集合中去
                       processedBeans.add(ppName);
                   }
               }
               //对currentRegistryProcessors集合中BeanDefinitionRegistryPostProcessor进行排序
               sortPostProcessors(currentRegistryProcessors, beanFactory);
               //把他加入到用于保存到registryProcessors中
               registryProcessors.addAll(currentRegistryProcessors);
               /**
                * 在这里典型的BeanDefinitionRegistryPostProcessor就是 ConfigurationClassPostProcessor 用于进行bean定义的加载 比如我们的包扫描，@import等
                */
               invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
               //调用完之后，马上clear
               currentRegistryProcessors.clear();
   
               //下一步 又去容器中获取BeanDefinitionRegistryPostProcessor的bean的处理器名称
               postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
               //循环上一步获取的BeanDefinitionRegistryPostProcessor的类型名称
               for (String ppName : postProcessorNames) {
                   //表示没有被处理过,且实现了Ordered接口的
                   if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
                       //显示的调用getBean()的方式获取出该对象然后加入到currentRegistryProcessors集合中去
                       currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                       //同时也加入到processedBeans集合中去
                       processedBeans.add(ppName);
                   }
               }
               //对currentRegistryProcessors集合中BeanDefinitionRegistryPostProcessor进行排序
               sortPostProcessors(currentRegistryProcessors, beanFactory);
               //把他加入到用于保存到registryProcessors中
               registryProcessors.addAll(currentRegistryProcessors);
               //调用他的后置处理方法
               invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
               //调用完之后，马上clear
               currentRegistryProcessors.clear();
   
               //调用没有实现任何优先级接口的BeanDefinitionRegistryPostProcessor
               //定义一个重复处理的开关变量 默认值为true
               boolean reiterate = true;
               //第一次就可以进来
               while (reiterate) {
                   //进入循环马上把开关变量给改为false
                   reiterate = false;
                   //去容器中获取BeanDefinitionRegistryPostProcessor的bean的处理器名称
                   postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
                   //循环上一步获取的BeanDefinitionRegistryPostProcessor的类型名称
                   for (String ppName : postProcessorNames) {
                       //没有被处理过的
                       if (!processedBeans.contains(ppName)) {
                           //显示的调用getBean()的方式获取出该对象然后加入到currentRegistryProcessors集合中去
                           currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                           //同时也加入到processedBeans集合中去
                           processedBeans.add(ppName);
                           //再次设置为true
                           reiterate = true;
                       }
                   }
                   //对currentRegistryProcessors集合中BeanDefinitionRegistryPostProcessor进行排序
                   sortPostProcessors(currentRegistryProcessors, beanFactory);
                   //把他加入到用于保存到registryProcessors中
                   registryProcessors.addAll(currentRegistryProcessors);
                   //调用他的后置处理方法
                   invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
                   //进行clear
                   currentRegistryProcessors.clear();
               }
   
               //调用实现了BeanDefinitionRegistryPostProcessor的接口 他是他也同时实现了BeanFactoryPostProcessor的方法
               invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
               //调用BeanFactoryPostProcessor
               invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
           }
   
           else { //若当前的beanFactory没有实现了BeanDefinitionRegistry 直接调用beanFactoryPostProcessor接口的方法进行后置处理
               invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
           }
   
           //最后一步 获取容器中所有的 BeanFactoryPostProcessor
           String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);
   
           //保存BeanFactoryPostProcessor类型实现了priorityOrdered
           List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
           //保存BeanFactoryPostProcessor类型实现了Ordered接口的
           List<String> orderedPostProcessorNames = new ArrayList<>();
           //保存BeanFactoryPostProcessor没有实现任何优先级接口的
           List<String> nonOrderedPostProcessorNames = new ArrayList<>();
           for (String ppName : postProcessorNames) {
               //processedBeans包含的话，表示在上面处理BeanDefinitionRegistryPostProcessor的时候处理过了
               if (processedBeans.contains(ppName)) {
                   // skip - already processed in first phase above
               }
               //判断是否实现了PriorityOrdered
               else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                   priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
               }
               //判断是否实现了Ordered
               else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
                   orderedPostProcessorNames.add(ppName);
               }
               //没有实现任何的优先级接口的
               else {
                   nonOrderedPostProcessorNames.add(ppName);
               }
           }
   
           //先调用BeanFactoryPostProcessor实现了PriorityOrdered接口的
           sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
           invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);
   
           //再调用BeanFactoryPostProcessor实现了Ordered.
           List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
           for (String postProcessorName : orderedPostProcessorNames) {
               orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
           }
           sortPostProcessors(orderedPostProcessors, beanFactory);
           invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);
   
           //调用没有实现任何方法接口的
           List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
           for (String postProcessorName : nonOrderedPostProcessorNames) {
               nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
           }
           invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);
   
           // Clear cached merged bean definitions since the post-processors might have
           // modified the original metadata, e.g. replacing placeholders in values...
           beanFactory.clearMetadataCache();
       }
   ````
   - beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
   
   - beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
     
			
#### 第六步:registerBeanPostProcessors,注册BeanPostProcessor(注册到beanFactory)

   ##### 委托给:PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this) 来实现功能
		
   ##### BeanPostProcessor类型:DestructionAwareBeanPostProcessor、InstantiationAwareBeanPostProcessor、SmartInstantiationAwareBeanPostProcessor、MergedBeanDefinitionPostProcessor
		
   ````
   public static void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
        //找出所有实现 BeanPostProcessor接口的类
   		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);
   
   		// Register BeanPostProcessorChecker that logs an info message when
   		// a bean is created during BeanPostProcessor instantiation, i.e. when
   		// a bean is not eligible for getting processed by all BeanPostProcessors.
   		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
        //添加BeanPostProcessorChecker(主要用于记录信息)到beanFactory中
   		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));
   
   		// Separate between BeanPostProcessors that implement PriorityOrdered,
   		// Ordered, and the rest.
        //定义不同的变量用于区分: 实现PriorityOrdered接口的BeanPostProcessor、实现Ordered接口的BeanPostProcessor、普通BeanPostProcessor
        //priorityOrderedPostProcessors: 用于存放实现PriorityOrdered接口的BeanPostProcessor
   		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>(); 
        //internalPostProcessors: 用于存放Spring内部的BeanPostProcessor
   		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
        //orderedPostProcessorNames: 用于存放实现Ordered接口的BeanPostProcessor的beanName
   		List<String> orderedPostProcessorNames = new ArrayList<>();
        //nonOrderedPostProcessorNames: 用于存放普通BeanPostProcessor的beanName
   		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
        //遍历postProcessorNames, 将BeanPostProcessors按3.1 - 3.4定义的变量区分开
   		for (String ppName : postProcessorNames) {
   			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
   				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
                // 如果ppName对应的Bean实例实现了PriorityOrdered接口, 则拿到ppName对应的Bean实例并添加到priorityOrderedPostProcessors
   				priorityOrderedPostProcessors.add(pp);
   				if (pp instanceof MergedBeanDefinitionPostProcessor) {
                    //如果ppName对应的Bean实例也实现了MergedBeanDefinitionPostProcessor接口,则将ppName对应的Bean实例添加到internalPostProcessors
   					internalPostProcessors.add(pp);
   				}
   			}
   			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
                //如果ppName对应的Bean实例没有实现PriorityOrdered接口, 但是实现了Ordered接口, 则将ppName添加到orderedPostProcessorNames
   				orderedPostProcessorNames.add(ppName);
   			}
   			else {
                //否则, 将ppName添加到nonOrderedPostProcessorNames
   				nonOrderedPostProcessorNames.add(ppName);
   			}
   		}
   
   		// First, register the BeanPostProcessors that implement PriorityOrdered.
        //第一步:首先, 注册实现PriorityOrdered接口的BeanPostProcessors;
        //对priorityOrderedPostProcessors进行排序
   		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
        //注册priorityOrderedPostProcessors
   		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);
   
   		// Next, register the BeanPostProcessors that implement Ordered.
        // 第二步:注册实现Ordered接口的BeanPostProcessors
   		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
   		for (String ppName : orderedPostProcessorNames) {
            // 拿到ppName对应的BeanPostProcessor实例对象
   			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
            //将ppName对应的BeanPostProcessor实例对象添加到orderedPostProcessors, 准备执行注册
   			orderedPostProcessors.add(pp);
   			if (pp instanceof MergedBeanDefinitionPostProcessor) {
                //如果ppName对应的Bean实例也实现了MergedBeanDefinitionPostProcessor接口,
                //则将ppName对应的Bean实例添加到internalPostProcessors
   				internalPostProcessors.add(pp);
   			}
   		}
        //对orderedPostProcessors进行排序
   		sortPostProcessors(orderedPostProcessors, beanFactory);
        //注册orderedPostProcessors
   		registerBeanPostProcessors(beanFactory, orderedPostProcessors);
   
   		// Now, register all regular BeanPostProcessors.
        //注册所有常规的BeanPostProcessors
   		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
   		for (String ppName : nonOrderedPostProcessorNames) {
   			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
   			nonOrderedPostProcessors.add(pp);
   			if (pp instanceof MergedBeanDefinitionPostProcessor) {
   				internalPostProcessors.add(pp);
   			}
   		}
   		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);
   
   		// Finally, re-register all internal BeanPostProcessors.
        // 最后, 重新注册所有内部BeanPostProcessors（相当于内部的BeanPostProcessor会被移到处理器链的末尾）
        // 对internalPostProcessors进行排序
   		sortPostProcessors(internalPostProcessors, beanFactory);
        //注册internalPostProcessors
   		registerBeanPostProcessors(beanFactory, internalPostProcessors);
   
   		// Re-register post-processor for detecting inner beans as ApplicationListeners,
   		// moving it to the end of the processor chain (for picking up proxies etc).
        //重新注册ApplicationListenerDetector
   		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
   	}
   ````
   
#### 第七步:initMessageSource();在SpringMVC中做初始化MessageSource组件(做国际化功能,消息绑定，消息解析)  :

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
#### 第八步:initApplicationEventMulticaster();初始化事件派发器；
		
   (1) 获取BeanFactory=>	ConfigurableListableBeanFactory beanFactory = getBeanFactory();

   (2) 从BeanFactory中获取name = "applicationEventMulticaster"的 applicationEventMulticaster;
   
   (3) 如果上一步没有配置则创建一个 SimpleApplicationEventMulticaster;
   
   (4) 将创建的ApplicationEventMulticaster添加到BeanFactory中，以后其他组件直接自动注入=>	beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
   
   		
#### 第九步:onRefresh() 模版方法让子类实现

	// Called on initialization of special beans, before instantiation of singletons.  特定bean初始化的时候调用，实例化之前调用。
   ##### 子类重写这个方法,默认不做任何操作在容器刷新的时候可以自定义逻辑; 内嵌tomcat在这个地方实例化;
   ##### springMVC 中 DispatcherServlet里的initStrategies()初始化九大组件也在这里实现
   
##### 具体类型子类 AbstractRefreshableWebApplicationContext;GenericWebApplicationContext;ServletWebServerApplicationContext;ReactiveWebServerApplicationContext;StaticWebApplicationContext
   
   ##### ServletWebServerApplicationContext;//[springBoot tomcat启动](../springboot/SpringBoot源码解析之tomcat启动过程.md)
   
              
#### 第十步:registerListeners();

    ````
    protected void registerListeners() {
    		// Register statically specified listeners first.
    		//（1）从容器中获取静态的ApplicationListener然后注册放入到applicationListeners中
    		for (ApplicationListener<?> listener : getApplicationListeners()) {
    			getApplicationEventMulticaster().addApplicationListener(listener);
    		}
    
    		// Do not initialize FactoryBeans here: We need to leave all regular beans
    		// uninitialized to let post-processors apply to them!
    		//从容器中获取所有实现了ApplicationListener接口的bd的bdName放入applicationListenerBeans
    		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
    		for (String listenerBeanName : listenerBeanNames) {
    			getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
    		}
    
    		// Publish early application events now that we finally have a multicaster...
    		// 发布早期的事件
    		Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
    		this.earlyApplicationEvents = null;
    		if (!CollectionUtils.isEmpty(earlyEventsToProcess)) {
    			for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
    				getApplicationEventMulticaster().multicastEvent(earlyEvent);
    			}
    		}
    	}
    ```` 
#### 第十一步:finishBeanFactoryInitialization(beanFactory);//初始化所有剩下的单实例bean；

   ````
        // Initialize conversion service for this context.
   		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
   				beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
            //为上下文初始化类型转换器
   			beanFactory.setConversionService(
   					beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
   		}
   
   		// Register a default embedded value resolver if no bean post-processor
   		// (such as a PropertyPlaceholderConfigurer bean) registered any before:
   		// at this point, primarily for resolution in annotation attribute values.
   		if (!beanFactory.hasEmbeddedValueResolver()) {
            //检查上下文中是否存在类型转换器
   			beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
   		}
   
   		// Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
        // 尽早初始化LoadTimeWeaverAware bean，以便尽早注册它们的转换器
   		String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
   		for (String weaverAwareName : weaverAwareNames) {
   			getBean(weaverAwareName);
   		}
   
   		// Stop using the temporary ClassLoader for type matching.
        //禁止使用临时类加载器进行类型匹配
   		beanFactory.setTempClassLoader(null);
   
   		// Allow for caching all bean definition metadata, not expecting further changes.
        //允许缓存所有的bean的定义数据;冻结所有bean定义，注册的bean定义不会被修改或进一步后处理，因为马上要创建 Bean 实例对象了
   		beanFactory.freezeConfiguration();
   
   		// Instantiate all remaining (non-lazy-init) singletons.
        //准备实例化bean
   		beanFactory.preInstantiateSingletons();

   ````

   - beanFactory.preInstantiateSingletons();初始化后剩下的单实例非懒加载的bean;// Instantiate all remaining (non-lazy-init) singletons.
   
   ##### 子类实现: DefaultListableBeanFactory
````
            // Iterate over a copy to allow for init methods which in turn register new bean definitions.
     		// While this may not be part of the regular factory bootstrap, it does otherwise work fine.
            // 遍历一个副本以允许init方法，而init方法反过来注册新的bean定义
            // 盛放所有的beanName,所有的需要实例化的beanName都在这里,包括Spring断断续续添加的, Aspectj的, 程序员通过注解标识的
     		List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);
     
     		// Trigger initialization of all non-lazy singleton beans...
            // 触发所有非延迟加载单例beans的初始化，主要步骤为调用getBean
     		for (String beanName : beanNames) {
                //合并父类BeanDefinition,可以进入查看
     			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
                //三个条件,抽象,单例,非懒加载
     			if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                    //如果是FactoryBean则加上&;检验是否是 FactoryBean 类型的对象
     				if (isFactoryBean(beanName)) {
     					Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
     					if (bean instanceof FactoryBean) {
     						FactoryBean<?> factory = (FactoryBean<?>) bean;
     						boolean isEagerInit;
     						if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
     							isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>) ((SmartFactoryBean<?>) factory)::isEagerInit,getAccessControlContext());
     						}
     						else {
     							isEagerInit = (factory instanceof SmartFactoryBean &&
     									((SmartFactoryBean<?>) factory).isEagerInit());
     						}
     						if (isEagerInit) {
     							getBean(beanName); 
     						}
     					}
     				}
     				else {
                        //因为我们没有添加FactoryBean类型的对象, 一般都会进入这个getBean
     					getBean(beanName);
     				}
     			}
     		}
     
     		// Trigger post-initialization callback for all applicable beans...
     		for (String beanName : beanNames) {
     			Object singletonInstance = getSingleton(beanName);
     			if (singletonInstance instanceof SmartInitializingSingleton) {
     				SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
     				if (System.getSecurityManager() != null) {
     					AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
     						smartSingleton.afterSingletonsInstantiated();
     						return null;
     					}, getAccessControlContext());
     				}
     				else {
     					smartSingleton.afterSingletonsInstantiated();
     				}
     			}
     		}
````
##### 合并BeanDefinition定义
````
protected RootBeanDefinition getMergedBeanDefinition(
			String beanName, BeanDefinition bd, @Nullable BeanDefinition containingBd)
			throws BeanDefinitionStoreException {

		synchronized (this.mergedBeanDefinitions) {
			RootBeanDefinition mbd = null;

			// 重新去获取一次，有可能该BeanDefinition已经生成
			if (containingBd == null) {
				mbd = this.mergedBeanDefinitions.get(beanName);
			}

			if (mbd == null) {
				if (bd.getParentName() == null) {
					// 没有父类则深拷贝一个RootBeanDefinition
					if (bd instanceof RootBeanDefinition) {
						mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
					}
					else {
						mbd = new RootBeanDefinition(bd);
					}
				}
				else {
					// 有父类则需要先获取父类的BeanDefinition，流程和获取子类的BeanDefinition一致
					BeanDefinition pbd;
					try {
						String parentBeanName = transformedBeanName(bd.getParentName());
						if (!beanName.equals(parentBeanName)) {
							pbd = getMergedBeanDefinition(parentBeanName);
						}
						else {
							BeanFactory parent = getParentBeanFactory();
							if (parent instanceof ConfigurableBeanFactory) {
								pbd = ((ConfigurableBeanFactory) parent).getMergedBeanDefinition(parentBeanName);
							}
							else {
								throw new NoSuchBeanDefinitionException(parentBeanName,"Parent name '" + parentBeanName + "' is equal to bean name '" + beanName +"': cannot be resolved without an AbstractBeanFactory parent");
							}
						}
					}
					catch (NoSuchBeanDefinitionException ex) {
						throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
								"Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
					}
					//这里进行深拷贝，并将子类重写的方法和属性进行覆盖
					mbd = new RootBeanDefinition(pbd);
					mbd.overrideFrom(bd);
				}

				// 若前面没配置scope类型，这里设置为单例范围
				if (!StringUtils.hasLength(mbd.getScope())) {
					mbd.setScope(RootBeanDefinition.SCOPE_SINGLETON);
				}

				// 这里对内部类做了一些处理，若包含它的bean不是单例的，则该bean也将不会是单例的
				if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
					mbd.setScope(containingBd.getScope());
				}

				// 将生产的BeanDefinition 缓存起来
				if (containingBd == null && isCacheBeanMetadata()) {
					this.mergedBeanDefinitions.put(beanName, mbd);
				}
			}

			return mbd;
		}
	}
````
##### 赋值之前使用后置处理器: [spring getBean()方法](./Spring源码解析之createBean()方法.md)

                 
#### 第十二步:finishRefresh() => 完成BeanFactory的初始化创建工作；IOC容器就创建完成；[Tomcat真正启动](../springboot/SpringBoot源码解析之tomcat启动过程.md)
	
   - clearResourceCaches()
  
   - initLifecycleProcessor();//初始化和生命周期有关的后置处理器；LifecycleProcessor 默认从容器中找是否有lifecycleProcessor的组件【LifecycleProcessor】；如果没有new DefaultLifecycleProcessor();加入到容器；写一个LifecycleProcessor的实现类，可以在BeanFactory void onRefresh();void onClose();	
		
   - getLifecycleProcessor().onRefresh();//拿到前面定义的生命周期处理器（BeanFactory）；回调onRefresh()；
   
   - publishEvent(new ContextRefreshedEvent(this));//发布容器刷新完成事件；
		
   - liveBeansView.registerApplicationContext(this);
   
##### 子类: ServletWebServerApplicationContext

   - WebServer webServer = startWebServer();//子类启动tomcat容器，发布事件(Tomcat started on port(s): XX (http) with context path '/XX')
     		
#### root容器启动成功,监听到时间后创建 DispatcherServlet->FrameworkServlet(initServletBean())->HttpServletBean->HttpServlet
  
     