### HandlerAdapter

#####  SimpleControllerHandlerAdapter //直接处理 HttpServletRequest , HttpServletResponse 

```
public class SimpleControllerHandlerAdapter implements HandlerAdapter {

	@Override
	public boolean supports(Object handler) {
		return (handler instanceof Controller);
	}

	@Override
	@Nullable
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		return ((Controller) handler).handleRequest(request, response);
	}

	@Override
	public long getLastModified(HttpServletRequest request, Object handler) {
		if (handler instanceof LastModified) {
			return ((LastModified) handler).getLastModified(request);
		}
		return -1L;
	}

}

```


#####  HttpRequestHandlerAdapter

适配org.springframework.web.HttpRequestHandler这种Handler
````
public class HttpRequestHandlerAdapter implements HandlerAdapter {

	@Override
	public boolean supports(Object handler) {
		return (handler instanceof HttpRequestHandler);
	}

	@Override
	@Nullable
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		((HttpRequestHandler) handler).handleRequest(request, response);
		return null; //HttpRequestHandler#handleRequest()它没有返回值（全靠开发者自己写response），而Controller最起码来说还有Model和View自动渲染
	}

	@Override
	public long getLastModified(HttpServletRequest request, Object handler) {
		if (handler instanceof LastModified) {
			return ((LastModified) handler).getLastModified(request);
		}
		return -1L;
	}

}

````

##### SimpleServletHandlerAdapter

Spring MVC默认并不向容器注册这种HandlerAdapter，若需要使用是需要调用者手动给注册这个Bean，Servlet这种Handler才能正常使用

```
public class SimpleServletHandlerAdapter implements HandlerAdapter {

	@Override
	public boolean supports(Object handler) {
		return (handler instanceof Servlet);
	}

	@Override
	@Nullable
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		((Servlet) handler).service(request, response);
		return null;
	}

	@Override
	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

}

```

#####  public class RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapter implements BeanFactoryAware, InitializingBean 

###### RequestMappingHandlerAdapter 是Spring MVC用来执行控制器方法的HandlerAdapter
 
```
1. RequestMappingHandlerAdapter继承自抽象基类AbstractHandlerMethodAdapter,AbstractHandlerMethodAdapter主要是定义了一些抽象方法和禁止了一些方法的继承。AbstractHandlerMethodAdapter又继承自WebContentGenerator,WebContentGenerator主要提供了对HTTP缓存机制方面的支持。

2. RequestMappingHandlerAdapter实现了InitializingBean接口，所以相应它的bean组件在实例化过程中会执行其初始化方法#afterPropertiesSet。

```

- List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers()

```
private List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

		// Annotation-based argument resolution
		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));//@RequestParam并且参数类型不是Map
		resolvers.add(new RequestParamMapMethodArgumentResolver());//@RequestParam并且参数类型是Map
		resolvers.add(new PathVariableMethodArgumentResolver());//@PathVariable并且参数类型不是Map
		resolvers.add(new PathVariableMapMethodArgumentResolver());//@PathVariable并且参数类型是Map
		resolvers.add(new MatrixVariableMethodArgumentResolver());//@MatrixVariable并且参数类型不是Map
		resolvers.add(new MatrixVariableMapMethodArgumentResolver());//@MatrixVariable并且参数类型是Map
		resolvers.add(new ServletModelAttributeMethodProcessor(false));//true 表示 : 即使不使用注解 @ModelAttribute,非简单类型的方法参数和返回值也会被认为是 model attribute
		resolvers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));//@RequestBody,@ResponseBody
		resolvers.add(new RequestPartMethodArgumentResolver(getMessageConverters(), this.requestResponseBodyAdvice));//@RequestPart
		resolvers.add(new RequestHeaderMethodArgumentResolver(getBeanFactory()));//@RequestHeader并且参数类型不是Map
		resolvers.add(new RequestHeaderMapMethodArgumentResolver());//@RequestHeader并且参数类型是Map
		resolvers.add(new ServletCookieValueMethodArgumentResolver(getBeanFactory()));//@CookieValue
		resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));//@Value
		resolvers.add(new SessionAttributeMethodArgumentResolver());//@SessionAttribute
		resolvers.add(new RequestAttributeMethodArgumentResolver());//@RequestAttribute

		// Type-based argument resolution
		resolvers.add(new ServletRequestMethodArgumentResolver());//WebRequest,ServletRequest,MultipartRequest,HttpSession,PushBuilder,Principal,InputStream, Reader,
                                                                  // HttpMethod,Locale, TimeZone,java.time.ZoneId                                                                                                                                                                                                                                                                                                                                    
		resolvers.add(new ServletResponseMethodArgumentResolver());//ServletResponse,OutputStream,Writer
		resolvers.add(new HttpEntityMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));//HttpEntity,RequestEntity,ResponseEntity
		resolvers.add(new RedirectAttributesMethodArgumentResolver());//RedirectAttributes
		resolvers.add(new ModelMethodProcessor());//Model类型的参数和返回值
		resolvers.add(new MapMethodProcessor());//Map类型的参数和返回值
		resolvers.add(new ErrorsMethodArgumentResolver());//Errors
		resolvers.add(new SessionStatusMethodArgumentResolver());//SessionStatus
		resolvers.add(new UriComponentsBuilderMethodArgumentResolver());//UriComponentsBuilder

		// Custom arguments 
		// Custom arguments , 添加定制的参数解析器
		if (getCustomArgumentResolvers() != null) {
			resolvers.addAll(getCustomArgumentResolvers());
		}

		// Catch-all 如果上面的参数解析器都处理不了，尝试使用下面的解析器尝试兜底
		//true 表示 : 即使不使用注解 @RequestParam ,简单类型的方法参数也会被认为是请求参数被解析,此时请求参数名称会根据方法参数名称派生而来。
		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));
		//true 表示 : 即使不使用注解 @ModelAttribute,非简单类型的方法参数和返回值也会被认为
		//是 model attribute 
		resolvers.add(new ServletModelAttributeMethodProcessor(true));
		return resolvers;
	}

```

