
#### public class DispatcherServlet extends FrameworkServlet

##### public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware

- initServletBean()

```
protected final void initServletBean() throws ServletException {
		getServletContext().log("Initializing Spring " + getClass().getSimpleName() + " '" + getServletName() + "'");
		if (logger.isInfoEnabled()) {
			logger.info("Initializing Servlet '" + getServletName() + "'");
		}
		long startTime = System.currentTimeMillis();

		try {
		    // 初始化WebApplicationContext属性 WebApplicationContext 是继承自ApplicationContext 接口的接口。 该属性也就是Spring容器上下文。 FrameworkServlet的作用就是将Servlet与Spring容器关联
			this.webApplicationContext = initWebApplicationContext();
			// 该方法主要是为了让子类覆写该方法并坐一些需要的处理，不过DispatcherServlet并未覆写该方法。
			initFrameworkServlet();
		}
		catch (ServletException | RuntimeException ex) {
			logger.error("Context initialization failed", ex);
			throw ex;
		}

		if (logger.isDebugEnabled()) {
			String value = this.enableLoggingRequestDetails ?
					"shown which may lead to unsafe logging of potentially sensitive data" :
					"masked to prevent unsafe logging of potentially sensitive data";
			logger.debug("enableLoggingRequestDetails='" + this.enableLoggingRequestDetails +
					"': request parameters and headers will be " + value);
		}

		if (logger.isInfoEnabled()) {
			logger.info("Completed initialization in " + (System.currentTimeMillis() - startTime) + " ms");
		}
	}

```

- initWebApplicationContext()

```
protected WebApplicationContext initWebApplicationContext() {
       // 得到跟上下文
		WebApplicationContext rootContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		WebApplicationContext wac = null;
		if (this.webApplicationContext != null) {
			// A context instance was injected at construction time -> use it
			wac = this.webApplicationContext;
			//以WebApplicationContext 为参数的构造函数，同样设置webApplicationContext 的 父容器为 根上下文
			if (wac instanceof ConfigurableWebApplicationContext) {
				ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) wac;
				if (!cwac.isActive()) {
					// The context has not yet been refreshed -> provide services such as
					// setting the parent context, setting the application context id, etc
					if (cwac.getParent() == null) {
						// The context instance was injected without an explicit parent -> set
						// the root application context (if any; may be null) as the parent
						cwac.setParent(rootContext);
					}
					configureAndRefreshWebApplicationContext(cwac);
				}
			}
		}
		if (wac == null) {
			// 以contextAttribute属性(FrameworkServlet的String类型属性)为key从ServletContext中找WebApplicationContext。
			// 一般不会设置contextAttribute属性，也就是说这里找到的wac为null
			wac = findWebApplicationContext();
		}
		if (wac == null) {
			// 创建WebApplicationContext并设置根上下文为父上下文，然后配置ServletConfig，ServletContext等实例到这个上下文中
			wac = createWebApplicationContext(rootContext);
		}

		if (!this.refreshEventReceived) {
			// webApplicationContext创建成功之后会进行调用，FrameworkServlet空实现子. 子类Dispatcher会覆写这个方法
			synchronized (this.onRefreshMonitor) {
				onRefresh(wac);
			}
		}

		if (this.publishContext) {
			// Publish the context as a servlet context attribute.
			String attrName = getServletContextAttributeName();
			//将新创建的容器上下文设置到ServletContext中
			getServletContext().setAttribute(attrName, wac);
		}

		return wac;
	}

```

##### 根上下文是web.xml中配置的ContextLoaderListener监听器中根据contextConfigLocation路径生成的上下文
```
<context-param>
  <param-name>contextConfigLocation</param-name>  
  <param-value>classpath:springConfig/applicationContext.xml</param-value>  
</context-param>
<listener>
  <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>  
</listener>
```

##### public abstract class HttpServletBean extends HttpServlet implements EnvironmentCapable, EnvironmentAware

```
public final void init() throws ServletException {

		// Set bean properties from init parameters.
		// 构造过程中会使用ServletConfig对象找出web.xml配置文件中的配置参数并设置到 ServletConfigPropertyValues 内部
		PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
		if (!pvs.isEmpty()) {
			try {
			    // 使用BeanWrapper构造DidpatcherServlet
				BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
				ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
				//Resource类型参数使用属性编辑器
				bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
				initBeanWrapper(bw);
				bw.setPropertyValues(pvs, true);
			}
			catch (BeansException ex) {
				if (logger.isErrorEnabled()) {
					logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
				}
				throw ex;
			}
		}

		// Let subclasses do whatever initialization they like.
		initServletBean();
	}

```

