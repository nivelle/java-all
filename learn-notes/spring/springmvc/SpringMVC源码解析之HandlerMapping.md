#### handlerMapping

````
handlerMapping 负责处理用户Url请求地址和处理类Handler的对应关系，在HandlerMapping接口中定义了根据一个URL必须返回一个由HandlerExecutionChain代表的处理链，我们可以在这个处理链中添加任意的HandlerAdapter实例来处理这个URL对应的请求

public interface HandlerMapping {
	//@since 4.3.21
	String BEST_MATCHING_HANDLER_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingHandler";
	
	String PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE = HandlerMapping.class.getName() + ".pathWithinHandlerMapping";
	String BEST_MATCHING_PATTERN_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingPattern";
	String INTROSPECT_TYPE_LEVEL_MAPPING = HandlerMapping.class.getName() + ".introspectTypeLevelMapping";
	String URI_TEMPLATE_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".uriTemplateVariables";
	String MATRIX_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".matrixVariables";
	String PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE = HandlerMapping.class.getName() + ".producibleMediaTypes";

	@Nullable
	HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;
}


````

#### 实现(一) public interface MatchableHandlerMapping extends HandlerMapping

```
public interface MatchableHandlerMapping extends HandlerMapping {

	/**
	 * Determine whether the given request matches the request criteria.
	 * @param request the current request
	 * @param pattern the pattern to match
	 * @return the result from request matching, or {@code null} if none
	 */
	@Nullable
	RequestMatchResult match(HttpServletRequest request, String pattern);//确定给定的请求是否符合请求条件  pattern：模版

}
```

#### 实现(二) public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport implements HandlerMapping, Ordered, BeanNameAware 

