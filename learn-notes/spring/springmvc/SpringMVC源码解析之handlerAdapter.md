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