##### public abstract class HttpServlet extends GenericServlet

##### public abstract class GenericServlet implements Servlet, ServletConfig,java.io.Serializable 


- protected void onRefresh(ApplicationContext context)//DispatcherServlet覆写了FrameworkServlet中的onRefresh方法

```
protected void onRefresh(ApplicationContext context) {
		initStrategies(context);
	}

```

-  initStrategies(ApplicationContext context)//springBoot context是 AnnotationConfigServletWebServerApplicationContext
```
protected void initStrategies(ApplicationContext context) {
        // MultipartResolver 文件上传相关:从bean容器中中获取名字为 MULTIPART_RESOLVER_BEAN_NAME(multipartResolver)的bean，记录到属性multipartResolver，没有响应bean的话设置为null
		initMultipartResolver(context);
		//LocaleResolver, 本地化语言相关:从bean容器中获取名字为LOCALE_RESOLVER_BEAN_NAME(localeResolver)的bean记录到属性localeResolver,没有相应bean的话使用缺省策略
		initLocaleResolver(context);
		//ThemeResolver 相关: 从bean容器中获取名字为THEME_RESOLVER_BEAN_NAME(themeResolver)的bean记录到属性themeResolver,没有相应bean的话使用缺省策略
		initThemeResolver(context);
		//HandlerMapping相关:从bean容器中获取名字为HANDLER_MAPPING_BEAN_NAME(handlerMapping)的bean,或者获取所有类型是HandlerMapping的bean(DispatcherServlet的缺省做法),记录到属性handlerMappings,没有相应bean的话使用缺省策略
		initHandlerMappings(context);
		//HandlerAdapter (list) 相关:从bean容器中获取名字为HANDLER_ADAPTER_BEAN_NAME(handlerAdapter)的bean,或者获取所有类型是HandlerAdapter的bean(DispatcherServlet的缺省做法),记录到属性handlerAdapters,没有相应bean的话使用缺省策略	
		initHandlerAdapters(context);
		//HandlerExceptionResolver (list) 异常处理Handler相关:从bean容器中获取名字为HANDLER_EXCEPTION_RESOLVER_BEAN_NAME(handlerExceptionResolver)的bean,或者获取所有类型是HandlerExceptionResolver的bean(DispatcherServlet的缺省做法),记录到属性handlerExceptionResolvers,没有相应bean的话使用缺省策略	
		initHandlerExceptionResolvers(context);
		//RequestToViewNameTranslator 相关:从bean容器中获取名字为REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME(viewNameTranslator)的bean记录到属性viewNameTranslator,没有相应bean的话使用缺省策略
		initRequestToViewNameTranslator(context);
		// ViewResolver (list) 相关:从bean容器中获取名字为VIEW_RESOLVER_BEAN_NAME(viewResolver)的bean,或者获取所有类型是ViewResolver的bean(DispatcherServlet的缺省做法),记录到属性viewResolvers,没有相应bean的话使用缺省策略
		initViewResolvers(context);
		//FlashMapManager:从bean容器中获取名字为FLASH_MAP_MANAGER_BEAN_NAME(flashMapManager)的bean记录到属性flashMapManager,没有相应bean的话使用缺省策略
		initFlashMapManager(context);
	}

```

#### 选择handler

```
protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		if (this.handlerMappings != null) {
			for (HandlerMapping mapping : this.handlerMappings) {
                // 返回 HandlerExecutionChain 因为Spring MVC的拦截器机制有可能需要向目标Handler包裹一些HandlerInterceptor。
                //常见的一些HandlerInterceptor有ConversionServiceExposingInterceptor,ResourceUrlProviderExposingInterceptor                                          
				HandlerExecutionChain handler = mapping.getHandler(request);
				if (handler != null) {
					return handler;
				}
			}
		}
		return null;
	}

```

#### 默认返回noHandlerFound

