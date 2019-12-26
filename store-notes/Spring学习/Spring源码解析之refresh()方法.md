# Spring容器的refresh() 方法，synchronized (this.startupShutdownMonitor)实现同步

## 第一步:prepareRefresh():刷新前的预处理：
   
   (1) initPropertySources():初始化一些属性设置,默认不做任何处理,留给子类自定义属性设置方法;
   
   (2) getEnvironment().validateRequiredProperties():校验非空属性是否设置了值，没有设置的话抛出异常(MissingRequiredPropertiesException);
       - systemEnvironment
       - systemProperties
   (3) earlyApplicationEvents= new LinkedHashSet<ApplicationEvent>():保存容器中的一些早期的事件,先清理掉旧的监听器
  
## 第二步:ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory():获取beanFactory

   (1) refreshBeanFactory(): 刷新(CAS刷新容器状态)并创建，this.beanFactory = new DefaultListableBeanFactory()并设置id；
   
   (2) getBeanFactory(): 返回刚才GenericApplicationContext创建的BeanFactory对象 DefaultListableBeanFactory；
	
## 第三步:prepareBeanFactory(beanFactory):BeanFactory的预准备工作（BeanFactory进行一些设置;
	  
   (1) 设置BeanFactory的类加载器(setBeanClassLoader)、设置表达式解析器(setBeanExpressionResolver)、添加属性解析器(addPropertyEditorRegistrar)
	
   (2) 添加部分BeanPostProcessor【ApplicationContextAwareProcessor,设置EmbeddedValueResolver值解析器;	设置忽略的自动装配的接口EnvironmentAware;
	
   (3) 注册可以解析的自动装配;我们能直接在任何组件中自动注入:BeanFactory、ResourceLoader、ApplicationEventPublisher、ApplicationContext
	
   (4) 添加BeanPostProcessor【ApplicationListenerDetector】
	
   (5) 添加编译时的AspectJ；
	
   (6) 给BeanFactory中注册一些能用的组件: environment【ConfigurableEnvironment、systemProperties【Map<String, Object>、systemEnvironment【Map<String, Object>】

## 第四步:postProcessBeanFactory(beanFactory);
     
   - 子类通过重写这个方法来在BeanFactory创建并预准备完成以后做进一步的设置;
   
   ```
   Modify the application context's internal bean factory after its standard initialization. All bean definitions will have been loaded, but no beans will have been instantiated yet. This allows for registering special
   
   ```

## 第五步:invokeBeanFactoryPostProcessors(beanFactory):
	
   ### 两个接口:接口BeanDefinitionRegistryPostProcessor 继承自 BeanFactoryPostProcessor
	
   - 如果beanFactory是BeanDefinitionRegistry先执行 BeanDefinitionRegistryPostProcessor的postProcessBeanDefinitionRegistry方法:
	    
     (1) 获取所有的BeanDefinitionRegistryPostProcessor；
		
     (2) 看先执行实现了PriorityOrdered优先级接口的BeanDefinitionRegistryPostProcessor,执行 invokeBeanDefinitionRegistryPostProcessors=>postProcessBeanDefinitionRegistry
			
     (3) 在执行实现了Ordered顺序接口的BeanDefinitionRegistryPostProcessor,执行 invokeBeanDefinitionRegistryPostProcessors=>postProcessBeanDefinitionRegistry
			
     (4) 最后执行没有实现任何优先级或者是顺序接口的BeanDefinitionRegistryPostProcessors,执行 invokeBeanDefinitionRegistryPostProcessors=>postProcessBeanDefinitionRegistry
	
   - 否则直接执行 invokeBeanFactoryPostProcessors(postProcessors,beanFactory)=>postProcessBeanFactory
		
   - 最后执行所有的BeanFactoryPostProcessor的方法:

     (1) 获取所有的BeanFactoryPostProcessor
		
     (2) 看先执行实现了PriorityOrdered优先级接口的BeanFactoryPostProcessor,执行postProcessor.postProcessBeanFactory(beanFactory)
			
     (3) 在执行实现了Ordered顺序接口的BeanFactoryPostProcessor,执行postProcessor.postProcessBeanFactory(beanFactory)
			
     (4) 最后执行没有实现任何优先级或者是顺序接口的BeanFactoryPostProcessor,执行postProcessor.postProcessBeanFactory(beanFactory)
			
