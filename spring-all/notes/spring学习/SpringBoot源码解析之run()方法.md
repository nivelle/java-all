# SpringBoot 的run() 方法

## 第一步: 开启启动时间监控

//非线程安全

- StopWatch stopWatch = new StopWatch();

## 第二步: 开启headless配置模式

- configureHeadlessProperty();

## 第二步: 通过 SpringFactoriesLoader 返回监听器

- SpringApplicationRunListeners listeners = getRunListeners(args);
    
    //构造监听器实例集合
  - new SpringApplicationRunListeners(logger, getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args))
    
    - SpringFactoriesLoader.loadFactoryNames(type, classLoader))-> Set<String> names
    
    - createSpringFactoriesInstances(type, parameterTypes,classLoader, args, names);
    
- listeners.starting();//springBoot开始启动监听器,监听器集合(starting,environmentPrepared,contextPrepared,contextLoaded,started,running or failed)如果启动失败,则回调失败处理器:callFailedListener

## 第三步: 参数准备以及启动参数准备

- ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);//启动参数构建

- ConfigurableEnvironment environment = prepareEnvironment(listeners,applicationArguments);//参数:监听器集合,启动时的参数,构造一个ConfigurableEnvironment(Environment)
  
  - ConfigurableEnvironment environment = getOrCreateEnvironment();//根据容器类型创建环境对象(StandardServletEnvironment,StandardReactiveWebEnvironment,StandardEnvironment)
  
  - configureEnvironment(environment, applicationArguments.getSourceArgs());
  
    - environment.setConversionService((ConfigurableConversionService) conversionService);//设置参数转换器
    
    - configurePropertySources(environment, args);//将启动时的参数转换成复合参数添加到Environment中
    
    - configureProfiles(environment, args);//获取激活的Profiles数组

  - listeners.environmentPrepared(environment);//环境准备好事件通知
  
  - bindToSpringApplication(environment);//将环境变量绑定到SpringApplication
    
    - Binder.get(environment).bind("spring.main", Bindable.ofInstance(this));
    
  - ConfigurationPropertySources.attach(environment);//Adapts each {@link PropertySource} managed by the environment to a {@link ConfigurationPropertySource} and allows classic {@link PropertySourcesPropertyResolver} calls to resolve using

## 第四步: 忽略的配置

- configureIgnoreBeanInfo(environment);
  
## 第五步: 打印启动标识

- Banner printedBanner = printBanner(environment);

## 第六步: 创建容器
			
- context = createApplicationContext(); 

  - contextClass = Class.forName(DEFAULT_SERVLET_WEB_CONTEXT_CLASS);//通过Class.forName()获取类实例；根据容器类型创建容器（AnnotationConfigServletWebServerApplicationContext;AnnotationConfigApplicationContext）

  - (ConfigurableApplicationContext) BeanUtils.instantiateClass(contextClass);
  
    - (KotlinDetector.isKotlinReflectPresent() && KotlinDetector.isKotlinType(ctor.getDeclaringClass()) ?KotlinDelegate.instantiateClass(ctor, args) : ctor.newInstance(args));//创建实例

## 第七步:创建异常处理器

- exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,new Class[] { ConfigurableApplicationContext.class }, context);

## 第八步: 容器准备