````
public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport implements HandlerMapping, Ordered, BeanNameAware {

	// the default handler for this handler mapping
	@Nullable
	private Object defaultHandler;
	// url路径计算的辅助类、工具类
	private UrlPathHelper urlPathHelper = new UrlPathHelper();
	// Ant风格的Path匹配模式,pathVariable 参数处理工具类
	private PathMatcher pathMatcher = new AntPathMatcher();
	// 拦截器集合
	private final List<Object> interceptors = new ArrayList<>();
	// 从 interceptors 中解析得到,直接添加给全部 handler
	private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<>();
	// 跨域相关的配置
	private CorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
	private CorsProcessor corsProcessor = new DefaultCorsProcessor();
	// 最低的顺序（default: same as non-Ordered）
	private int order = Ordered.LOWEST_PRECEDENCE;
	@Nullable
	private String beanName;
	
	// Set the interceptors to apply for all handlers mapped by this handler mapping
	// 为此 mapping 对应的 handlers 设置拦截器
	public void setInterceptors(Object... interceptors) {
		this.interceptors.addAll(Arrays.asList(interceptors));
	}
	// Configure a custom {@link CorsProcessor} to use to apply the matched
	// @since 4.2
	public void setCorsProcessor(CorsProcessor corsProcessor) {
		Assert.notNull(corsProcessor, "CorsProcessor must not be null");
		this.corsProcessor = corsProcessor;
	}
	@Override
	protected void initApplicationContext() throws BeansException {
		// 给子类扩展: 增加拦截器，默认为空实现
		extendInterceptors(this.interceptors);
		// 找到所有 MappedInterceptor 拦截器类型的bean添加到adaptedInterceptors中
		detectMappedInterceptors(this.adaptedInterceptors);
		// 将 interceptors 中的拦截器取出放入 adaptedInterceptors
		// 如果是 WebRequestInterceptor 类型的拦截器  需要用WebRequestHandlerInterceptorAdapter进行包装适配
		initInterceptors();
	}
	// 去容器（含祖孙容器）内找到所有的MappedInterceptor类型的拦截器出来，添加进去  非单例的Bean也包含
	// 备注MappedInterceptor 为Spring MVC拦截器接口`HandlerInterceptor`的实现类  并且是个final类 Spring3.0后出来的。
	protected void detectMappedInterceptors(List<HandlerInterceptor> mappedInterceptors) {
		mappedInterceptors.addAll(BeanFactoryUtils.beansOfTypeIncludingAncestors(obtainApplicationContext(), MappedInterceptor.class, true, false).values());
	}
	// 它就是把调用者放进来的interceptors，适配成HandlerInterceptor然后统一放在`adaptedInterceptors`里面装着~~~
	protected void initInterceptors() {
		if (!this.interceptors.isEmpty()) {
			for (int i = 0; i < this.interceptors.size(); i++) {
				Object interceptor = this.interceptors.get(i);
				if (interceptor == null) {
					throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
				}
				this.adaptedInterceptors.add(adaptInterceptor(interceptor));
			}
		}
	}
	// 适配: 支持源生的HandlerInterceptor以及WebRequestInterceptor
	protected HandlerInterceptor adaptInterceptor(Object interceptor) {
		if (interceptor instanceof HandlerInterceptor) {
			return (HandlerInterceptor) interceptor;
		} else if (interceptor instanceof WebRequestInterceptor) {
			// WebRequestHandlerInterceptorAdapter它就是个`HandlerInterceptor`，内部持有一个WebRequestInterceptor的引用而已
			// 内部使用到了DispatcherServletWebRequest包request和response包装成`WebRequest`等等
			return new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor) interceptor);
		} else {
			throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
		}
	}

	// 这个方法也是一个该抽象类提供的一个非常重要的模版方法：根据request获取到一个HandlerExecutionChain
	@Override
	@Nullable
	public final HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		// 根据request获取对应的handler 抽象方法，由具体的子类去实现
		Object handler = getHandlerInternal(request);
		// 若没有匹配上处理器，那就走默认的处理器，默认的处理器也是需要由子类给赋值  否则也会null的
		if (handler == null) {
			handler = getDefaultHandler();
		}
		if (handler == null) {
			return null;
		}
		// 意思是如果是个String类型的名称，那就去容器内找这个Bean，当作一个Handler
		if (handler instanceof String) {
			String handlerName = (String) handler;
			handler = obtainApplicationContext().getBean(handlerName);
		}
		// 关键步骤: 根据handler和request构造一个请求处理链
		HandlerExecutionChain executionChain = getHandlerExecutionChain(handler, request);
		return executionChain;
	}
	// 包装 handler 为HandlerExecutionChain,将拦截器设置进来。
	protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
		HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ? (HandlerExecutionChain) handler : new HandlerExecutionChain(handler));
		// 此处就用到了urlPathHelper来解析request,获取项目名之后的请求地址
		String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
		for (HandlerInterceptor interceptor : this.adaptedInterceptors) {
			if (interceptor instanceof MappedInterceptor) {
				// 这里其实就能体现出MappedInterceptor的些许优势了：也就是它只有路径匹配上了才会拦截，没有匹配上的就不会拦截了备注：MappedInterceptor可以设置includePatterns和excludePatterns等
				MappedInterceptor mappedInterceptor = (MappedInterceptor) interceptor;
				if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
					chain.addInterceptor(mappedInterceptor.getInterceptor());
				}
			} else {
				chain.addInterceptor(interceptor);
			}
		}
		return chain;
	}
}

````

-  子类: public abstract class AbstractUrlHandlerMapping extends AbstractHandlerMapping implements MatchableHandlerMapping //将url对应的Handler保存在一个Map中，在getHandlerInternal方法中使用url从Map中获取Handler

