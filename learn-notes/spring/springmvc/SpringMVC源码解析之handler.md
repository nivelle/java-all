
### HandlerMethod

#### 一个HandlerMethod对象,包装了以下信息：

- Object bean: Web控制器方法所在的Web控制器bean. 可以是字符串，代表bean 的名称，也可以是bean实例对象本身

- Class beanType: Web控制器方法所在的Web控制器bean的类型，如果该bean被代理，这里记录的是被代理的用户类信息

- Method method: Web控制器方法

- Method bridgedMethod: 被桥接的Web控制器方法

- MethodParameter[] parameter :Web控制器方法的参数信息，所在类所在方法，参数，索引，参数类型

- HttpStatus responseStatus: 注解@ResponseStatus 的code 属性

- String responseStatusReason: 注解@ResponseStatus 的 reason 属性

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

// 真正调用handler方法(反射)
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


### HandlerExecutionChain

#### boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response)
```
boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
			for (int i = 0; i < interceptors.length; i++) {
				HandlerInterceptor interceptor = interceptors[i];
				//如果某个拦截器调用失败则返回调用失败,同时调用 triggerAfterCompletion
				if (!interceptor.preHandle(request, response, this.handler)) {
					triggerAfterCompletion(request, response, null);
					return false;
				}
				this.interceptorIndex = i;
			}
		}
		return true;
	}
	
```

#### void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv)

```
void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv)
			throws Exception {

		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
			for (int i = interceptors.length - 1; i >= 0; i--) {
				HandlerInterceptor interceptor = interceptors[i];
				interceptor.postHandle(request, response, this.handler, mv);
			}
		}
	}

```

#### void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex)

```
void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex)throws Exception {

		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
		    //只处理 interceptor.preHandle(request, response, this.handler) 返回true的拦截器
			for (int i = this.interceptorIndex; i >= 0; i--) {
				HandlerInterceptor interceptor = interceptors[i];
				try {
					interceptor.afterCompletion(request, response, this.handler, ex);
				}
				catch (Throwable ex2) {
					logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
				}
			}
		}
	}
```