## 第六步:registerBeanPostProcessors,注册BeanPostProcessor(注册到beanFactory)
		
   ### BeanPostProcessor类型:DestructionAwareBeanPostProcessor、InstantiationAwareBeanPostProcessor、SmartInstantiationAwareBeanPostProcessor、MergedBeanDefinitionPostProcessor
		
   (1) 获取所有的 BeanPostProcessor;后置处理器都默认可以通过PriorityOrdered、Ordered接口来执行优先级
		
   (2) 先注册PriorityOrdered级别的BeanPostProcessor=>registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);
   
   (3) 再注册Ordered接口级别的BeanPostProcessor=>registerBeanPostProcessors(beanFactory, orderedPostProcessors);
   
   (4) 最后注册没有实现任何优先级接口的 BeanPostProcessor=>registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);
		
   (5) 最终注册实现MergedBeanDefinitionPostProcessor接口的BeanPostProcessor =>registerBeanPostProcessors(beanFactory, internalPostProcessors);

   (6) 注册一个ApplicationListenerDetector在beanPostProcessor chain 尾部
   
## 第七步:initMessageSource();在SpringMVC中做初始化MessageSource组件（做国际化功能,消息绑定，消息解析):

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
## 第八步:initApplicationEventMulticaster();初始化事件派发器；
		
   (1) 获取BeanFactory=>	ConfigurableListableBeanFactory beanFactory = getBeanFactory();

   (2) 从BeanFactory中获取name = "applicationEventMulticaster"的applicationEventMulticaster;
   
   (3) 如果上一步没有配置则创建一个SimpleApplicationEventMulticaster;
   
   (4) 将创建的ApplicationEventMulticaster添加到BeanFactory中，以后其他组件直接自动注入=>	beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
   		
## 第九步:onRefresh();留给子容器（子类）
	
	// Called on initialization of special beans, before instantiation of singletons.
   - 子类重写这个方法,默认不做任何操作在容器刷新的时候可以自定义逻辑;	

## 第十步:registerListeners();
	
  （1）从容器中获取静态的ApplicationListener; 然后直接注入=>getApplicationEventMulticaster().addApplicationListener(listener);

  （2）将非静态监听器名字添加到事件派发器中; getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
 
  （3）派发器派发一些早起的事件;=>getApplicationEventMulticaster().multicastEvent(earlyEvent);		