- private List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() 

```
	private List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
		List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();

		// Single-purpose return value types
		handlers.add(new ModelAndViewMethodReturnValueHandler());
		handlers.add(new ModelMethodProcessor());
		handlers.add(new ViewMethodReturnValueHandler());
		handlers.add(new ResponseBodyEmitterReturnValueHandler(getMessageConverters(),
				this.reactiveAdapterRegistry, this.taskExecutor, this.contentNegotiationManager));
		handlers.add(new StreamingResponseBodyReturnValueHandler());
		handlers.add(new HttpEntityMethodProcessor(getMessageConverters(),
				this.contentNegotiationManager, this.requestResponseBodyAdvice));
		handlers.add(new HttpHeadersReturnValueHandler());
		handlers.add(new CallableMethodReturnValueHandler());
		handlers.add(new DeferredResultMethodReturnValueHandler());
		handlers.add(new AsyncTaskMethodReturnValueHandler(this.beanFactory));

		// Annotation-based return value types
		handlers.add(new ModelAttributeMethodProcessor(false));
		handlers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(),
				this.contentNegotiationManager, this.requestResponseBodyAdvice));

		// Multi-purpose return value types
		handlers.add(new ViewNameMethodReturnValueHandler());
		handlers.add(new MapMethodProcessor());

		// Custom return value types
		if (getCustomReturnValueHandlers() != null) {
			handlers.addAll(getCustomReturnValueHandlers());
		}

		// Catch-all
		if (!CollectionUtils.isEmpty(getModelAndViewResolvers())) {
			handlers.add(new ModelAndViewResolverMethodReturnValueHandler(getModelAndViewResolvers()));
		}
		else {
			handlers.add(new ModelAttributeMethodProcessor(true));
		}

		return handlers;
	}
```

#### 子类：RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapter implements BeanFactoryAware, InitializingBean

