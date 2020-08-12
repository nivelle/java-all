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

#####  public abstract class AbstractUrlHandlerMapping extends AbstractHandlerMapping implements MatchableHandlerMapping 

//将url对应的Handler保存在一个Map中，在getHandlerInternal方法中使用url从Map中获取Handler

```
public abstract class AbstractUrlHandlerMapping extends AbstractHandlerMapping implements MatchableHandlerMapping {
	// 根路径 / 的处理器
	@Nullable
	private Object rootHandler;
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
		// 找到URL的后半段，由此可见Spring MVC处理URL路径匹配都是从工程名后面开始匹配的
		String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
		// 根据url查找handler 
		// 1、先去handlerMap里找，若找到了那就实例化它，并且给chain里加入一个拦截器：`PathExposingHandlerInterceptor`  它是个private私有类的HandlerInterceptor
		// 该拦截器的作用：request.setAttribute()请求域里面放置四个属性 key见 HandlerMapping 的常量
		// 2、否则就使用 PathMatcher 去匹配URL，这里面光匹配其实是比较简单的。但是这里面还解决了一个问题：那就是匹配上多个路径的问题
		// 因此：若匹配上多个路径了，就按照PathMatcher的排序规则排序，取值get(0)然后加上那个HandlerInterceptor即可
		// 需要注意的是：若存在uriTemplateVariables，也就是路径里都存在多个最佳的匹配的情况  比如/book/{id}和/book/{name}这两种。
		// 还有就是URI完全一样，但是一个是get方法，一个是post方法之类的  那就再加一个拦截器`UriTemplateVariablesHandlerInterceptor`  它request.setAttribute()了一个属性：key为 xxx.uriTemplateVariables
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

	// =========该抽象类提供的这个方法就特别重要了：向handlerMap里面put值的唯一入口
	protected void registerHandler(String[] urlPaths, String beanName) throws BeansException, IllegalStateException {
		Assert.notNull(urlPaths, "URL path array must not be null");
		for (String urlPath : urlPaths) {
			registerHandler(urlPath, beanName);
		}
	}
	protected void registerHandler(String urlPath, Object handler) throws BeansException, IllegalStateException {
		Assert.notNull(urlPath, "URL path must not be null");
		Assert.notNull(handler, "Handler object must not be null");
		Object resolvedHandler = handler;
		// 如果是beanName，并且它是立马加载的~
		if (!this.lazyInitHandlers && handler instanceof String) {
			String handlerName = (String) handler;
			ApplicationContext applicationContext = obtainApplicationContext();
			// 并且还需要是单例的，那就立马实例化
			if (applicationContext.isSingleton(handlerName)) {
				resolvedHandler = applicationContext.getBean(handlerName);
			}
		}
		// 先尝试从Map中去获取
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
			// 注意此处：好像是Spring5之后 把这句Mapped的日志级别   直接降低到trace级别了，简直太低了有木有~~~
			// 在Spring 5之前，这里的日志级别包括上面的setRoot等是info（所以我们在控制台经常能看见大片的'Mapped URL path'日志~~~~）
			// 所以：自Spring5之后不再会看controller这样的映射的日志了
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