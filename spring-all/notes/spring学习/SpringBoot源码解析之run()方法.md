# SpringBoot 的run() 方法

## 第一步: 开启启动时间监控

```
//非线程安全
- StopWatch stopWatch = new StopWatch();

```

## 第二步: 开启headless配置模式

```
- configureHeadlessProperty();

```

## 第二步: 通过 SpringFactoryLoader 返回监听器

````
- SpringApplicationRunListeners listeners = getRunListeners(args);
  
  - new SpringApplicationRunListeners(logger, getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args))
    
    - SpringFactoriesLoader.loadFactoryNames(type, classLoader))
    
    - createSpringFactoriesInstances(type, parameterTypes,classLoader, args, names);
    
- listeners.starting();//springBoot开始启动监听器


````

## 第三步: 参数准备以及启动参数准备

```
- ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);

- ConfigurableEnvironment environment = prepareEnvironment(listeners,applicationArguments);
  
  - ConfigurableEnvironment environment = getOrCreateEnvironment();//根据容器类型创建环境对象,获取servle容器对象的参数
  
  - configureEnvironment(environment, applicationArguments.getSourceArgs());
  
    - environment.setConversionService((ConfigurableConversionService) conversionService);//参数转换器
    
    - configurePropertySources(environment, args);//
    
    - configureProfiles(environment, args);

  - listeners.environmentPrepared(environment);//环境准备好事件通知
  
  - bindToSpringApplication(environment);//将环境变量绑定到SpringApplication

```

## 第四步: 忽略的配置

````
- configureIgnoreBeanInfo(environment);
  
````

## 第五步: 打印启动标识

```
- Banner printedBanner = printBanner(environment);

```

## 第六步: 创建容器
			
```
- context = createApplicationContext(); //根据容器类型创建容器（org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext；
org.springframework.context.annotation.AnnotationConfigApplicationContext）

```

## 第七步:创建异常处理器

```
- exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,new Class[] { ConfigurableApplicationContext.class }, context);

```

## 第八步: 容器准备

```
- prepareContext(context, environment, listeners, applicationArguments,printedBanner);
  
  - postProcessApplicationContext(context);//将 beanNameGenerator;resourceLoader;ConversionService
  
  - applyInitializers(context);//执行所有实现了ApplicationContextInitializer接口的方法
  
  - listeners.contextPrepared(context);//监听器
  
  - load(context, sources.toArray(new Object[0]));//将编译后的bean信息载入SpringApplication容器当中，注册到BeanDefinitionRegistry中

  - listeners.contextLoaded(context);//监听器


```

## 第九步: 容器刷新

```
- refreshContext(context);

  - refresh(context);//Spring源码分析值refresh()方法

```

## 第十步: 刷新之后

```
- afterRefresh(context, applicationArguments);

```


## 第十一步: 停止启动时间监控

```
- stopWatch.stop();

```


## 第十二步: 容器启动成功监听器

```
- listeners.started(context);

```

## 第十三步: 启动后回调函数

```
- callRunners(context, applicationArguments);

```


## 第十四步: 容器运行中监听器

```
- listeners.running(context);

```

