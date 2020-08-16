### HandlerAdapter

####  SimpleControllerHandlerAdapter //直接处理 HttpServletRequest , HttpServletResponse 

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


####  HttpRequestHandlerAdapter(适配org.springframework.web.HttpRequestHandler这种Handler)

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

#### SimpleServletHandlerAdapter(Spring MVC默认并不向容器注册这种HandlerAdapter，若需要使用是需要调用者手动给注册这个Bean，Servlet这种Handler才能正常使用)

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


### HandlerMethod

#### public class InvocableHandlerMethod extends HandlerMethod

````

private WebDataBinderFactory dataBinderFactory;

// 一组控制器方法参数解析器，用于从请求上下文信息中解析控制器方法参数对应的参数值
private HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();

// 用于发现控制器方法参数的名称，参数名称在从请求上下文信息中解析控制器方法参数值时会被使用
private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

//基于一个HandlerMethod 构造一个InvocableHandlerMethod 对象,通常用在请求处理过程中，根据请求信息找到了相应的hadnlerMethod,然后准备调用底层的控制器方法了,
//此时调用该构造函数形成一个 InvocableHandlerMethod实例,然后传递其他必要的参数给该InvocableHandlerMethod实例。然后执行目标控制器方法的调用。
public InvocableHandlerMethod(HandlerMethod handlerMethod) {
		super(handlerMethod);
}

//基于Web控制器对象和相应的控制器方法构造一个InvocableHandlerMethod对象
public InvocableHandlerMethod(Object bean, Method method) {
		super(bean, method);
}

public Object invokeForRequest(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {
        // 从请求上下文中解析控制器方法参数对应的参数值
		Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
		if (logger.isTraceEnabled()) {
			logger.trace("Arguments: " + Arrays.toString(args));
		}
		//执行目标控制器方法调用
		return doInvoke(args);
}

protected Object[] getMethodArgumentValues(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {

		MethodParameter[] parameters = getMethodParameters();
		if (ObjectUtils.isEmpty(parameters)) {
		    //控制器方法没有参数的情况
			return EMPTY_ARGS;
		}

		Object[] args = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
		    //获取一个控制器方法参数
			MethodParameter parameter = parameters[i];
			//发现控制方法参数的名称
			parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
			//看看针对该参数，有没有通过provideArgs 提供的参数值
			args[i] = findProvidedArgument(parameter, providedArgs);
			if (args[i] != null) {
				continue;
			}
			// 如果针对该参数，没有通过 providedArgs 提供的参数值，则尝试使用属性 resolvers,其实是一组 HandlerMethodArgumentResolver 实例的一个组合对象，从请求上下文中解析该参数的参数值
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

````

##### public class ServletInvocableHandlerMethod extends InvocableHandlerMethod 
 
````
ServletInvocableHandlerMethod 这个类在HandlerAdapter对每个请求处理过程中，都会实例化一个出来(上面提到的属性由HandlerAdapter进行设置)，分别对请求和返回进行处理

private HandlerMethodReturnValueHandlerComposite returnValueHandlers;


public void invokeAndHandle(ServletWebRequest webRequest, ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {
        
        // 从请求上下文中解析出控制器方法参数并调用目标控制器方法。方法invokeForRequest 定义在基类中
		Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);
		//根据 控制器安方法注解@RequestStatus 中响应状态码信息设置响应状态码。如果没有使用注解 @ResponseStatus，可以将该方法理解为什么都没做
		setResponseStatus(webRequest);
        //如果控制器方法执行返回值为null,则综合考虑其他信息考虑是否将请求标记为处理完成
		if (returnValue == null) {
			if (isRequestNotModified(webRequest) || getResponseStatus() != null || mavContainer.isRequestHandled()) {
				disableContentCachingIfNecessary(webRequest);
				mavContainer.setRequestHandled(true);
				return;
			}
		}
		//即使返回值不为null,但@ResponseStatus注解属性 reason被设置，则也将请求处理标记为已经完成
		else if (StringUtils.hasText(getResponseStatusReason())) {
			mavContainer.setRequestHandled(true);
			return;
		}

		mavContainer.setRequestHandled(false);
		Assert.state(this.returnValueHandlers != null, "No return value handlers");
		try {
		    //使用返回值处理器处理控制器方法执行得到的返回值
			this.returnValueHandlers.handleReturnValue(returnValue, getReturnValueType(returnValue), mavContainer, webRequest);
		}
		catch (Exception ex) {
			if (logger.isTraceEnabled()) {
				logger.trace(formatErrorForReturnValue(returnValue), ex);
			}
			throw ex;
		}
	}
	
	// 根据控制器方法注解 @ResponseStatus 中响应状态信息码信息设置状态响应状态码，如果没有使用注解 @ResponseStatus，可以将该方法理解为什么都没做
	private void setResponseStatus(ServletWebRequest webRequest) throws IOException {
	        //获取控制器方法注解@ResponseStatus的信息，当然也可能没有使用该注解
    		HttpStatus status = getResponseStatus();
    		if (status == null) {
    		// 如果没有使用注解@ResponseStatus,则直接返回
    			return;
    		}
             // 如果注解@ResponseStatus 属性 reason 未被设置(null 或者 "") ，则调用 response.sendError
             // 如果注解 @ResponseStatus 属性 reason被设置（有值的字符串），则调用response.sednStatus
    		HttpServletResponse response = webRequest.getResponse();
    		if (response != null) {
    			String reason = getResponseStatusReason();
    			if (StringUtils.hasText(reason)) {
    				response.sendError(status.value(), reason);
    			}
    			else {
    				response.setStatus(status.value());
    			}
    		}
    
    		// To be picked up by RedirectView
    		webRequest.getRequest().setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, status);
    	}

````