```
protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (pageNotFoundLogger.isWarnEnabled()) {
			pageNotFoundLogger.warn("No mapping for " + request.getMethod() + " " + getRequestUri(request));
		}
        // 如果属性 throwExceptionIfNoHandlerFound 为 true，则抛出异常 NoHandlerFoundException
		if (this.throwExceptionIfNoHandlerFound) {
			throw new NoHandlerFoundException(request.getMethod(), getRequestUri(request),
					new ServletServerHttpRequest(request).getHeaders());
		}
		else {
            //如果属性 throwExceptionIfNoHandlerFound 为 false ， 则 response.sendError(404)
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

```
#### getHandlerAdapter(Object handler)

- DispatcherServlet请求处理过程中,执行Handler处理请求是通过HandlerAdapter完成的,而并非是DispatcherServlet直接调用Handler提供的处理方法

- DispatcherServlet初始化过程中初始化了 initHandlerAdapters,SpringMVC默认提供了多种HandlerAdapter (WebMvcConfigurationSupport)

```
protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		if (this.handlerAdapters != null) {
			for (HandlerAdapter adapter : this.handlerAdapters) {
				if (adapter.supports(handler)) {
					return adapter;
				}
			}
		}
		throw new ServletException("No adapter for handler [" + handler +
				"]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
	}

```
##### handle()方法子类实现： public abstract class AbstractHandlerMethodAdapter extends WebContentGenerator implements HandlerAdapter, Ordered

```
protected ModelAndView handleInternal(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

		ModelAndView mav;
		checkRequest(request);
		// Execute invokeHandlerMethod in synchronized block if required.
		if (this.synchronizeOnSession) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				Object mutex = WebUtils.getSessionMutex(session);
				synchronized (mutex) {
					mav = invokeHandlerMethod(request, response, handlerMethod);
				}
			}
			else {
				// No HttpSession available -> no mutex necessary
				mav = invokeHandlerMethod(request, response, handlerMethod);
			}
		}
		else {
			// No synchronization on session demanded at all...
			mav = invokeHandlerMethod(request, response, handlerMethod);
		}

		if (!response.containsHeader(HEADER_CACHE_CONTROL)) {
			if (getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
				applyCacheSeconds(response, this.cacheSecondsForSessionAttributeHandlers);
			}
			else {
				prepareResponse(response);
			}
		}

		return mav;
	}

```

##### handleInternal()方法 子类实现：public class RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapter implements BeanFactoryAware, InitializingBean
 
 ```
 protected ModelAndView handleInternal(HttpServletRequest request,HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
       
       		ModelAndView mav;
       		checkRequest(request);
       		// Execute invokeHandlerMethod in synchronized block if required.
       		if (this.synchronizeOnSession) {
       			HttpSession session = request.getSession(false);
       			if (session != null) {
       				Object mutex = WebUtils.getSessionMutex(session);
       				synchronized (mutex) {
       					mav = invokeHandlerMethod(request, response, handlerMethod);
       				}
       			}
       			else {
       				// No HttpSession available -> no mutex necessary
       				mav = invokeHandlerMethod(request, response, handlerMethod);
       			}
       		}
       		else {
       			// No synchronization on session demanded at all...
       			mav = invokeHandlerMethod(request, response, handlerMethod);
       		}
       
       		if (!response.containsHeader(HEADER_CACHE_CONTROL)) {
       			if (getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
       				applyCacheSeconds(response, this.cacheSecondsForSessionAttributeHandlers);
       			}
       			else {
       				prepareResponse(response);
       			}
       		}
       
       		return mav;
       	}

```

##### invokeHandlerMethod 实现