```
public abstract class AbstractUrlHandlerMapping extends AbstractHandlerMapping implements MatchableHandlerMapping {
	
	@Nullable
	private Object rootHandler;// 根路径 / 的处理器
	
	// 尾部是否使用斜线/匹配   如果为true  那么`/users`它也会匹配上`/users/`  默认是false的
	private boolean useTrailingSlashMatch = false;
	
	// 设置是否延迟初始化handler。仅适用于单实例handler，默认是false表示立即实例化
	private boolean lazyInitHandlers = false;
	
	// 这个Map就是缓存下，URL对应的Handler（注意这里只是handler，而不是chain）
	private final Map<String, Object> handlerMap = new LinkedHashMap<>();

	// 这个就是父类留给子类实现的抽象方法，此抽象类相当于进行了进一步的模版实现。
	@Override
	@Nullable
	protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
		// 找到URL的后半段,由此可见Spring MVC处理URL路径匹配都是从工程名后面开始匹配的
		String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);		
		// 根据url查找handler: 
		// (1) 先去handlerMap里找，若找到了那就实例化它，并且给chain里加入一个拦截器：`PathExposingHandlerInterceptor` 它是个private私有类的HandlerInterceptor		
		// (2) 否则就使用 PathMatcher 去匹配URL，存在匹配上多个路径的问题若匹配上多个路径了，就按照PathMatcher的排序规则排序，取值get(0)
		// 请注意：这里默认的两个拦截器每次都是new出来的和Handler可议说是绑定的，所以不会存在线程安全问题
		Object handler = lookupHandler(lookupPath, request);
		// 若没找到：
		if (handler == null) {
			// 处理跟路径 / 和默认的Handler
			Object rawHandler = null;
			if ("/".equals(lookupPath)) {
				rawHandler = getRootHandler();
			}
			if (rawHandler == null) {
				rawHandler = getDefaultHandler();
			}
			if (rawHandler != null) {
				if (rawHandler instanceof String) {
					String handlerName = (String) rawHandler;
					rawHandler = obtainApplicationContext().getBean(handlerName);
				}
				validateHandler(rawHandler, request);
				// 然后把rawHandler转换成chain（这个时候chain里面可能已经有两个拦截器了，然后父类还会继续把用户自定义的拦截器放上去）
				handler = buildPathExposingHandler(rawHandler, lookupPath, lookupPath, null);
			}
		}
		return handler;
	}

	// 向handlerMap里面put值的唯一入口
	protected void registerHandler(String[] urlPaths, String beanName) throws BeansException, IllegalStateException {
		Assert.notNull(urlPaths, "URL path array must not be null");
		for (String urlPath : urlPaths) {
			registerHandler(urlPath, beanName);
		}
	}
	//向 handlerMapping 中添加 key=url value=handler
	protected void registerHandler(String urlPath, Object handler) throws BeansException, IllegalStateException {
		Assert.notNull(urlPath, "URL path must not be null");
		Assert.notNull(handler, "Handler object must not be null");
		Object resolvedHandler = handler;
		// 如果是beanName，而且非懒加载
		if (!this.lazyInitHandlers && handler instanceof String) {
			String handlerName = (String) handler;
			ApplicationContext applicationContext = obtainApplicationContext();
			// 如果是单例，那么直接从上下文中获取已经是实例化的
			if (applicationContext.isSingleton(handlerName)) {
				resolvedHandler = applicationContext.getBean(handlerName);
			}
		}
		// 先尝试通过 urlPath从 Map中去获取 handler
		Object mappedHandler = this.handlerMap.get(urlPath);
		if (mappedHandler != null) {
			// 这个异常错误信息，相信我们在开发中经常碰到吧：简单就是说就是一个URL只能映射到一个Handler上（但是一个Handler是可以处理多个URL的，这个需要注意）
			if (mappedHandler != resolvedHandler) {
				throw new IllegalStateException("Cannot map " + getHandlerDescription(handler) + " to URL path [" + urlPath +"]: There is already " + getHandlerDescription(mappedHandler) + " mapped.");
			}
		} else {
			// 如果你的handler处理的路径是根路径
			if (urlPath.equals("/")) {
				setRootHandler(resolvedHandler);
			}
			// 这个路径相当于处理所有  优先级是最低的  所以当作默认的处理器来使用
			else if (urlPath.equals("/*")) {
				setDefaultHandler(resolvedHandler);
			}
			else {
				this.handlerMap.put(urlPath, resolvedHandler);
				if (logger.isTraceEnabled()) {
					logger.trace("Mapped [" + urlPath + "] onto " + getHandlerDescription(handler));
				}
			}
		}
	}

	// 该缓存也提供了一个只读视图给调用者访问
	public final Map<String, Object> getHandlerMap() {
		return Collections.unmodifiableMap(this.handlerMap);
	}

}

```

- BeanNameUrlHandlerMapping //是 AbstractDetectingUrlHandlerMapping extends AbstractUrlHandlerMapping 的唯一实现类

