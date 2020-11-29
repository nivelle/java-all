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

- public class BeanNameUrlHandlerMapping extends AbstractDetectingUrlHandlerMapping  //是 AbstractDetectingUrlHandlerMapping extends AbstractUrlHandlerMapping 的唯一实现类

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
	// SCOPED_TARGET的 BeanName的前缀
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
	// 为处HandlerMetho的映射分配名称的策略接口,只有一个方法getName()
	// 唯一实现为：RequestMappingInfoHandlerMethodMappingNamingStrategy 策略为：@RequestMapping指定了name属性，那就以指定的为准 否则策略为：取出Controller所有的`大写字母` + # + method.getName()
	// 如：AppoloController#match方法  最终的name为：AC#match 
	// 当然这个你也可以自己实现这个接口，然后set进来即可
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
	// 此处是根据mappingName来获取一个Handler
	@Nullable
	public List<HandlerMethod> getHandlerMethodsForMappingName(String mappingName) {
		return this.mappingRegistry.getHandlerMethodsByMappingName(mappingName);
	}
	// 最终都是委托给 mappingRegistry去做了注册的工作，此处日志级别为trace级别
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
	// 加载HandlerMethod
	protected void initHandlerMethods() {
		// getCandidateBeanNames：Object.class相当于拿到当前容器内所有的Bean定义信息
		for (String beanName : getCandidateBeanNames()) {
			// BeanName不是以这个打头得 : scopedTarget. 
			if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
				// 会在每个Bean里面找处理方法，HandlerMethod，然后注册进去
				processCandidateBean(beanName);
			}
		}
		handlerMethodsInitialized(getHandlerMethods());
	}
	// 确定指定的候选bean的类型，如果标识为Handler类型，则调用DetectHandlerMethods
	
	// isHandler(beanType):判断这个type是否为Handler类型 它是个抽象方法，由子类去判断是否是Handler
	// `RequestMappingHandlerMapping`的判断依据为：该类上标注了@Controller注解或者@Controller注解  就算作是一个Handler
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

	// 在指定的Handler的bean中查找处理程序方法Methods
	protected void detectHandlerMethods(Object handler) {
		Class<?> handlerType = (handler instanceof String ?obtainApplicationContext().getType((String) handler) : handler.getClass());
		if (handlerType != null) {
			Class<?> userType = ClassUtils.getUserClass(handlerType);
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

- HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) //由路径找到handlerMethod

```
protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
		// Match是一个private class，内部就两个属性：T mapping和HandlerMethod handlerMethod
		List<Match> matches = new ArrayList<>();
		// 根据lookupPath去注册中心里查找mappingInfos，因为一个具体的url可能匹配上多个MappingInfo的
		// 至于为何是多值？有这么一种情况  URL都是/api/v1/hello  但是有的是get post delete等方法  当然还有可能是headers/consumes等等不一样，都算多个的  所以有可能是会匹配到多个MappingInfo的
		// 所有这个里可以匹配出多个出来。比如/hello 匹配出GET、POST、PUT都成，所以size可以为3
		List<T> directPathMatches = this.mappingRegistry.getMappingsByUrl(lookupPath);
		if (directPathMatches != null) {
			// 依赖于子类实现的抽象方法：getMatchingMapping()  看看到底匹不匹配，而不仅仅是URL匹配就行
			// 比如还有method、headers、consumes等等这些不同都代表着不同的MappingInfo的
			// 最终匹配上的，会new Match()放进matches里面去
			addMatchingMappings(directPathMatches, matches, request);
		}
		if (matches.isEmpty()) {
			// No choice but to go through all mappings...
			addMatchingMappings(this.mappingRegistry.getMappings().keySet(), matches, request);
		}
		if (!matches.isEmpty()) {
			// getMappingComparator这个方法也是抽象方法由子类去实现的。
			// 比如：`RequestMappingInfoHandlerMapping`的实现为先比较Method，patterns、params
			Comparator<Match> comparator = new MatchComparator(getMappingComparator(request));
			matches.sort(comparator);
			// 排序后的最佳匹配为get(0)
			Match bestMatch = matches.get(0);
			// 如果总的匹配个数大于1的话
			if (matches.size() > 1) {
				if (CorsUtils.isPreFlightRequest(request)) {
					return PREFLIGHT_AMBIGUOUS_MATCH;
				}
				// 次最佳匹配
				Match secondBestMatch = matches.get(1);
				// 注意：这个是运行时的检查
				if (comparator.compare(bestMatch, secondBestMatch) == 0) {
					Method m1 = bestMatch.handlerMethod.getMethod();
					Method m2 = secondBestMatch.handlerMethod.getMethod();
					String uri = request.getRequestURI();
					throw new IllegalStateException("Ambiguous handler methods mapped for '" + uri + "': {" + m1 + ", " + m2 + "}");
				}
			}
			// 把最最佳匹配的方法放进request的属性里面: bestMatchingHandler
			request.setAttribute(BEST_MATCHING_HANDLER_ATTRIBUTE, bestMatch.handlerMethod);
			// pathWithinHandlerMapping
			handleMatch(bestMatch.mapping, lookupPath, request);
			return bestMatch.handlerMethod;
		}
		// 一个都没匹配上，handleNoMatch这个方法虽然不是抽象方法，protected方法子类复写
		// RequestMappingInfoHandlerMapping有复写此方
		else {
			return handleNoMatch(this.mappingRegistry.getMappings().keySet(), lookupPath, request);
		}
	}
	
```

####  RequestMappingHandlerMapping

```
// @since 3.1  Spring3.1才提供的这种注解扫描的方式的支持~~~  它也实现了MatchableHandlerMapping分支的接口
// EmbeddedValueResolverAware接口：说明要支持解析Spring的表达式~
public class RequestMappingHandlerMapping extends RequestMappingInfoHandlerMapping
		implements MatchableHandlerMapping, EmbeddedValueResolverAware {
	
	...
	private Map<String, Predicate<Class<?>>> pathPrefixes = new LinkedHashMap<>();

	// 配置要应用于控制器方法的路径前缀
	// @since 5.1：Spring5.1才出来的新特性，其实有时候还是很好的使的  下面给出使用的Demo
	// 前缀用于enrich每个@RequestMapping方法的映射，至于匹不匹配由Predicate来决定  有种前缀分类的效果~~~~
	// 推荐使用Spring5.1提供的类：org.springframework.web.method.HandlerTypePredicate
	public void setPathPrefixes(Map<String, Predicate<Class<?>>> prefixes) {
		this.pathPrefixes = Collections.unmodifiableMap(new LinkedHashMap<>(prefixes));
	}
	// @since 5.1   注意pathPrefixes是只读的~~~因为上面Collections.unmodifiableMap了  有可能只是个空Map
	public Map<String, Predicate<Class<?>>> getPathPrefixes() {
		return this.pathPrefixes;
	}
	
	public void setUseRegisteredSuffixPatternMatch(boolean useRegisteredSuffixPatternMatch) {
		this.useRegisteredSuffixPatternMatch = useRegisteredSuffixPatternMatch;
		this.useSuffixPatternMatch = (useRegisteredSuffixPatternMatch || this.useSuffixPatternMatch);
	}
	// If enabled a method mapped to "/users" also matches to "/users/".
	public void setUseTrailingSlashMatch(boolean useTrailingSlashMatch) {
		this.useTrailingSlashMatch = useTrailingSlashMatch;
	}
	
	@Override
	public void afterPropertiesSet() {
		// 对RequestMappingInfo的配置进行初始化  赋值
		this.config = new RequestMappingInfo.BuilderConfiguration();
		this.config.setUrlPathHelper(getUrlPathHelper()); // 设置urlPathHelper默认为UrlPathHelper.class
		this.config.setPathMatcher(getPathMatcher()); //默认为AntPathMatcher，路径匹配校验器
		this.config.setSuffixPatternMatch(this.useSuffixPatternMatch); // 是否支持后缀补充，默认为true
		this.config.setTrailingSlashMatch(this.useTrailingSlashMatch); // 是否添加"/"后缀，默认为true
		this.config.setRegisteredSuffixPatternMatch(this.useRegisteredSuffixPatternMatch); // 是否采用mediaType匹配模式，比如.json/.xml模式的匹配，默认为false      
		this.config.setContentNegotiationManager(getContentNegotiationManager()); //mediaType处理类：ContentNegotiationManager

		// 此处 必须还是要调用父类的方法的
		super.afterPropertiesSet();
	}

	// 判断该类，是否是一个handler（此处就体现出@Controller注解的特殊性了）
	@Override
	protected boolean isHandler(Class<?> beanType) {
		return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
				AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
	}

	// 还记得父类：AbstractHandlerMethodMapping#detectHandlerMethods的时候，回去该类里面找所有的指定的方法
	@Override
	@Nullable
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		// 第一步：先拿到方法上的info
		RequestMappingInfo info = createRequestMappingInfo(method);
		if (info != null) {
			// 方法上有。在第二步：拿到类上的info
			RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
			if (typeInfo != null) {
				// 倘若类上面也有，那就combine把两者结合
				// combile的逻辑基如下：
				// names：name1+#+name2
				// path：路径拼接起来作为全路径(容错了方法里没有/的情况)
				// method、params、headers：取并集
				// consumes、produces：以方法的为准，没有指定再取类上的
				// custom：谁有取谁的。若都有：那就看custom具体实现的.combine方法去决定把  简单的说就是交给调用者了~~~
				info = typeInfo.combine(info);
			}

			// 在Spring5.1之后还要处理这个前缀匹配
			// 根据这个类，去找看有没有前缀  getPathPrefix()：entry.getValue().test(handlerType) = true算是hi匹配上了
			// 备注：也支持${os.name}这样的语法拿值，可以把前缀也写在专门的配置文件里面
			String prefix = getPathPrefix(handlerType);
			if (prefix != null) {
				// RequestMappingInfo.paths(prefix)  相当于统一在前面加上这个前缀~
				info = RequestMappingInfo.paths(prefix).build().combine(info);
			}
		}
		return info;
	}

	// 根据此方法/类，创建一个RequestMappingInfo
	@Nullable
	private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
		// 注意：此处使用的是 findMergedAnnotation 这也就是为什么虽然@RequestMapping它并不具有继承的特性，但是你子类仍然有继承的效果的原因
		RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
		
		// 请注意：这里进行了区分处理:Class 和 Method
		// 这里返回的是一个condition 也就是看看要不要处理这个请求的条件
		RequestCondition<?> condition = (element instanceof Class ?getCustomTypeCondition((Class<?>) element) : getCustomMethodCondition((Method) element));
		// 这个createRequestMappingInfo 就是根据一个@RequestMapping以及一个condition创建一个
		return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
	}
	// 他俩都是返回的null。protected方法留给子类复写, 子类可以据此自己定义一套自己的规则来限制匹配
	// Provide a custom method-level request condition.
	// 它相当于在Spring MVC默认的规则的基础上，用户还可以自定义条件进行处理
	@Nullable
	protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
		return null;
	}
	@Nullable
	protected RequestCondition<?> getCustomMethodCondition(Method method) {
		return null;
	}
	// 根据@RequestMapping 创建一个RequestMappingInfo 
	protected RequestMappingInfo createRequestMappingInfo(RequestMapping requestMapping, @Nullable RequestCondition<?> customCondition) {
		RequestMappingInfo.Builder builder = RequestMappingInfo
				// 强大的地方在此处：path里竟然还支持/api/v1/${os.name}/hello 这样形式动态的获取值
				// 也就是说URL还可以从配置文件里面读取,@GetMapping("/${os.name}/hello") // 支持从配置文件里读取此值  Windows 10
				.paths(resolveEmbeddedValuesInPatterns(requestMapping.path()))
				.methods(requestMapping.method())
				.params(requestMapping.params())
				.headers(requestMapping.headers())
				.consumes(requestMapping.consumes())
				.produces(requestMapping.produces())
				.mappingName(requestMapping.name());
		// 调用者自定义的条件
		if (customCondition != null) {
			builder.customCondition(customCondition);
		}
		// 注意此处：把当前的config设置进去了
		return builder.options(this.config).build();
	}

	@Override
	public RequestMatchResult match(HttpServletRequest request, String pattern) { ... }
	// 支持了@CrossOrigin注解  Spring4.2提供的注解
	@Override
	protected CorsConfiguration initCorsConfiguration(Object handler, Method method, RequestMappingInfo mappingInfo) { ... }
}

```

#### @RequestMapping 注解

```
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface RequestMapping {

	//给这个Mapping取一个名字。若不填写，就用HandlerMethodMappingNamingStrategy 去按规则生成
	String name() default "";

	// 路径  数组形式  可以写多个
	@AliasFor("path")
	String[] value() default {};
	@AliasFor("value")
	String[] path() default {};
	
	// 请求方法：GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
	// 显然可以指定多个方法。如果不指定，表示适配所有方法类型~~
	// 同时还有类似的枚举类：org.springframework.http.HttpMethod
	RequestMethod[] method() default {};
	
	// 指定request中必须包含某些参数值时，才让该方法处理
	// 使用 params 元素，你可以让多个处理方法处理到同一个URL 的请求, 而这些请求的参数是不一样的
	// 如：@RequestMapping(value = "/fetch", params = {"personId=10"} 和 @RequestMapping(value = "/fetch", params = {"personId=20"}
	// 这两个方法都处理请求`/fetch`，但是参数不一样，进入的方法也不一样
	// 支持!myParam和myParam!=myValue这种
	String[] params() default {};

	// 指定request中必须包含某些指定的header值，才能让该方法处理请求
	// @RequestMapping(value = "/head", headers = {"content-type=text/plain"}
	String[] headers() default {};

	// 指定处理请求request的**提交内容类型(Content-Type),例如application/json、text/html等相当于只有指定的这些Content-Type的才处理 @RequestMapping(value = "/cons", consumes = {"application/json", "application/XML"}
	// 不指定表示处理所有,取值参见枚举类：org.springframework.http.MediaType
	String[] consumes() default {};
	// 指定返回的内容类型,返回的内容类型必须是request请求头(Accept)中所包含的类型仅当request请求头中的(Accept)类型中包含该指定类型才返回；
	// 参见枚举类：org.springframework.http.MediaType
	String[] produces() default {};

}

```