```
protected ModelAndView invokeHandlerMethod(HttpServletRequest request,HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

		ServletWebRequest webRequest = new ServletWebRequest(request, response);
		try {
			WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);
			ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);

			ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
			if (this.argumentResolvers != null) {
				invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
			}
			if (this.returnValueHandlers != null) {
				invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
			}
			invocableMethod.setDataBinderFactory(binderFactory);
			invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

			ModelAndViewContainer mavContainer = new ModelAndViewContainer();
			mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
			modelFactory.initModel(webRequest, mavContainer, invocableMethod);
			mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);

			AsyncWebRequest asyncWebRequest = WebAsyncUtils.createAsyncWebRequest(request, response);
			asyncWebRequest.setTimeout(this.asyncRequestTimeout);

			WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
			asyncManager.setTaskExecutor(this.taskExecutor);
			asyncManager.setAsyncWebRequest(asyncWebRequest);
			asyncManager.registerCallableInterceptors(this.callableInterceptors);
			asyncManager.registerDeferredResultInterceptors(this.deferredResultInterceptors);

			if (asyncManager.hasConcurrentResult()) {
				Object result = asyncManager.getConcurrentResult();
				mavContainer = (ModelAndViewContainer) asyncManager.getConcurrentResultContext()[0];
				asyncManager.clearConcurrentResult();
				LogFormatUtils.traceDebug(logger, traceOn -> {
					String formatted = LogFormatUtils.formatValue(result, !traceOn);
					return "Resume with async result [" + formatted + "]";
				});
				invocableMethod = invocableMethod.wrapConcurrentResult(result);
			}

			invocableMethod.invokeAndHandle(webRequest, mavContainer);
			if (asyncManager.isConcurrentHandlingStarted()) {
				return null;
			}

			return getModelAndView(mavContainer, modelFactory, webRequest);
		}
		finally {
			webRequest.requestCompleted();
		}
	}


```

##### invokeAndHandle 方法子类实现 : public class ServletInvocableHandlerMethod extends InvocableHandlerMethod 

```
public void invokeAndHandle(ServletWebRequest webRequest, ModelAndViewContainer mavContainer,Object... providedArgs) throws Exception {
        
        //真正执行控制器方法
		Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);
		setResponseStatus(webRequest);
		if (returnValue == null) {
			if (isRequestNotModified(webRequest) || getResponseStatus() != null || mavContainer.isRequestHandled()) {
				disableContentCachingIfNecessary(webRequest);
				mavContainer.setRequestHandled(true);
				return;
			}
		}
		else if (StringUtils.hasText(getResponseStatusReason())) {
			mavContainer.setRequestHandled(true);
			return;
		}
		mavContainer.setRequestHandled(false);
		Assert.state(this.returnValueHandlers != null, "No return value handlers");
		try {
		   //对返回值做处理 
			this.returnValueHandlers.handleReturnValue(returnValue, getReturnValueType(returnValue), mavContainer, webRequest);
		}
		catch (Exception ex) {
			if (logger.isTraceEnabled()) {
				logger.trace(formatErrorForReturnValue(returnValue), ex);
			}
			throw ex;
		}
	}

```
##### invokeForRequest子类实现：public class InvocableHandlerMethod extends HandlerMethod 

```
public Object invokeForRequest(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {

		Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
		if (logger.isTraceEnabled()) {
			logger.trace("Arguments: " + Arrays.toString(args));
		}
		return doInvoke(args);
}

```
##### 执行反射方法之前执行 methodArgumentResolvers.resolveArgument

```
protected Object[] getMethodArgumentValues(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {

		MethodParameter[] parameters = getMethodParameters();
		if (ObjectUtils.isEmpty(parameters)) {
			return EMPTY_ARGS;
		}
		Object[] args = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			MethodParameter parameter = parameters[i];
			parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
			args[i] = findProvidedArgument(parameter, providedArgs);
			if (args[i] != null) {
				continue;
			}
			if (!this.resolvers.supportsParameter(parameter)) {
				throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
			}
			try {
				args[i] = this.resolvers.resolveArgument(parameter, mavContainer, request, this.dataBinderFactory);
			}
			catch (Exception ex) {
				// Leave stack trace for later, exception may actually be resolved and handled...
				if (logger.isDebugEnabled()) {
					String exMsg = ex.getMessage();
					if (exMsg != null && !exMsg.contains(parameter.getExecutable().toGenericString())) {
						logger.debug(formatArgumentError(parameter, exMsg));
					}
				}
				throw ex;
			}
		}
		return args;
	}

```

#### 子类实现: public class RequestResponseBodyMethodProcessor extends AbstractMessageConverterMethodProcessor 

```
public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
		parameter = parameter.nestedIfOptional();
		Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
		String name = Conventions.getVariableNameForParameter(parameter);
		if (binderFactory != null) {
			WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name);
			if (arg != null) {
				validateIfApplicable(binder, parameter);
				if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
					throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
				}
			}
			if (mavContainer != null) {
				mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
			}
		}

		return adaptArgumentIfNecessary(arg, parameter);
	}
```
##### 执行类型转换 [SpringMVC源码解析只类型转换](./SpringMVC源码解析之类型转换.md)