- prepareContext(context, environment, listeners, applicationArguments,printedBanner);
  
  - postProcessApplicationContext(context);//将 internalConfigurationBeanNameGenerator;resourceLoader;ConversionService设置到当前上下文context
  
  - applyInitializers(context);//执行所有实现了 ApplicationContextInitializer<C extends ConfigurableApplicationContext> 接口的方法
    
    - getInitializers();//获取实现了ApplicationContextInitializer接口的实现类
    
    - initializer.initialize(context);//分别执行initialize方法
  
  - listeners.contextPrepared(context);//监听器容器准备事件触发
  
  - logStartupInfo(context.getParent() == null);//打印启动日志
  
  - logStartupProfileInfo();//打印 active profile 信息。 例如:No active profile set, falling back to default profiles:### 或则 The following profiles are active:

  - ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();//获取底层的beanFactory
  
  - beanFactory.registerSingleton("springApplicationArguments", applicationArguments);//注册单实例:springApplicationArguments

  - beanFactory.registerSingleton("springBootBanner", printedBanner);//注册单实例:springBootBanner

  - beanFactory instanceof DefaultListableBeanFactory? true ;//beanFactory.setAllowBeanDefinitionOverriding()设置是否可以重写
  
  - getAllSources(); //获取 primarySources 和 sources; return Collections.unmodifiableSet(allSources),不可修改的集合
  
  - load(context, sources.toArray(new Object[0]));//将编译后的bean信息载入SpringApplication容器当中，注册到BeanDefinitionRegistry中
  
    - BeanDefinitionLoader loader = createBeanDefinitionLoader(beanDefinitionRegistry, sources);
    
      - new BeanDefinitionLoader(registry, sources);//参数:当前上下文和需要加载的资源;
      
      ```
      
      BeanDefinitionLoader(BeanDefinitionRegistry registry, Object... sources) {
      		Assert.notNull(registry, "Registry must not be null");
      		Assert.notEmpty(sources, "Sources must not be empty");
      		this.sources = sources;
      		this.annotatedReader = new AnnotatedBeanDefinitionReader(registry);
      		//注解类型的解析器,由AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry)加载了默认的beanPostProcess
      		this.xmlReader = new XmlBeanDefinitionReader(registry);//xml类型的解析器
      		if (isGroovyPresent()) {
      			this.groovyReader = new GroovyBeanDefinitionReader(registry);
      		}
      		this.scanner = new ClassPathBeanDefinitionScanner(registry);//环境变量处的解析器
      		this.scanner.addExcludeFilter(new ClassExcludeFilter(sources));//排除过滤器
      	}
      	
      ```
        
        - new AnnotatedBeanDefinitionReader(registry);
        
          - this(registry, getOrCreateEnvironment(registry));
          
            - AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
        
              - registerAnnotationConfigProcessors(BeanDefinitionRegistry registry);
          
                - DefaultListableBeanFactory beanFactory = unwrapDefaultListableBeanFactory(registry);//获取原始beanFactory
            
                - beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);//设置默认的排序器
            
                - beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());// providing support for qualifier annotations as well as for lazy resolution
            
                - beanDefs.add();//设置一些基础的 beanPostProcess例如: internalConfigurationAnnotationProcessor(ConfigurationClassPostProcessor);internalAutowiredAnnotationProcessor;internalCommonAnnotationProcessor
                
                  ;internalEventListenerProcessor;internalEventListenerFactory

    - loader.setBeanNameGenerator(this.beanNameGenerator);
    
    - loader.setResourceLoader(this.resourceLoader);
    
    - loader.setEnvironment(this.environment);
    
    - loader.load();//加载 Bean 定义, 只根据源类型将源设置到具体的 Bean定义加载器，并没有全面执行Bean定义加载.这里 sources 指定的类，如果有注解@Component，则会被作为Bean定义添加到容器中，比如当前SpringApplication的入口类，因为有注解@SpringBootConfiguration，内含了注解@Component,所以它会作为作为一个Bean被注册进容器;
    
      ```
      private int load(Object source) {
      		Assert.notNull(source, "Source must not be null");
      		if (source instanceof Class<?>) {
      			return load((Class<?>) source);
      		}
      		if (source instanceof Resource) {
      			return load((Resource) source);
      		}
      		if (source instanceof Package) {
      			return load((Package) source);
      		}
      		if (source instanceof CharSequence) {
      			return load((CharSequence) source);
      		}
      		throw new IllegalArgumentException("Invalid source type " + source.getClass());
      	}
      	
      ```

  - listeners.contextLoaded(context);//监听器容器加载完毕事件触发


## 第九步: 容器刷新


- refreshContext(context);//((AbstractApplicationContext) applicationContext).refresh()

  - refresh(context);//子类 Spring源码分析值refresh()方法; [Spring refresh()方法](./Spring源码解析之refresh()方法.md)


## 第十步: 刷新之后

- afterRefresh(context, applicationArguments);

## 第十一步: 停止启动时间监控

- stopWatch.stop();

## 第十二步: 容器启动成功监听器

- listeners.started(context);

## 第十三步: 启动后回调函数

- callRunners(context, applicationArguments);

## 第十四步: 容器运行中监听器

- listeners.running(context);