## 第十一步:finishBeanFactoryInitialization(beanFactory);//初始化所有剩下的单实例bean；

   - 设置conversionService方法=>beanFactory.setConversionService(beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));

   - 注册一个字符解析器=>beanFactory.addEmbeddedValueResolver(strVal => getEnvironment().resolvePlaceholders(strVal))

   - 注册一个字符解析器=>beanFactory.addEmbeddedValueResolver(strVal => getEnvironment().resolvePlaceholders(strVal));//和AOP相关

   - beanFactory.freezeConfiguration();//冻结bean定义,保证缓存有效

   - beanFactory.preInstantiateSingletons();初始化后剩下的单实例非懒加载的bean

  （1）获取容器中的所有的 beanDefinitionNames

  （2）获取Bean的定义信息,RootBeanDefinition = getMergedLocalBeanDefinition(beanName);Bean不是抽象的,是单实例的,不是懒加载;
      
   #### 判断是否是FactoryBean => isFactoryBean(beanName)
            
   ##### 是工厂bean
            
    //是 A standard FactoryBean is not expected to initialize eagerly,工厂方法获取bean
   - Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
                
   - getBean(beanName) => doGetBean(name, requiredType, args, typeCheckOnly)
               
   - 先获取缓存中保存的单实例Bean。如果能获取到说明这个Bean之前被创建过（所有创建过的单实例Bean都会被缓存起来）=> Object singletonObject = this.singletonObjects.get(beanName);
	 private final Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>(256);

   - 缓存中获取不到，开始Bean的创建对象流程,标记当前bean已经被创建（防止多线程重复创建）=> markBeanAsCreated(beanName);

   - 获取Bean的定义信息 => getMergedLocalBeanDefinition(beanName)

   - 获取当前Bean依赖的其他Bean;如果有按照getBean()把依赖的Bean先创建出来=>String[] dependsOn = mbd.getDependsOn();

   - 启动单实例Bean的创建流程=>createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args);
                    
    // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
   - Object bean = resolveBeforeInstantiation(beanName, mbdToUse);让BeanPostProcessor 返回代理对象;

   - InstantiationAwareBeanPostProcessor先触发 postProcessBeforeInstantiation()=>如果有返回值触发postProcessAfterInitialization();

	 - 如果上面的 InstantiationAwareBeanPostProcessor 没有返回代理对象则继续执行下面的 doCreateBean(beanName, mbdToUse, args);真正创建Bean

	 - BeanWrapper instanceWrapper = createBeanInstance(beanName, mbd, args);利用工厂方法或者对象的构造器创建出Bean实例；

	 - applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName); 调用 MergedBeanDefinitionPostProcessor的postProcessMergedBeanDefinition(mbd, beanType, beanName);

	 - Bean属性赋值,populateBean(beanName, mbd, instanceWrapper);

   ###### 赋值之前使用后置处理器:

   (1)、拿到InstantiationAwareBeanPostProcessor后置处理器,postProcessAfterInstantiation()；

   (2)、拿到InstantiationAwareBeanPostProcessor后置处理器,postProcessPropertyValues()；

   (3)、应用Bean属性的值；为属性利用setter方法等进行赋值=> applyPropertyValues(beanName, mbd, bw, pvs);

   - Bean初始化=>initializeBean(beanName, exposedObject, mbd);

   (1)、执行Aware接口方法=>invokeAwareMethods(beanName, bean);执行xxxAware接口的方法BeanNameAware、BeanClassLoaderAware、BeanFactoryAware

   (2)、执行后置处理器初始化之前=>applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName)=>BeanPostProcessor.postProcessBeforeInitialization();

   (3)、执行初始化方法=>invokeInitMethods(beanName, wrappedBean, mbd):
   
   - 是否是InitializingBean接口的实现；执行接口规定的初始化；
   
   - 是否自定义初始化方法；

   (4)、执行后置处理器初始化之后=>pplyBeanPostProcessorsAfterInitialization=>BeanPostProcessor.postProcessAfterInitialization();

   - 注册Bean的销毁方法=>registerDisposableBeanIfNecessary

   - 将创建的Bean添加到缓存中singletonObjects;
					
   ##### 不是工厂Bean,利用getBean(beanName)创建对象
            
   ##### 遍历所有的bean实现了 SmartInitializingSingleton接口的执行=>smartSingleton.afterSingletonsInstantiated()
                 
## 第十二步:finishRefresh();完成BeanFactory的初始化创建工作；IOC容器就创建完成；
	
   - clearResourceCaches()
  
   - initLifecycleProcessor();初始化和生命周期有关的后置处理器；LifecycleProcessor 默认从容器中找是否有lifecycleProcessor的组件【LifecycleProcessor】；如果没有new DefaultLifecycleProcessor();加入到容器；写一个LifecycleProcessor的实现类，可以在BeanFactory void onRefresh();void onClose();	
		
   - getLifecycleProcessor().onRefresh();拿到前面定义的生命周期处理器（BeanFactory）；回调onRefresh()；
   
   - publishEvent(new ContextRefreshedEvent(this));发布容器刷新完成事件；
		
   - liveBeansView.registerApplicationContext(this);
   
[来源备注:watermelon1015](https://gitee.com/watermelon1015/spring_source_parsing_data)
   