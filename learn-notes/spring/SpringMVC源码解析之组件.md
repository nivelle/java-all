
### initMultipartResolver


### initHandlerMappings 

```
private void initHandlerMappings(ApplicationContext context) {
        //初始化记录 HandlerMapping 对象的属性变量为null
		this.handlerMappings = null;
		//根据属性detectAllHandlerMappings决定是检测所有的 HandlerMapping 对象,还是使用指定名称的 HandlerMapping 对象
		//默认是true按照类型获取handlerMapping组件
		if (this.detectAllHandlerMappings) {
			// Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList<>(matchingBeans.values());
				// We keep HandlerMappings in sorted order.
				//排序，关于这里的排序，可以参考   WebMvcConfigurationSupport 类中对各种 HandlerMapping bean进行定义时所使用的 order 属性，顺序属性很关键，因为它涉及到 HandlerMapping 使用时的优先级
				AnnotationAwareOrderComparator.sort(this.handlerMappings);
			}
		}
		else { //获取名称为  handlerMapping 的 HandlerMapping bean 并记录到 handlerMappings
			try {
				HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
				this.handlerMappings = Collections.singletonList(hm);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerMapping later.
			}
		}

		// Ensure we have at least one HandlerMapping, by registering
		// a default HandlerMapping if no other mappings are found.
		// 如果上面步骤从容器获取 HandlerMapping 失败，则使用缺省策略创建 HandlerMapping 对象记录到handlerMappings
		if (this.handlerMappings == null) {
			this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerMappings declared for servlet '" + getServletName() +"': using default strategies from DispatcherServlet.properties");
			}
		}
	}

//该方法使用指定的策略接口 strategyInterface 创建一组策略对象。上面的方法initHandlerMappings就是使用该方法创建了一组缺省的HandlerMapping策略对象	
protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
        //策略接口长名称作为 key
		String key = strategyInterface.getName();
		//这里 defaultStrategies 是一个类静态属性，指向classpath resource 文件 DispatcherServlet.properties
		//该行获取策略接口对应的实现类,是','分割的实现类的长名称
		String value = defaultStrategies.getProperty(key);
		if (value != null) {
			String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
			List<T> strategies = new ArrayList<>(classNames.length);
			for (String className : classNames) {
				try {
				    // 获取策略接口实现类
					Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
					//创建该策略接口实现类的对象
					Object strategy = createDefaultStrategy(context, clazz);
					strategies.add((T) strategy);
				}
				catch (ClassNotFoundException ex) {
					throw new BeanInitializationException(
							"Could not find DispatcherServlet's default strategy class [" + className +
							"] for interface [" + key + "]", ex);
				}
				catch (LinkageError err) {
					throw new BeanInitializationException(
							"Unresolvable class definition for DispatcherServlet's default strategy class [" +
							className + "] for interface [" + key + "]", err);
				}
			}
			return strategies;
		}
		else {
			return new LinkedList<>();
		}
	}

```
### initHandlerAdapters

```
private void initHandlerAdapters(ApplicationContext context) {
        // 初始化记录 handlerAdapters 对象的属性变量为null 初始化记录 handlerAdap 初始化记录 handlerAdapters 对象的属性变量为null   ters 对象的属性变量为null
		this.handlerAdapters = null;
        //根据属性 detectAllHandlerAdapters 决定是检测所有的 HandlerAdapter 对象，还是使用指定名称的 HandlerAdapter 对象
		if (this.detectAllHandlerAdapters) {
			// Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
			//从容器及其祖先容器查找所有类型为 HandlerAdapter 的 HandlerAdapter 对象，记录到   handlerAdapters 并排序
			Map<String, HandlerAdapter> matchingBeans =BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerAdapters = new ArrayList<>(matchingBeans.values());
				// We keep HandlerAdapters in sorted order.
				// 排序
				AnnotationAwareOrderComparator.sort(this.handlerAdapters);
			}
		}
		else {
			try {
			    //获取名称为  handlerAdapter 的 HandlerAdapter bean 并记录到 handlerAdapters
				HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
				this.handlerAdapters = Collections.singletonList(ha);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerAdapter later.
			}
		}

		// Ensure we have at least some HandlerAdapters, by registering
		// default HandlerAdapters if no other adapters are found.
		// 如果上面步骤从容器获取 HandlerAdapter 失败，则使用缺省策略创建 HandlerAdapter 对象记录到handlerAdapters
		// 默认有三种: 1.HttpRequestHandlerAdapter; 2.SimpleControllerHandlerAdapter; 3.RequestMappingHandlerAdapter
		if (this.handlerAdapters == null) {
			this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerAdapters declared for servlet '" + getServletName() +"': using default strategies from DispatcherServlet.properties");
			}
		}
	}

```

### initHandlerExceptionResolvers
``` 
private void initHandlerExceptionResolvers(ApplicationContext context) {
        //初始化记录 handlerExceptionResolvers 对象的属性变量为null
		this.handlerExceptionResolvers = null;
        //根据属性 detectAllHandlerExceptionResolvers 决定是检测所有的 HandlerExceptionResolver 对象，还是使用指定名称的 HandlerExceptionResolver 对象
		if (this.detectAllHandlerExceptionResolvers) {
			// Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
				// We keep HandlerExceptionResolvers in sorted order.
				// 排序
				AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
			}
		}
		else {
			try {
			    //获取名称为  handlerExceptionResolver 的 HandlerExceptionResolver bean 并记录到 handlerExceptionResolvers
				HandlerExceptionResolver her =context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
				this.handlerExceptionResolvers = Collections.singletonList(her);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, no HandlerExceptionResolver is fine too.
			}
		}
		// 如果上面步骤从容器获取 HandlerExceptionResolver 失败，则使用缺省策略创建 HandlerExceptionResolver 对象记录到 handlerExceptionResolvers	
		// 默认有三种: 1.ExceptionHandlerExceptionResolver 2.ResponseStatusExceptionResolver 3.DefaultHandlerExceptionResolver
		if (this.handlerExceptionResolvers == null) {
			this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerExceptionResolvers declared in servlet '" + getServletName() +"': using default strategies from DispatcherServlet.properties");
			}
		}
	}


```


#### DispatcherServlet.properties

文件DispatcherServlet.properties是一个属性文件。每个属性的key是一个策略接口的长名称，而value是key指定的策略接口的多个实现类的长名称，每个类名称之间使用,分割。

```
org.springframework.web.servlet.LocaleResolver=
org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver

org.springframework.web.servlet.ThemeResolver=
org.springframework.web.servlet.theme.FixedThemeResolver

org.springframework.web.servlet.HandlerMapping=
org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping,
org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

org.springframework.web.servlet.HandlerAdapter=
org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter,
org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter,
org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

org.springframework.web.servlet.HandlerExceptionResolver=
org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver,
org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver,
org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver

org.springframework.web.servlet.RequestToViewNameTranslator=
org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator

org.springframework.web.servlet.ViewResolver=
org.springframework.web.servlet.view.InternalResourceViewResolver

org.springframework.web.servlet.FlashMapManager=
org.springframework.web.servlet.support.SessionFlashMapManager


```