```
public class RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapterimplements BeanFactoryAware, InitializingBean {

	/**
	 * MethodFilter that matches InitBinder @InitBinder methods.
	 */
	public static final MethodFilter INIT_BINDER_METHODS = method -> AnnotatedElementUtils.hasAnnotation(method, InitBinder.class);

	/**
	 * MethodFilter that matches ModelAttribute @ModelAttribute methods.
	 */
	public static final MethodFilter MODEL_ATTRIBUTE_METHODS = method ->(!AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class) &&
					AnnotatedElementUtils.hasAnnotation(method, ModelAttribute.class));


    // 自定义的参数解析器
	@Nullable
	private List<HandlerMethodArgumentResolver> customArgumentResolvers;

    // 内置参数解析器
	@Nullable
	private HandlerMethodArgumentResolverComposite argumentResolvers;

    // 供@InitBinder注解方法使用的参数解析器
	@Nullable
	private HandlerMethodArgumentResolverComposite initBinderArgumentResolvers;

    // 自定义的返回值处理器
	@Nullable
	private List<HandlerMethodReturnValueHandler> customReturnValueHandlers;

    // 内置返回值处理器
	@Nullable
	private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

	@Nullable
	private List<ModelAndViewResolver> modelAndViewResolvers;

    // 内容协商管理器 : 可以判断请求的媒体类型MediaType,也可以根据媒体类型MediaType获取相应的文件扩展名
	private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

    // HTTP消息转换器
	private List<HttpMessageConverter<?>> messageConverters;

	private List<Object> requestResponseBodyAdvice = new ArrayList<>();

	@Nullable
	private WebBindingInitializer webBindingInitializer;

    // 异步任务执行器
	private AsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("MvcAsync");

	@Nullable
	private Long asyncRequestTimeout;

	private CallableProcessingInterceptor[] callableInterceptors = new CallableProcessingInterceptor[0];

	private DeferredResultProcessingInterceptor[] deferredResultInterceptors = 
		new DeferredResultProcessingInterceptor[0];

	private ReactiveAdapterRegistry reactiveAdapterRegistry = ReactiveAdapterRegistry.getSharedInstance();

	private boolean ignoreDefaultModelOnRedirect = false;

	private int cacheSecondsForSessionAttributeHandlers = 0;

    // 是否会话级别同步处理请求，缺省为 false
	private boolean synchronizeOnSession = false;

	private SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();

    // 控制器方法参数名称发现器，缺省使用 DefaultParameterNameDiscoverer
	private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

	@Nullable
	private ConfigurableBeanFactory beanFactory;


	private final Map<Class<?>, SessionAttributesHandler> sessionAttributesHandlerCache 
        = new ConcurrentHashMap<>(64);

	private final Map<Class<?>, Set<Method>> initBinderCache = new ConcurrentHashMap<>(64);

	private final Map<ControllerAdviceBean, Set<Method>> initBinderAdviceCache = new LinkedHashMap<>();

	private final Map<Class<?>, Set<Method>> modelAttributeCache = new ConcurrentHashMap<>(64);

	private final Map<ControllerAdviceBean, Set<Method>> modelAttributeAdviceCache = new LinkedHashMap<>();


    // 缺省构造函数
	public RequestMappingHandlerAdapter() {
        // 缺省使用字符集 ISO-8859-1
		StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
		stringHttpMessageConverter.setWriteAcceptCharset(false);  // see SPR-7316

		this.messageConverters = new ArrayList<>(4);
       // 缺省支持所有媒体类型 : */* , 写使用 Content-Type : application/octet-stream
		this.messageConverters.add(new ByteArrayHttpMessageConverter());
		this.messageConverters.add(stringHttpMessageConverter);
		try {
           // text/xml,application/xml, application/*+xml
			this.messageConverters.add(new SourceHttpMessageConverter<>());
		}
		catch (Error err) {
			// Ignore when no TransformerFactory implementation is available
		}
		this.messageConverters.add(new AllEncompassingFormHttpMessageConverter());
	}


	/**
	 * 提供自定义的参数解析器。自定义参数解析器会排在内置参数解析器之后使用。
     * 如果要覆盖内置参数解析器，要使用 #setArgumentResolvers 而不是该方法
     * Provide resolvers for custom argument types. Custom resolvers are ordered
	 * after built-in ones. To override the built-in support for argument
	 * resolution use #setArgumentResolvers instead.
	 */
	public void setCustomArgumentResolvers(@Nullable List<HandlerMethodArgumentResolver> argumentResolvers) {
		this.customArgumentResolvers = argumentResolvers;
	}

	/**
	 * 返回所设置的自定义参数解析器，如果没有设置自定义参数解析器则返回null
     * Return the custom argument resolvers, or null.
	 */
	@Nullable
	public List<HandlerMethodArgumentResolver> getCustomArgumentResolvers() {
		return this.customArgumentResolvers;
	}

	/**
	 * 提供一个完整的要支持的参数解析器列表从而覆盖内置缺省的参数类型解析器
     * Configure the complete list of supported argument types thus overriding
	 * the resolvers that would otherwise be configured by default.
	 */
	public void setArgumentResolvers(@Nullable List<HandlerMethodArgumentResolver> argumentResolvers) {
		if (argumentResolvers == null) {
			this.argumentResolvers = null;
		}
		else {
			this.argumentResolvers = new HandlerMethodArgumentResolverComposite();
			this.argumentResolvers.addResolvers(argumentResolvers);
		}
	}

	/**
	 * Return the configured argument resolvers, or possibly null if
	 * not initialized yet via #afterPropertiesSet().
    * 返回所设置的参数解析器，如果当前bean尚未初始化(#afterPropertiesSet()还未执行)则返回null
	 */
	@Nullable
	public List<HandlerMethodArgumentResolver> getArgumentResolvers() {
		return (this.argumentResolvers != null ? this.argumentResolvers.getResolvers() : null);
	}

	/**
	 * Configure the supported argument types in @InitBinder methods.
	 */
	public void setInitBinderArgumentResolvers(@Nullable List<HandlerMethodArgumentResolver> argumentResolvers) {
		if (argumentResolvers == null) {
			this.initBinderArgumentResolvers = null;
		}
		else {
			this.initBinderArgumentResolvers = new HandlerMethodArgumentResolverComposite();
			this.initBinderArgumentResolvers.addResolvers(argumentResolvers);
		}
	}

	/**
	 * Return the argument resolvers for @InitBinder methods, or possibly
	 * null if not initialized yet via #afterPropertiesSet().
    * 返回@InitBinder方法所要使用的参数解析器，如果当前bean尚未初始化(#afterPropertiesSet()还未执行)则返回null
	 */
	@Nullable
	public List<HandlerMethodArgumentResolver> getInitBinderArgumentResolvers() {
		return (this.initBinderArgumentResolvers != null ? 
			this.initBinderArgumentResolvers.getResolvers() : null);
	}

	/**
	 * 提供一组自定义的返回值处理器。自定义返回值处理器会排在内置返回值处理器之后被使用。
     * 如果想覆盖内置返回值处理器，需要使用 #setReturnValueHandlers 而不是该方法
     * Provide handlers for custom return value types. Custom handlers are
	 * ordered after built-in ones. To override the built-in support for
	 * return value handling use #setReturnValueHandlers.
	 */
	public void setCustomReturnValueHandlers(
		@Nullable List<HandlerMethodReturnValueHandler> returnValueHandlers) {
		this.customReturnValueHandlers = returnValueHandlers;
	}

	/**
	 * 返回自定义返回值解析器，如果没有设置则返回null
     * Return the custom return value handlers, or null.
	 */
	@Nullable
	public List<HandlerMethodReturnValueHandler> getCustomReturnValueHandlers() {
		return this.customReturnValueHandlers;
	}

	/**
	 * 提供一组完整的要支持的返回值解析器覆盖内置缺省使用得返回值解析器。
     * Configure the complete list of supported return value types thus
	 * overriding handlers that would otherwise be configured by default.
	 */
	public void setReturnValueHandlers(@Nullable List<HandlerMethodReturnValueHandler> returnValueHandlers) {
		if (returnValueHandlers == null) {
			this.returnValueHandlers = null;
		}
		else {
			this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite();
			this.returnValueHandlers.addHandlers(returnValueHandlers);
		}
	}

	/**
	 * Return the configured handlers, or possibly null if not
	 * initialized yet via #afterPropertiesSet().
     * 返回所配置的返回值处理器，如果当前bean尚未初始化(#afterPropertiesSet()还未执行)则返回null
	 */
	@Nullable
	public List<HandlerMethodReturnValueHandler> getReturnValueHandlers() {
		return (this.returnValueHandlers != null ? this.returnValueHandlers.getHandlers() : null);
	}

	/**
	 * Provide custom ModelAndViewResolvers.
	 * Note: This method is available for backwards
	 * compatibility only. However, it is recommended to re-write a
	 *  ModelAndViewResolver as HandlerMethodReturnValueHandler.
	 * An adapter between the two interfaces is not possible since the
	 * HandlerMethodReturnValueHandler#supportsReturnType method
	 * cannot be implemented. Hence ModelAndViewResolvers are limited
	 * to always being invoked at the end after all other return value
	 * handlers have been given a chance.
	 * A HandlerMethodReturnValueHandler provides better access to
	 * the return type and controller method information and can be ordered
	 * freely relative to other return value handlers.
	 */
	public void setModelAndViewResolvers(@Nullable List<ModelAndViewResolver> modelAndViewResolvers) {
		this.modelAndViewResolvers = modelAndViewResolvers;
	}

	/**
	 * Return the configured ModelAndViewResolver ModelAndViewResolvers, or null.
	 */
	@Nullable
	public List<ModelAndViewResolver> getModelAndViewResolvers() {
		return this.modelAndViewResolvers;
	}

	/**
	 * Set the ContentNegotiationManager to use to determine requested media types.
	 * If not set, the default constructor is used.
	 */
	public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
		this.contentNegotiationManager = contentNegotiationManager;
	}

	/**
	 * Provide the converters to use in argument resolvers and return value
	 * handlers that support reading and/or writing to the body of the
	 * request and response.
	 */
	public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
		this.messageConverters = messageConverters;
	}

	/**
	 * Return the configured message body converters.
	 */
	public List<HttpMessageConverter<?>> getMessageConverters() {
		return this.messageConverters;
	}

	/**
	 * Add one or more RequestBodyAdvice instances to intercept the
	 * request before it is read and converted for @RequestBody and
	 * HttpEntity method arguments.
	 */
	public void setRequestBodyAdvice(@Nullable List<RequestBodyAdvice> requestBodyAdvice) {
		if (requestBodyAdvice != null) {
			this.requestResponseBodyAdvice.addAll(requestBodyAdvice);
		}
	}

	/**
	 * Add one or more ResponseBodyAdvice instances to intercept the
	 * response before @ResponseBody or ResponseEntity return
	 * values are written to the response body.
	 */
	public void setResponseBodyAdvice(@Nullable List<ResponseBodyAdvice<?>> responseBodyAdvice) {
		if (responseBodyAdvice != null) {
			this.requestResponseBodyAdvice.addAll(responseBodyAdvice);
		}
	}

	/**
	 * Provide a WebBindingInitializer with "global" initialization to apply
	 * to every DataBinder instance.
	 */
	public void setWebBindingInitializer(@Nullable WebBindingInitializer webBindingInitializer) {
		this.webBindingInitializer = webBindingInitializer;
	}

	/**
	 * Return the configured WebBindingInitializer, or null if none.
	 */
	@Nullable
	public WebBindingInitializer getWebBindingInitializer() {
		return this.webBindingInitializer;
	}

	/**
	 * Set the default AsyncTaskExecutor to use when a controller method
	 * return a Callable. Controller methods can override this default on
	 * a per-request basis by returning an WebAsyncTask.
	 * By default a SimpleAsyncTaskExecutor instance is used.
	 * It's recommended to change that default in production as the simple executor
	 * does not re-use threads.
	 */
	public void setTaskExecutor(AsyncTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * Specify the amount of time, in milliseconds, before concurrent handling
	 * should time out. In Servlet 3, the timeout begins after the main request
	 * processing thread has exited and ends when the request is dispatched again
	 * for further processing of the concurrently produced result.
	 * If this value is not set, the default timeout of the underlying
	 * implementation is used, e.g. 10 seconds on Tomcat with Servlet 3.
	 * @param timeout the timeout value in milliseconds
	 */
	public void setAsyncRequestTimeout(long timeout) {
		this.asyncRequestTimeout = timeout;
	}

	/**
	 * Configure CallableProcessingInterceptor's to register on async requests.
	 * @param interceptors the interceptors to register
	 */
	public void setCallableInterceptors(List<CallableProcessingInterceptor> interceptors) {
		this.callableInterceptors = interceptors.toArray(new CallableProcessingInterceptor[0]);
	}

	/**
	 * Configure DeferredResultProcessingInterceptor's to register on async requests.
	 * @param interceptors the interceptors to register
	 */
	public void setDeferredResultInterceptors(List<DeferredResultProcessingInterceptor> interceptors) {
		this.deferredResultInterceptors = interceptors.toArray(new DeferredResultProcessingInterceptor[0]);
	}

	/**
	 * Configure the registry for reactive library types to be supported as
	 * return values from controller methods.
	 * @since 5.0.5
	 */
	public void setReactiveAdapterRegistry(ReactiveAdapterRegistry reactiveAdapterRegistry) {
		this.reactiveAdapterRegistry = reactiveAdapterRegistry;
	}

	/**
	 * Return the configured reactive type registry of adapters.
	 * @since 5.0
	 */
	public ReactiveAdapterRegistry getReactiveAdapterRegistry() {
		return this.reactiveAdapterRegistry;
	}

	/**
	 * By default the content of the "default" model is used both during
	 * rendering and redirect scenarios. Alternatively a controller method
	 * can declare a RedirectAttributes argument and use it to provide
	 * attributes for a redirect.
	 * Setting this flag to true guarantees the "default" model is
	 * never used in a redirect scenario even if a RedirectAttributes argument
	 * is not declared. Setting it to false means the "default" model
	 * may be used in a redirect if the controller method doesn't declare a
	 * RedirectAttributes argument.
	 * The default setting is false but new applications should
	 * consider setting it to true.
	 * @see RedirectAttributes
	 */
	public void setIgnoreDefaultModelOnRedirect(boolean ignoreDefaultModelOnRedirect) {
		this.ignoreDefaultModelOnRedirect = ignoreDefaultModelOnRedirect;
	}

	/**
	 * Specify the strategy to store session attributes with. The default is
	 * org.springframework.web.bind.support.DefaultSessionAttributeStore,
	 * storing session attributes in the HttpSession with the same attribute
	 * name as in the model.
	 */
	public void setSessionAttributeStore(SessionAttributeStore sessionAttributeStore) {
		this.sessionAttributeStore = sessionAttributeStore;
	}

	/**
	 * Cache content produced by @SessionAttributes annotated handlers
	 * for the given number of seconds.
	 * Possible values are:
	 * 
	 * -1: no generation of cache-related headers
	 * 0 (default value): "Cache-Control: no-store" will prevent caching
	 * 1 or higher: "Cache-Control: max-age=seconds" will ask to cache content;
	 * not advised when dealing with session attributes
	 * 
	 * In contrast to the "cacheSeconds" property which will apply to all general
	 * handlers (but not to @SessionAttributes annotated handlers),
	 * this setting will apply to @SessionAttributes handlers only.
	 * @see #setCacheSeconds
	 * @see org.springframework.web.bind.annotation.SessionAttributes
	 */
	public void setCacheSecondsForSessionAttributeHandlers(int cacheSecondsForSessionAttributeHandlers) {
		this.cacheSecondsForSessionAttributeHandlers = cacheSecondsForSessionAttributeHandlers;
	}

	/**
	 * Set if controller execution should be synchronized on the session,
	 * to serialize parallel invocations from the same client.
	 * 
     * More specifically, the execution of the handleRequestInternal
	 * method will get synchronized if this flag is "true". The best available
	 * session mutex will be used for the synchronization; ideally, this will
	 * be a mutex exposed by HttpSessionMutexListener.
	 * 
     * The session mutex is guaranteed to be the same object during
	 * the entire lifetime of the session, available under the key defined
	 * by the SESSION_MUTEX_ATTRIBUTE constant. It serves as a
	 * safe reference to synchronize on for locking on the current session.
	 * 
     * In many cases, the HttpSession reference itself is a safe mutex
	 * as well, since it will always be the same object reference for the
	 * same active logical session. However, this is not guaranteed across
	 * different servlet containers; the only 100% safe way is a session mutex.
	 * @see org.springframework.web.util.HttpSessionMutexListener
	 * @see org.springframework.web.util.WebUtils#getSessionMutex(javax.servlet.http.HttpSession)
	 */
	public void setSynchronizeOnSession(boolean synchronizeOnSession) {
		this.synchronizeOnSession = synchronizeOnSession;
	}

	/**
	 * Set the ParameterNameDiscoverer to use for resolving method parameter names if needed
	 * (e.g. for default attribute names).
	 * Default is a org.springframework.core.DefaultParameterNameDiscoverer.
     * 设置控制器方法参数名称发现器，如果不设置，缺省使用 
     * org.springframework.core.DefaultParameterNameDiscoverer
	 */
	public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	/**
	 * A ConfigurableBeanFactory is expected for resolving expressions
	 * in method argument default values.
     * bean容器，类型要求为 ConfigurableBeanFactory， 用于解析方法参数缺省值表达式
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (beanFactory instanceof ConfigurableBeanFactory) {
			this.beanFactory = (ConfigurableBeanFactory) beanFactory;
		}
	}

	/**
	 * Return the owning factory of this bean instance, or null if none.
	 */
	@Nullable
	protected ConfigurableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}


    // InitializingBean 接口定义的该bean的初始化方法 ：
    // 1. 初始化 controller advice cache : this.requestResponseBodyAdvice
    // 2. 初始化参数解析器 ：this.argumentResolvers
    // 3. 初始化 @InitBinder 方法使用的参数解析器 : this.initBinderArgumentResolvers
    // 4. 初始化 返回值处理器 : this.returnValueHandlers
	@Override
	public void afterPropertiesSet() {
		// Do this first, it may add ResponseBody advice beans
		initControllerAdviceCache();

		if (this.argumentResolvers == null) {
			List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
			this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
		}
		if (this.initBinderArgumentResolvers == null) {
			List<HandlerMethodArgumentResolver> resolvers = getDefaultInitBinderArgumentResolvers();
			this.initBinderArgumentResolvers = 
				new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
		}
		if (this.returnValueHandlers == null) {
			List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
			this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
		}
	}

    // 初始化控制器 advice cache
	private void initControllerAdviceCache() {
		if (getApplicationContext() == null) {
			return;
		}

		// 找到所有使用了注解 @ControllerAdvice 的 bean,将它们包装成一组ControllerAdviceBean对象返回	
		List<ControllerAdviceBean> adviceBeans = 
			ControllerAdviceBean.findAnnotatedBeans(getApplicationContext());
		// 排序	
		AnnotationAwareOrderComparator.sort(adviceBeans);

		List<Object> requestResponseBodyAdviceBeans = new ArrayList<>();

		for (ControllerAdviceBean adviceBean : adviceBeans) {
			Class<?> beanType = adviceBean.getBeanType();
			if (beanType == null) {
				throw new IllegalStateException("Unresolvable type for ControllerAdviceBean: " + adviceBean);
			}
			// 找到所有使用注解@ModelAttribute的方法
			Set<Method> attrMethods = MethodIntrospector.selectMethods(beanType, MODEL_ATTRIBUTE_METHODS);
			if (!attrMethods.isEmpty()) {
				this.modelAttributeAdviceCache.put(adviceBean, attrMethods);
			}
			
			// 找到所有使用注解@InitBinder的方法
			Set<Method> binderMethods = MethodIntrospector.selectMethods(beanType, INIT_BINDER_METHODS);
			if (!binderMethods.isEmpty()) {
				this.initBinderAdviceCache.put(adviceBean, binderMethods);
			}
			// 找到所有 RequestBodyAdvice
			if (RequestBodyAdvice.class.isAssignableFrom(beanType)) {
				requestResponseBodyAdviceBeans.add(adviceBean);
			}
		    // 找到所有 ResponseBodyAdvice
			if (ResponseBodyAdvice.class.isAssignableFrom(beanType)) {
				requestResponseBodyAdviceBeans.add(adviceBean);
			}
		}

		if (!requestResponseBodyAdviceBeans.isEmpty()) {
			this.requestResponseBodyAdvice.addAll(0, requestResponseBodyAdviceBeans);
		}

		if (logger.isDebugEnabled()) {
			int modelSize = this.modelAttributeAdviceCache.size();
			int binderSize = this.initBinderAdviceCache.size();
			int reqCount = getBodyAdviceCount(RequestBodyAdvice.class);
			int resCount = getBodyAdviceCount(ResponseBodyAdvice.class);
			if (modelSize == 0 && binderSize == 0 && reqCount == 0 && resCount == 0) {
				logger.debug("ControllerAdvice beans: none");
			}
			else {
				logger.debug("ControllerAdvice beans: " + modelSize + " @ModelAttribute, " + binderSize +
						" @InitBinder, " + reqCount + " RequestBodyAdvice, " 
						+ resCount + " ResponseBodyAdvice");
			}
		}
	}

	// Count all advice, including explicit registrations..
	private int getBodyAdviceCount(Class<?> adviceType) {
		List<Object> advice = this.requestResponseBodyAdvice;
		return RequestBodyAdvice.class.isAssignableFrom(adviceType) ?
				RequestResponseBodyAdviceChain.getAdviceByType(advice, RequestBodyAdvice.class).size() :
				RequestResponseBodyAdviceChain.getAdviceByType(advice, ResponseBodyAdvice.class).size();
	}

	/**
	 * 返回缺省使用的参数解析器列表，包含内置的参数解析器，以及通过 #setCustomArgumentResolvers 指定的
     * 自定义参数解析器
     * Return the list of argument resolvers to use including built-in resolvers
	 * and custom resolvers provided via #setCustomArgumentResolvers.
	 */
	private List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

       // 基于注解的参数解析器 
		// Annotation-based argument resolution
		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
		resolvers.add(new RequestParamMapMethodArgumentResolver());
		resolvers.add(new PathVariableMethodArgumentResolver());
		resolvers.add(new PathVariableMapMethodArgumentResolver());
		resolvers.add(new MatrixVariableMethodArgumentResolver());
		resolvers.add(new MatrixVariableMapMethodArgumentResolver());
		resolvers.add(new ServletModelAttributeMethodProcessor(false));
		resolvers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(), 
			this.requestResponseBodyAdvice));
		resolvers.add(new RequestPartMethodArgumentResolver(getMessageConverters(), 
			this.requestResponseBodyAdvice));
		resolvers.add(new RequestHeaderMethodArgumentResolver(getBeanFactory()));
		resolvers.add(new RequestHeaderMapMethodArgumentResolver());
		resolvers.add(new ServletCookieValueMethodArgumentResolver(getBeanFactory()));
		resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));
		resolvers.add(new SessionAttributeMethodArgumentResolver());
		resolvers.add(new RequestAttributeMethodArgumentResolver());

       // 基于类型的参数解析器 
		// Type-based argument resolution
		resolvers.add(new ServletRequestMethodArgumentResolver());
		resolvers.add(new ServletResponseMethodArgumentResolver());
		resolvers.add(new HttpEntityMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));
		resolvers.add(new RedirectAttributesMethodArgumentResolver());
		resolvers.add(new ModelMethodProcessor());
		resolvers.add(new MapMethodProcessor());
		resolvers.add(new ErrorsMethodArgumentResolver());
		resolvers.add(new SessionStatusMethodArgumentResolver());
		resolvers.add(new UriComponentsBuilderMethodArgumentResolver());


       // 自定义参数解析器 
		// Custom arguments
		if (getCustomArgumentResolvers() != null) {
			resolvers.addAll(getCustomArgumentResolvers());
		}

		// Catch-all 兜底参数解析器
       // 下面两个参数解析器构造函数的第二个参数是 useDefaultResolution, 设置为 true， 表示 :
       // in default resolution mode a method argument
      // that is a simple type, as defined in BeanUtils#isSimpleProperty,
      // is treated as a request parameter even if it isn't annotated, the
      // request parameter name is derived from the method parameter name.
		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));
		resolvers.add(new ServletModelAttributeMethodProcessor(true));

		return resolvers;
	}

	/**
	 * 返回一组给 @InitBinder 方法适用的参数解析器，包含内置的一些参数解析器和
     * #setCustomArgumentResolvers 指定的自定义参数解析器
     * 
     * Return the list of argument resolvers to use for @InitBinder
	 * methods including built-in and custom resolvers.  
	 */
	private List<HandlerMethodArgumentResolver> getDefaultInitBinderArgumentResolvers() {
		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

		// Annotation-based argument resolution
		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
		resolvers.add(new RequestParamMapMethodArgumentResolver());
		resolvers.add(new PathVariableMethodArgumentResolver());
		resolvers.add(new PathVariableMapMethodArgumentResolver());
		resolvers.add(new MatrixVariableMethodArgumentResolver());
		resolvers.add(new MatrixVariableMapMethodArgumentResolver());
		resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));
		resolvers.add(new SessionAttributeMethodArgumentResolver());
		resolvers.add(new RequestAttributeMethodArgumentResolver());

       // 基于类型的参数解析器 
		// Type-based argument resolution
		resolvers.add(new ServletRequestMethodArgumentResolver());
		resolvers.add(new ServletResponseMethodArgumentResolver());

       // 自定义参数解析器 
		// Custom arguments
		if (getCustomArgumentResolvers() != null) {
			resolvers.addAll(getCustomArgumentResolvers());
		}

		// Catch-all
		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));

		return resolvers;
	}

	/**
	 * 返回缺省要使用的返回值处理器，包括内置的一组返回值处理器和通过方法 #setReturnValueHandlers
     * 设置的返回值解析器
     * Return the list of return value handlers to use including built-in and
	 * custom handlers provided via #setReturnValueHandlers.
	 */
	private List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
		List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();

		// Single-purpose return value types
		handlers.add(new ModelAndViewMethodReturnValueHandler());
		handlers.add(new ModelMethodProcessor());
		handlers.add(new ViewMethodReturnValueHandler());
		handlers.add(new ResponseBodyEmitterReturnValueHandler(getMessageConverters(),
				this.reactiveAdapterRegistry, this.taskExecutor, this.contentNegotiationManager));
		handlers.add(new StreamingResponseBodyReturnValueHandler());
		handlers.add(new HttpEntityMethodProcessor(getMessageConverters(),
				this.contentNegotiationManager, this.requestResponseBodyAdvice));
		handlers.add(new HttpHeadersReturnValueHandler());
		handlers.add(new CallableMethodReturnValueHandler());
		handlers.add(new DeferredResultMethodReturnValueHandler());
		handlers.add(new AsyncTaskMethodReturnValueHandler(this.beanFactory));

		// Annotation-based return value types
		handlers.add(new ModelAttributeMethodProcessor(false));
		handlers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(),
				this.contentNegotiationManager, this.requestResponseBodyAdvice));

		// Multi-purpose return value types
		handlers.add(new ViewNameMethodReturnValueHandler());
		handlers.add(new MapMethodProcessor());

		// Custom return value types
		if (getCustomReturnValueHandlers() != null) {
			handlers.addAll(getCustomReturnValueHandlers());
		}

		// Catch-all 兜底参数解析器   
		if (!CollectionUtils.isEmpty(getModelAndViewResolvers())) {
			handlers.add(new ModelAndViewResolverMethodReturnValueHandler(getModelAndViewResolvers()));
		}
		else {
			handlers.add(new ModelAttributeMethodProcessor(true));
		}

		return handlers;
	}


	/**
	 * Always return true since any method argument and return value
	 * type will be processed in some way. A method argument not recognized
	 * by any HandlerMethodArgumentResolver is interpreted as a request parameter
	 * if it is a simple type, or as a model attribute otherwise. A return value
	 * not recognized by any HandlerMethodReturnValueHandler will be interpreted
	 * as a model attribute.
	 */
	@Override
	protected boolean supportsInternal(HandlerMethod handlerMethod) {
		return true;
	}

    // 针对请求 request/response 执行目标控制器方法 handlerMethod，
    // 该方法主要是对基类定义的该抽象方法提供实现，最终目标控制器的方法调用又委托给了方法
    // invokeHandlerMethod,本方法主要流程是 :
    // 1. 目标控制器方法调用前做一些检查和准备工作:HTTP方法是否支持，session需要的话尝试创建,是否有会话同步要求
    // 2. invokeHandlerMethod 调用目标控制器方法
    // 3. 目标控制器调用之后做一些响应对象的处理: 针对HTTP缓存机制，设置相应的响应头部
	@Override
	protected ModelAndView handleInternal(HttpServletRequest request,HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

		ModelAndView mav;
       // 检查请求 :
       // 1. HTTP 方法是否被支持,如果方法不被支持则抛出异常
       // new HttpRequestMethodNotSupportedException(method, this.supportedMethods)
       // 2. 是否需要 session, 如果需要 session 但是创建失败则抛出异常 
       //  new HttpSessionRequiredException("Pre-existing session required but none found")
		checkRequest(request);

		// Execute invokeHandlerMethod in synchronized block if required.
		if (this.synchronizeOnSession) {
        // 请求处理被要求在会话session上同步执行的情况
			HttpSession session = request.getSession(false);
			if (session != null) {
				Object mutex = WebUtils.getSessionMutex(session);
				synchronized (mutex) {
                // 调用目标控制方法,返回 ModelAndView mav
					mav = invokeHandlerMethod(request, response, handlerMethod);
				}
			}
			else {
				// No HttpSession available -> no mutex necessary
                // 调用目标控制方法,返回 ModelAndView mav
				mav = invokeHandlerMethod(request, response, handlerMethod);
			}
		}
		else {
			// No synchronization on session demanded at all...
            // 调用目标控制方法,返回 ModelAndView mav
			mav = invokeHandlerMethod(request, response, handlerMethod);
		}

       // 针对 HTTP 缓存机制对响应对象进行处理 
		if (!response.containsHeader(HEADER_CACHE_CONTROL)) {
			if (getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
				applyCacheSeconds(response, this.cacheSecondsForSessionAttributeHandlers);
			}
			else {
				prepareResponse(response);
			}
		}

		// 返回调用目标控制器方法所得到的 ModelAndView mav
		return mav;
	}

	/**
	 * This implementation always returns -1. An @RequestMapping method can
	 * calculate the lastModified value, call WebRequest#checkNotModified(long),
	 * and return null if the result of that call is true.
     * 框架内部方法，返回请求中的 Last-Modified 头部属性，这里的实现总是返回 -1。
     * 控制器方法如果想计算 lastModified 值，可以调用 WebRequest#checkNotModified(long),
     * 如果该方法返回true，则控制器方法可以返回null，表明文档没有发生改变。
	 */
	@Override
	protected long getLastModifiedInternal(HttpServletRequest request, HandlerMethod handlerMethod) {
		return -1;
	}
	/**
	 * Return the SessionAttributesHandler instance for the given handler type
	 * (never null).
	 */
	private SessionAttributesHandler getSessionAttributesHandler(HandlerMethod handlerMethod) {
		Class<?> handlerType = handlerMethod.getBeanType();
		SessionAttributesHandler sessionAttrHandler = this.sessionAttributesHandlerCache.get(handlerType);
		if (sessionAttrHandler == null) {
			synchronized (this.sessionAttributesHandlerCache) {
				sessionAttrHandler = this.sessionAttributesHandlerCache.get(handlerType);
				if (sessionAttrHandler == null) {
					sessionAttrHandler = new SessionAttributesHandler(handlerType, this.sessionAttributeStore);
					this.sessionAttributesHandlerCache.put(handlerType, sessionAttrHandler);
				}
			}
		}
		return sessionAttrHandler;
	}

	/**
	 * Invoke the RequestMapping handler method preparing a ModelAndView if view resolution is required.
     * 调用 @RequestMapping 注解的目标控制器方法，如果需要视图解析的话准备一个 ModelAndView对象并返回
	 * @since 4.2
	 * @see #createInvocableHandlerMethod(HandlerMethod)
	 */
	@Nullable
	protected ModelAndView invokeHandlerMethod(HttpServletRequest request,HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        // 将请求/响应对象包装成一个 ServletWebRequest 对象,
        // 最终的目标控制器方法调用使用该包装对象进行
		ServletWebRequest webRequest = new ServletWebRequest(request, response);
		try {
			WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);
			ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);
          // 将目标控制器方法包装成一个  ServletInvocableHandlerMethod,最终的目标控制器方法调用将使用该对象进行，
          // ServletInvocableHandlerMethod 类具备从请求上下文解析控制器方法参数，以及处理控制器方法返回值的能力,而参数 HandlerMethod handlerMethod 不具备这个能力
			ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
			if (this.argumentResolvers != null) {
                // 设置控制器方法参数解析器
				invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
			}
			if (this.returnValueHandlers != null) {
            // 设置控制器方法返回值处理器
				invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
			}
			invocableMethod.setDataBinderFactory(binderFactory);
            //设置控制器方法参数名称发现器
			invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
            //准备 ModelAndView 容器，用于接收目标控制器方法执行获得的  ModelAndView 信息  
			ModelAndViewContainer mavContainer = new ModelAndViewContainer();
			mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
			modelFactory.initModel(webRequest, mavContainer, invocableMethod);
			mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);

            //关于异步请求的处理  
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

           // 调用目标控制器方法 
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

	/**
	 * Create a ServletInvocableHandlerMethod from the given HandlerMethod definition.
    * 将指定控制器方法包装成一个  ServletInvocableHandlerMethod ，最终对该控制器方法调用
    * 将使用该 ServletInvocableHandlerMethod 对象进行，  ServletInvocableHandlerMethod 类
    * 具备从请求上下文解析控制器方法参数，以及处理控制器方法返回值的能力，而 HandlerMethod 
    * 类不具备这个能力
	 * @param handlerMethod the HandlerMethod definition
	 * @return the corresponding ServletInvocableHandlerMethod (or custom subclass thereof)
	 * @since 4.2
	 */
	protected ServletInvocableHandlerMethod createInvocableHandlerMethod(HandlerMethod handlerMethod) {
		return new ServletInvocableHandlerMethod(handlerMethod);
	}

	private ModelFactory getModelFactory(HandlerMethod handlerMethod, WebDataBinderFactory binderFactory) {
		SessionAttributesHandler sessionAttrHandler = getSessionAttributesHandler(handlerMethod);
		Class<?> handlerType = handlerMethod.getBeanType();
		Set<Method> methods = this.modelAttributeCache.get(handlerType);
		if (methods == null) {
			methods = MethodIntrospector.selectMethods(handlerType, MODEL_ATTRIBUTE_METHODS);
			this.modelAttributeCache.put(handlerType, methods);
		}
		List<InvocableHandlerMethod> attrMethods = new ArrayList<>();
		// Global methods first
		this.modelAttributeAdviceCache.forEach((clazz, methodSet) -> {
			if (clazz.isApplicableToBeanType(handlerType)) {
				Object bean = clazz.resolveBean();
				for (Method method : methodSet) {
					attrMethods.add(createModelAttributeMethod(binderFactory, bean, method));
				}
			}
		});
		for (Method method : methods) {
			Object bean = handlerMethod.getBean();
			attrMethods.add(createModelAttributeMethod(binderFactory, bean, method));
		}
		return new ModelFactory(attrMethods, binderFactory, sessionAttrHandler);
	}

	private InvocableHandlerMethod createModelAttributeMethod(WebDataBinderFactory factory, 
        Object bean, Method method) {
		InvocableHandlerMethod attrMethod = new InvocableHandlerMethod(bean, method);
		if (this.argumentResolvers != null) {
			attrMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
		}
		attrMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
		attrMethod.setDataBinderFactory(factory);
		return attrMethod;
	}

	private WebDataBinderFactory getDataBinderFactory(HandlerMethod handlerMethod) throws Exception {
		Class<?> handlerType = handlerMethod.getBeanType();
		Set<Method> methods = this.initBinderCache.get(handlerType);
		if (methods == null) {
			// 找到控制器类上所有使用了注解 @InitBinder 的方法
			methods = MethodIntrospector.selectMethods(handlerType, INIT_BINDER_METHODS);
			this.initBinderCache.put(handlerType, methods);
		}
		List<InvocableHandlerMethod> initBinderMethods = new ArrayList<>();
		// Global methods first
		this.initBinderAdviceCache.forEach((clazz, methodSet) -> {
			if (clazz.isApplicableToBeanType(handlerType)) {
				Object bean = clazz.resolveBean();
				for (Method method : methodSet) {
					initBinderMethods.add(createInitBinderMethod(bean, method));
				}
			}
		});
		for (Method method : methods) {
			Object bean = handlerMethod.getBean();
			initBinderMethods.add(createInitBinderMethod(bean, method));
		}
		return createDataBinderFactory(initBinderMethods);
	}

	private InvocableHandlerMethod createInitBinderMethod(Object bean, Method method) {
		InvocableHandlerMethod binderMethod = new InvocableHandlerMethod(bean, method);
		if (this.initBinderArgumentResolvers != null) {
			binderMethod.setHandlerMethodArgumentResolvers(this.initBinderArgumentResolvers);
		}
		binderMethod.setDataBinderFactory(new DefaultDataBinderFactory(this.webBindingInitializer));
		binderMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
		return binderMethod;
	}

	/**
	 * Template method to create a new InitBinderDataBinderFactory instance.
	 * The default implementation creates a ServletRequestDataBinderFactory.
	 * This can be overridden for custom ServletRequestDataBinder subclasses.
	 * @param binderMethods @InitBinder methods
	 * @return the InitBinderDataBinderFactory instance to use
	 * @throws Exception in case of invalid state or arguments
	 */
	protected InitBinderDataBinderFactory createDataBinderFactory(List<InvocableHandlerMethod> binderMethods)
			throws Exception {

		return new ServletRequestDataBinderFactory(binderMethods, getWebBindingInitializer());
	}

    // 从 ModelAndViewContainer 中提取 ModelAndView 信息
	@Nullable
	private ModelAndView getModelAndView(ModelAndViewContainer mavContainer,
			ModelFactory modelFactory, NativeWebRequest webRequest) throws Exception {

		modelFactory.updateModel(webRequest, mavContainer);
		if (mavContainer.isRequestHandled()) {
           // 如果请求已经被处理则返回 null 
			return null;
		}
        
        // 从 mavContainer 获取 model 和 view 信息构建 ModelAndView 对象，
        // 最终返回给调用者
		ModelMap model = mavContainer.getModel();
		ModelAndView mav = new ModelAndView(mavContainer.getViewName(), model, mavContainer.getStatus());
		if (!mavContainer.isViewReference()) {
			mav.setView((View) mavContainer.getView());
		}
		if (model instanceof RedirectAttributes) {
			Map<String, ?> flashAttributes = ((RedirectAttributes) model).getFlashAttributes();
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				RequestContextUtils.getOutputFlashMap(request).putAll(flashAttributes);
			}
		}
		return mav;
	}
}
```
