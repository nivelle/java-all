### SpringBoot 的run() 方法

#### 第一步: 开启启动时间监控

//非线程安全

- StopWatch stopWatch = new StopWatch();

#### 第二步: 开启headless配置模式

- configureHeadlessProperty();//设置该应用程序,即使没有检测到显示器,也允许其启动,默认为true

#### 第二步: 通过 SpringFactoriesLoader 返回监听器

- SpringApplicationRunListeners listeners = getRunListeners(args);
    
    //构造监听器实例集合
  - new SpringApplicationRunListeners(logger, getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args))
    
    ##### getSpringFactoriesInstances //创建 META-INF/spring.factories 配置文件指定listener

    - SpringFactoriesLoader.loadFactoryNames(type, classLoader))-> Set<String> names
    
    - createSpringFactoriesInstances(type, parameterTypes,classLoader, args, names);
    
- **listeners.starting();//触发容器开始启动事件通知**

#### springBoot 启动事件顺序: springBoot开始启动监听器,监听器集合(starting,environmentPrepared,contextPrepared,contextLoaded,started,running or failed)如果启动失败,则回调失败处理器:callFailedListener

#### 第三步: 参数准备以及启动参数准备

- ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);//启动参数构建

- ConfigurableEnvironment environment = prepareEnvironment(listeners,applicationArguments);
  
  ##### prepareEnvironment //参数:监听器集合,启动时的参数,构造一个ConfigurableEnvironment(Environment)
  
  - ConfigurableEnvironment environment = getOrCreateEnvironment();//根据容器类型创建环境对象(StandardServletEnvironment,StandardReactiveWebEnvironment,StandardEnvironment)
  
  - configureEnvironment(environment, applicationArguments.getSourceArgs());
  
    - environment.setConversionService((ConfigurableConversionService) conversionService);//设置参数转换器
    
    - configurePropertySources(environment, args);//将启动时的参数转换成复合参数添加到Environment中,注意
    
    - configureProfiles(environment, args);//获取激活的Profiles数组

  - **listeners.environmentPrepared(environment);//触发环境准备好事件通知**
  
  - bindToSpringApplication(environment);//将environment环境变量绑定到SpringApplication,key= spring.main
    
    - Binder.get(environment).bind("spring.main", Bindable.ofInstance(this));
    
  - ConfigurationPropertySources.attach(environment);
     
    ````
    1. Adapts each {@link PropertySource} managed by the environment to a {@link ConfigurationPropertySource} and allows classic {@link PropertySourcesPropertyResolver} calls to resolve using
    
    2. 删除非本次创建的 environment(configurationProperties)
    
    3. 本次创建的 environment addFirst ,优先级最高
    ````
    
#### 第四步: 忽略的配置

- configureIgnoreBeanInfo(environment);//配置spring.beaninfo.ignore，并添加到名叫systemProperties的PropertySource中；默认为true即开启
  
#### 第五步: 打印启动标识

- Banner printedBanner = printBanner(environment);

#### 第六步: 创建容器
			
- context = createApplicationContext(); 
  
  - contextClass = Class.forName(DEFAULT_SERVLET_WEB_CONTEXT_CLASS);
  
  ````
  1. 通过Class.forName()获取类实例;
  
  2. 根据容器类型创建容器（AnnotationConfigServletWebServerApplicationContext;默认:AnnotationConfigApplicationContext）
  ````
  - (ConfigurableApplicationContext) BeanUtils.instantiateClass(contextClass);
  
    - (KotlinDetector.isKotlinReflectPresent() && KotlinDetector.isKotlinType(ctor.getDeclaringClass()) ?KotlinDelegate.instantiateClass(ctor, args) : ctor.newInstance(args));//创建实例

#### 第七步:创建异常处理器

  //META-INF/spring.factories 配置文件
- exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,new Class[] { ConfigurableApplicationContext.class }, context);

#### 第八步: 容器准备

- prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner);

  - context.setEnvironment(environment);
    
  - postProcessApplicationContext(context);//Apply any relevant post processing the {@link ApplicationContext}，applicationContext的后置处理器
  
    ````
    将 internalConfigurationBeanNameGenerator;resourceLoader;ConversionService设置到当前上下文context
      
    ````
  - applyInitializers(context);//Apply any {@link ApplicationContextInitializer}s to the context before it is refreshed，applicationContext的init方法
  
    ````
    refresh()方法执行之前，执行所有实现了 ApplicationContextInitializer<C extends ConfigurableApplicationContext> 接口的方法
    
    ````
    - getInitializers();//获取实现了ApplicationContextInitializer接口的实现类
    
    - initializer.initialize(context);//分别执行initialize方法
  
  - **listeners.contextPrepared(context);//触发 容器准备事件**
  
  - logStartupInfo(context.getParent() == null);//打印启动日志
  
  - logStartupProfileInfo();
    `````
    //打印 active profile 信息。 例如:No active profile set, falling back to default profiles: 
    或者 The following profiles are active:
    `````
  - ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();//**最底层的容器:获取底层的beanFactory**
  
  - beanFactory.registerSingleton("springApplicationArguments", applicationArguments);//注册单实例:springApplicationArguments

  - beanFactory.registerSingleton("springBootBanner", printedBanner);//注册单实例:springBootBanner

  - beanFactory instanceof DefaultListableBeanFactory? true ;//beanFactory.setAllowBeanDefinitionOverriding()设置beanDefinition是否可以重写
  
  - context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());//如果设置 lazyInitialization 为true 则bean为懒汉模式，默认为饥汉模式，通过beanFactoryPostProcessor实现
  
  - getAllSources(); 
     ````
    //获取主源类，也就XXXApplicationContext类 primarySources 和 sources; return Collections.unmodifiableSet(allSources),不可修改的集合
     ````
  
  -load(ApplicationContext context, Object[] sources) ;
  
    ````
    //将编译后的bean信息载入SpringApplication容器当中,注册到BeanDefinitionRegistry中,包括spring核心基础工具类
    ````
    - BeanDefinitionLoader loader = createBeanDefinitionLoader(beanDefinitionRegistry, sources);
    
      - new BeanDefinitionLoader(registry, sources);//参数:当前上下文和需要加载的资源;获取类定义加载工具
      
        ````
      
        BeanDefinitionLoader(BeanDefinitionRegistry registry, Object... sources) {
      		Assert.notNull(registry, "Registry must not be null");
      		Assert.notEmpty(sources, "Sources must not be empty");
      		this.sources = sources;
            //注解类型的解析器,由AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry)加载了默认的beanPostProcess
      		this.annotatedReader = new AnnotatedBeanDefinitionReader(registry);
            //xml类型的解析器
      		this.xmlReader = new XmlBeanDefinitionReader(registry);
      		if (isGroovyPresent()) {
      			this.groovyReader = new GroovyBeanDefinitionReader(registry);
      		}
            //环境变量处的解析器
      		this.scanner = new ClassPathBeanDefinitionScanner(registry);
            //排除过滤器
      		this.scanner.addExcludeFilter(new ClassExcludeFilter(sources));
      	 }
      	
        ````
          // 以注解类型解析为例
        - new AnnotatedBeanDefinitionReader(registry);
        
          - this(registry, getOrCreateEnvironment(registry));
          
            - AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
        
              - registerAnnotationConfigProcessors(BeanDefinitionRegistry registry);
          
                - DefaultListableBeanFactory beanFactory = unwrapDefaultListableBeanFactory(registry);//获取原始beanFactory
            
                - beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);//设置默认的排序器
                  //解析@Lazy、@Qualifier注解的原理
                - beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());// providing support for qualifier annotations as well as for lazy resolution
            
                - beanDefs.add();
                  
                  ````
                  换包括一些其他的基础 的 beanPostProcess 定义:
                  
                  1. internalConfigurationAnnotationProcessor(ConfigurationClassPostProcessor);//解析 @Configuration
                  
                  2. internalAutowiredAnnotationProcessor(AutowiredAnnotationBeanPostProcessor);//解析 @Autowired
                  
                  3. internalCommonAnnotationProcessor(CommonAnnotationBeanPostProcessor);//JSR-250 @Resource
                
                  4. internalEventListenerProcessor;
                  
                  5. internalEventListenerFactory
                  
                  6. PersistenceAnnotationBeanPostProcessor //jpa
                  
                  ````

    - loader.setBeanNameGenerator(this.beanNameGenerator);
    
    - loader.setResourceLoader(this.resourceLoader);
    
    - loader.setEnvironment(this.environment);
    
    - loader.load();
    
    ````
     1. 加载 Bean 定义, 只根据 源类型将源设置到具体的 Bean定义加载器，并没有全面执行Bean定义加载.
     
     2. 这里 sources 指定的类，如果有注解@Component，则会被作为Bean定义添加到容器中，比如当前SpringApplication的入口类，因为有注解@SpringBootConfiguration，内含了注解@Component,所以它会作为作为一个Bean被注册进容器;
    ````
      
    ````
      private int load(Object source) {
      		Assert.notNull(source, "Source must not be null");
      		if (source instanceof Class<?>) {
      			return load((Class<?>) source);
      		}
      		if (source instanceof Resource) {//xmlReader 配置文件格式
      			return load((Resource) source);
      		}
      		if (source instanceof Package) {//doScan()扫描注解类
      			return load((Package) source);
      		}
      		if (source instanceof CharSequence) {
      			return load((CharSequence) source);
      		}
      		throw new IllegalArgumentException("Invalid source type " + source.getClass());
      	}
      	
     ````

  - listeners.contextLoaded(context);//**触发容器加载完成事件**


#### 第九步: 容器刷新


- refreshContext(context);//((AbstractApplicationContext) applicationContext).refresh()

  - refresh(context);//子类 Spring源码分析值refresh()方法; [Spring refresh()方法](../springcore/Spring源码解析之refresh()方法.md)


#### 第十步: 刷新之后

- afterRefresh(context, applicationArguments); //springBoot空实现，容器刷新之后做一些操作

#### 第十一步: 停止启动时间监控

- stopWatch.stop();

#### 第十二步: 容器启动成功监听器

- listeners.started(context);

#### 第十三步: 启动后回调函数，实现ApplicationRunner 或者 CommandLineRunner 接口的类运行，在容器启动完成后做一些操作

- callRunners(context, applicationArguments);

#### 第十四步: 容器运行中监听器

- listeners.running(context);