```	
protected <T> Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter,Type paramType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {
		HttpInputMessage inputMessage = createInputMessage(webRequest);
		return readWithMessageConverters(inputMessage, parameter, paramType);
	}
	
```

#### 执行反射方法

```
protected Object doInvoke(Object... args) throws Exception {
		ReflectionUtils.makeAccessible(getBridgedMethod());
		try {
			return getBridgedMethod().invoke(getBean(), args);
		}
		catch (IllegalArgumentException ex) {
			assertTargetBean(getBridgedMethod(), getBean(), args);
			String text = (ex.getMessage() != null ? ex.getMessage() : "Illegal argument");
			throw new IllegalStateException(formatInvokeError(text, args), ex);
		}
		catch (InvocationTargetException ex) {
			// Unwrap for HandlerExceptionResolvers ...
			Throwable targetException = ex.getTargetException();
			if (targetException instanceof RuntimeException) {
				throw (RuntimeException) targetException;
			}
			else if (targetException instanceof Error) {
				throw (Error) targetException;
			}
			else if (targetException instanceof Exception) {
				throw (Exception) targetException;
			}
			else {
				throw new IllegalStateException(formatInvokeError("Invocation failure", args), targetException);
			}
		}
	}
	
```

#### doDispatch()

```
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpServletRequest processedRequest = request;
		HandlerExecutionChain mappedHandler = null;
		boolean multipartRequestParsed = false;

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

		try {
			ModelAndView mv = null;
			Exception dispatchException = null;

			try {
				processedRequest = checkMultipart(request);
				multipartRequestParsed = (processedRequest != request);
				// 获取
				mappedHandler = getHandler(processedRequest);
                //如果获取请求的Handler失败,则设置 response No handler found -> set appropriate HTTP response status.
				if (mappedHandler == null) {
					noHandlerFound(processedRequest, response);
					return;
				}

				// 获取指定handler的HandlerAdapter
				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

				// Process last-modified header, if supported by the handler.
				String method = request.getMethod();
				boolean isGet = "GET".equals(method);
				if (isGet || "HEAD".equals(method)) {
					long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
					if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
						return;
					}
				}
                //1. mappedHandler 是所找到的Handler，类型为HandlerExecutionChain,相当于多个 HandlerInterceptor 包裹一个Handler            
                //2. 这里 mv 是一个类型为ModeAndView的对象
                //3. 这里ha 是做找到的HandlerAdapter
                //在调用 Handler 处理请求之前,应用各个 HandlerInterceptor 的前置拦截处理逻辑 preHandle;如果拦截器处理失败，直接返回
				if (!mappedHandler.applyPreHandle(processedRequest, response)) {
					return;
				}

				// Actually invoke the handler.
				// 各个HandlerInterceptor#preHandle前置处理逻辑应用完成且都返回true,现在需要通过ha调用相应的Handler了
				mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

				if (asyncManager.isConcurrentHandlingStarted()) {
					return;
				}
                // 如果 ModelAndView 不等于null 并且不含试图名时设置默认名称。(默认视图名为 前缀+lookupPath+后缀)
				applyDefaultViewName(processedRequest, mv);
				// 现在已经使用Handler处理了请求，结果是ModelAndView，并且对默认视图设置了默认视图名。调用各个HandlerInterceptor的后置拦截处理器postHandler
				mappedHandler.applyPostHandle(processedRequest, response, mv);
			}
			catch (Exception ex) {
				dispatchException = ex;
			}
			catch (Throwable err) {
				// As of 4.3, we're processing Errors thrown from handler methods as well,
				// making them available for @ExceptionHandler methods and other scenarios.
				dispatchException = new NestedServletException("Handler dispatch failed", err);
			}
			// 如果有异常或者是 ModelAndView 是返回类型则通过render方法直接返回视图
			processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
		}
		catch (Exception ex) {
			triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
		}
		catch (Throwable err) {
			triggerAfterCompletion(processedRequest, response, mappedHandler,
					new NestedServletException("Handler processing failed", err));
		}
		finally {
			if (asyncManager.isConcurrentHandlingStarted()) {
				// Instead of postHandle and afterCompletion
				if (mappedHandler != null) {
					mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
				}
			}
			else {
				// Clean up any resources used by a multipart request.
				if (multipartRequestParsed) {
					cleanupMultipart(processedRequest);
				}
			}
		}
	}

```