```
public class BeanNameUrlHandlerMapping extends AbstractDetectingUrlHandlerMapping {

	@Override
	protected String[] determineUrlsForHandler(String beanName) {
		List<String> urls = new ArrayList<>();
		// 意思就是必须以/开头
		if (beanName.startsWith("/")) {
			urls.add(beanName);
		}
		// 别名
		String[] aliases = obtainApplicationContext().getAliases(beanName);
		for (String alias : aliases) {
			if (alias.startsWith("/")) {
				urls.add(alias);
			}
		}
		return StringUtils.toStringArray(urls);
	}
}

```

- public class SimpleUrlHandlerMapping extends AbstractUrlHandlerMapping

```
public class SimpleUrlHandlerMapping extends AbstractUrlHandlerMapping {
	private final Map<String, Object> urlMap = new LinkedHashMap<>();

	public void setMappings(Properties mappings) {
		CollectionUtils.mergePropertiesIntoMap(mappings, this.urlMap);
	}
	public void setUrlMap(Map<String, ?> urlMap) {
		this.urlMap.putAll(urlMap);
	}

	@Override
	public void initApplicationContext() throws BeansException {
		super.initApplicationContext();
		registerHandlers(this.urlMap);
	}
	// 这个实现简单到令人发指
	protected void registerHandlers(Map<String, Object> urlMap) throws BeansException {
		if (urlMap.isEmpty()) {
			logger.trace("No patterns in " + formatMappingName());
		} else {
			urlMap.forEach((url, handler) -> {
				// 如果还没有斜线，在前面加上斜线
				if (!url.startsWith("/")) {
					url = "/" + url;
				}
				if (handler instanceof String) {
					handler = ((String) handler).trim();
				}
				registerHandler(url, handler);
			});
		}
	}
}


```
##### public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean

````
// @since 3.1  Spring3.1之后才出现，这个时候注解驱动也出来了
// 实现了initializingBean接口，其实主要的注册操作则是通过afterPropertiesSet()接口方法来调用的
// 它是带有泛型T的。T：包含HandlerMethod与传入请求匹配所需条件的handlerMethod的映射
public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean {
	// SCOPED_TARGET的BeanName的前缀
	private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";
	private static final HandlerMethod PREFLIGHT_AMBIGUOUS_MATCH = new HandlerMethod(new EmptyHandler(), ClassUtils.getMethod(EmptyHandler.class, "handle"));
	// 跨域相关
	private static final CorsConfiguration ALLOW_CORS_CONFIG = new CorsConfiguration();
	static {
		ALLOW_CORS_CONFIG.addAllowedOrigin("*");
		ALLOW_CORS_CONFIG.addAllowedMethod("*");
		ALLOW_CORS_CONFIG.addAllowedHeader("*");
		ALLOW_CORS_CONFIG.setAllowCredentials(true);
	}
	
	// 默认不会去祖先容器里面找Handlers
	private boolean detectHandlerMethodsInAncestorContexts = false;
	// @since 4.1提供的新接口
	// 为处HandlerMetho的映射分配名称的策略接口   只有一个方法getName()
	// 唯一实现为：RequestMappingInfoHandlerMethodMappingNamingStrategy
	// 策略为：@RequestMapping指定了name属性，那就以指定的为准  否则策略为：取出Controller所有的`大写字母` + # + method.getName()
	// 如：AppoloController#match方法  最终的name为：AC#match 
	// 当然这个你也可以自己实现这个接口，然后set进来即可（只是一般没啥必要这么去干~~）
	@Nullable
	private HandlerMethodMappingNamingStrategy<T> namingStrategy;
	// 内部类：负责注册
	private final MappingRegistry mappingRegistry = new MappingRegistry();

	// 此处细节：使用的是读写锁  比如此处使用的是读锁   获得所有的注册进去的Handler的Map
	public Map<T, HandlerMethod> getHandlerMethods() {
		this.mappingRegistry.acquireReadLock();
		try {
			return Collections.unmodifiableMap(this.mappingRegistry.getMappings());
		} finally {
			this.mappingRegistry.releaseReadLock();
		}
	}
	// 此处是根据mappingName来获取一个Handler  此处需要注意哦
	@Nullable
	public List<HandlerMethod> getHandlerMethodsForMappingName(String mappingName) {
		return this.mappingRegistry.getHandlerMethodsByMappingName(mappingName);
	}
	// 最终都是委托给mappingRegistry去做了注册的工作，此处日志级别为trace级别
	public void registerMapping(T mapping, Object handler, Method method) {
		if (logger.isTraceEnabled()) {
			logger.trace("Register \"" + mapping + "\" to " + method.toGenericString());
		}
		this.mappingRegistry.register(mapping, handler, method);
	}
	public void unregisterMapping(T mapping) {
		if (logger.isTraceEnabled()) {
			logger.trace("Unregister mapping \"" + mapping + "\"");
		}
		this.mappingRegistry.unregister(mapping);
	}

	// 初始化HandlerMethods的入口
	@Override
	public void afterPropertiesSet() {
		initHandlerMethods();
	}
	// 看initHandlerMethods()，观察是如何实现加载HandlerMethod
	protected void initHandlerMethods() {
		// getCandidateBeanNames：Object.class相当于拿到当前容器（一般都是当前容器） 内所有的Bean定义信息
		// 如果阁下容器隔离到到的话，这里一般只会拿到@Controller标注的web组件  以及其它相关web组件的  不会非常的多的~~~~
		for (String beanName : getCandidateBeanNames()) {
			// BeanName不是以这个打头得  这里才会process这个BeanName~~~~
			if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
				// 会在每个Bean里面找处理方法，HandlerMethod，然后注册进去
				processCandidateBean(beanName);
			}
		}
		// 略：它就是输出一句日志：debug日志或者trace日志   `7 mappings in 'requestMappingHandlerMapping'`
		handlerMethodsInitialized(getHandlerMethods());
	}

	// 确定指定的候选bean的类型，如果标识为Handler类型，则调用DetectHandlerMethods
	// isHandler(beanType):判断这个type是否为Handler类型   它是个抽象方法，由子类去决定到底啥才叫Handler~~~~
	// `RequestMappingHandlerMapping`的判断依据为：该类上标注了@Controller注解或者@Controller注解  就算作是一个Handler
	// 所以此处：@Controller起到了一个特殊的作用，不能等价于@Component的哟~~~~
	protected void processCandidateBean(String beanName) {
		Class<?> beanType = null;
		try {
			beanType = obtainApplicationContext().getType(beanName);
		} catch (Throwable ex) {
			// 即使抛出异常  程序也不会终止~
		}
		if (beanType != null && isHandler(beanType)) {
			// 这个和我们上篇博文讲述的类似，都属于detect探测系列~~~~
			detectHandlerMethods(beanName);
		}
	}

	// 在指定的Handler的bean中查找处理程序方法Methods  找打就注册进去：mappingRegistry
	protected void detectHandlerMethods(Object handler) {
		Class<?> handlerType = (handler instanceof String ?obtainApplicationContext().getType((String) handler) : handler.getClass());
		if (handlerType != null) {
			Class<?> userType = ClassUtils.getUserClass(handlerType);
			// 又是非常熟悉的方法：MethodIntrospector.selectMethods
			// 它在我们招@EventListener、@Scheduled等注解方法时已经遇到过多次
			// 此处特别之处在于：getMappingForMethod 属于一个抽象方法，由子类去决定它的寻找规则：什么才算作一个处理器方法
			Map<Method, T> methods = MethodIntrospector.selectMethods(userType,
					(MethodIntrospector.MetadataLookup<T>) method -> {
						try {
							return getMappingForMethod(method, userType);
						} catch (Throwable ex) {
							throw new IllegalStateException("Invalid mapping on handler class [" + userType.getName() + "]: " + method, ex);
						}
					});
			
			// 把找到的Method  遍历后注册进去
			methods.forEach((method, mapping) -> {
				// 找到这个可调用的方法（AopUtils.selectInvocableMethod）
				Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
				registerHandlerMethod(handler, invocableMethod, mapping);
			});
		}
	}
}


````