#### private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,@Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,@Nullable Exception exception) throws Exception 

```
private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
			@Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,
			@Nullable Exception exception) throws Exception {
        //记录是否所要渲染的视图是一个错误页面视图，用于一些属性标记和清除动作 
		boolean errorView = false;
        //如果有异常则返回错误视图
		if (exception != null) {
		    //注意这种情况下，此方法调用开始时参数 mv 一定为 null，这是由DispatcherServlet#doDispatch方法的逻辑流程所决定的。
		    //如果是ModelAndViewDefiningException类型异常则直接返回
			if (exception instanceof ModelAndViewDefiningException) {
				logger.debug("ModelAndViewDefiningException encountered", exception);
				mv = ((ModelAndViewDefiningException) exception).getModelAndView();
			}
			//非ModelAndViewDefiningException类型异常，则构造一个异常视图
			else {
				Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
				mv = processHandlerException(request, response, handler, exception);
				errorView = (mv != null);
			}
		}
		//  当前 mv 也可能是 null，比如控制器方法直接返回 JSON/XML而无需视图渲染的情况，这种情况下当前方法在这里就算结束了，仅为它进入不了下面的 if 语句块
		if (mv != null && !mv.wasCleared()) {
			render(mv, request, response);
			//如果是错误视图
			if (errorView) {
				WebUtils.clearErrorRequestAttributes(request);
			}
		}
		else {
			if (logger.isTraceEnabled()) {
				logger.trace("No view rendering, null ModelAndView returned.");
			}
		}

		if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
			// Concurrent handling started during a forward
			return;
		}

		if (mappedHandler != null) {
			mappedHandler.triggerAfterCompletion(request, response, null);
		}
	}

```

#### protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response)//解析绘出视图

```
protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// Determine locale for request and apply it to the response.
		//本地化处理, 属性 this.localeResolver 在 DispatcherServlet 初始化时由初始化方法 initLocaleResolver 填充
		Locale locale =(this.localeResolver != null ? this.localeResolver.resolveLocale(request) : request.getLocale());
		response.setLocale(locale);

		View view;
		//尝试从 ModelAndView mv中分析得到 View view 
		String viewName = mv.getViewName();
		if (viewName != null) {
			// 根据视图名称找到视图对象，该方法使用到了this.viewResolvers属性中注册的视图解析器
			// 将视图名称解析成一个View对象返回
			// this,viewResolvers 是DispatcherServlet初始化方法，initViewResolvers填充的
			// 常见视图解析器：
			// 1. ContentNegotiatingViewResolver 2. BeanNameViewResolver 3. FreeMarkerViewResolver 4. ViewResolverComposite 5. InternalResourceViewResolver
			view = resolveViewName(viewName, mv.getModelInternal(), locale, request);
			if (view == null) {
			   //如果不存在能够解析当前视图名称的  ViewResolver，则抛出该异常
				throw new ServletException("Could not resolve view with name '" + mv.getViewName() +
						"' in servlet with name '" + getServletName() + "'");
			}
		}
		else {
			// No need to lookup: the ModelAndView object contains the actual View object.
			//现在已经从 ModelAndView 中分析得到 View 对象了，现在使用它进行结合要渲染的数据进行视图渲染
			view = mv.getView();
			if (view == null) {
				throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " +
						"View object in servlet with name '" + getServletName() + "'");
			}
		}

		// Delegate to the View object for rendering.
		if (logger.isTraceEnabled()) {
			logger.trace("Rendering view [" + view + "] ");
		}
		try {
			if (mv.getStatus() != null) {
				response.setStatus(mv.getStatus().value());
			}
			// 视图渲染：
			// 结合数据模型和视图模版形成最终的视图数据写入到响应对象
			// 不过这里视图数据写入后并不会发送给请求者。将响应数据最终发送给请求者也就是浏览器端的动作，会在随后调用控制返回给应用服务器时，由应用服务器执行：
			// 1. 从用户直观的感受来讲，当前方法结束时，用户在浏览器上看不到写入的视图数据
			// 2. 针对应用服务器是Tomcat的情况，是在 CoyoteAdapter#service 中请求被Spring MVC 处理完之后调用 response.finishResponse() 才真正将数据返回给请求者。
			view.render(mv.getModelInternal(), request, response);
		}
		catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Error rendering view [" + view + "]", ex);
			}
			throw ex;
		}
	}

```