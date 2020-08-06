
### HandlerMethodArgumentResolver

#### 基于Name

- AbstractNameValueMethodArgumentResolver

```
// 负责从路径变量、请求、头等中拿到值。（都可以指定name、required、默认值等属性）
// 子类需要做如下事：获取方法参数的命名值信息、将名称解析为参数值当需要参数值时处理缺少的参数值、可选地处理解析值特别注意的是：默认值可以使用${}占位符，或者SpEL语句#{}是木有问题的
public abstract class AbstractNamedValueMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Nullable
	private final ConfigurableBeanFactory configurableBeanFactory;
	@Nullable
	private final BeanExpressionContext expressionContext;
	private final Map<MethodParameter, NamedValueInfo> namedValueInfoCache = new ConcurrentHashMap<>(256);

	public AbstractNamedValueMethodArgumentResolver() {
		this.configurableBeanFactory = null;
		this.expressionContext = null;
	}
	public AbstractNamedValueMethodArgumentResolver(@Nullable ConfigurableBeanFactory beanFactory) {
		this.configurableBeanFactory = beanFactory;
		// 默认是RequestScope
		this.expressionContext = (beanFactory != null ? new BeanExpressionContext(beanFactory, new RequestScope()) : null);
	}

	// protected的内部类  所以所有子类（注解）都是用友这三个属性值的
	protected static class NamedValueInfo {
		private final String name;
		private final boolean required;
		@Nullable
		private final String defaultValue;
		public NamedValueInfo(String name, boolean required, @Nullable String defaultValue) {
			this.name = name;
			this.required = required;
			this.defaultValue = defaultValue;
		}
	}

	//核心方法:注意此方法是final的，并不希望子类覆盖
	@Override
	@Nullable
	public final Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
		// 创建 MethodParameter 对应的 NamedValueInfo
		NamedValueInfo namedValueInfo = getNamedValueInfo(parameter);
		// 支持到了Java 8 中支持的 java.util.Optional
		MethodParameter nestedParameter = parameter.nestedIfOptional();
		// 解析name属性（也就是注解标注的value/name属性）这里既会解析占位符,还会解析SpEL表达式,因为此时的 name 可能还是被 ${} 符号包裹,则通过 BeanExpressionResolver 来进行解析
		Object resolvedName = resolveStringValue(namedValueInfo.name);
		if (resolvedName == null) {
			throw new IllegalArgumentException("Specified name must not resolve to null: [" + namedValueInfo.name + "]");
		}
		// 模版抽象方法：将给定的参数类型和值名称解析为参数值。  由子类去实现
		// @PathVariable     --> 通过对uri解析后得到的decodedUriVariables值(常用) ==》PathVariableMethodArgumentResolver
		// @RequestParam     --> 通过 HttpServletRequest.getParameterValues(name) ==》RequestParamMethodArgumentResolver
		// @RequestAttribute --> 通过 HttpServletRequest.getAttribute(name,RequestAttributes.SCOPE_REQUEST) ==》RequestAttributeMethodArgumentResolver
		// @SessionAttribute --> 通过 request.getAttribute(name, RequestAttributes.SCOPE_SESSION)  ==》SessionAttributeMethodArgumentResolver
		// @RequestHeader    --> 通过 HttpServletRequest.getHeaderValues(name) =String[] headerValues = request.getHeaderValues(name);
		// @CookieValue      --> 通过 HttpServletRequest.getCookies() == AbstractCookieValueMethodArgumentResolver
		// @Value --> ExpressionValueMethodArgumentResolver
		Object arg = resolveName(resolvedName.toString(), nestedParameter, webRequest);
		// 若解析出来值仍旧为null，那就走defaultValue （若指定了的话）
		if (arg == null) {
			// 可以发现：defaultValue也是支持占位符和SpEL的
			if (namedValueInfo.defaultValue != null) {
				arg = resolveStringValue(namedValueInfo.defaultValue);

			// 若 arg == null && defaultValue == null && 非 optional 类型的参数 则通过 handleMissingValue 来进行处理, 一般是报异常
			} else if (namedValueInfo.required && !nestedParameter.isOptional()) {
				// 它是个protected方法，默认抛出ServletRequestBindingException异常
				// 各子类都复写了此方法，转而抛出自己的异常（但都是ServletRequestBindingException的异常子类）
				handleMissingValue(namedValueInfo.name, nestedParameter, webRequest);
			}
			// handleNullValue是private方法，来处理null值
			// 针对Bool类型有这个判断：Boolean.TYPE.equals(paramType) 就return Boolean.FALSE;
			// 此处注意：Boolean.TYPE = Class.getPrimitiveClass("boolean") 它指的基本类型的boolean，而不是Boolean类型哦~~~
			// 如果到了这一步（value是null），但你还是基本类型，那就抛出异常了（只有boolean类型不会抛异常哦~）
			// 这里多嘴一句，即使请求传值为&bool=1，效果同bool=true的（1：true 0：false） 并且不区分大小写哦（TrUe效果同true）
			arg = handleNullValue(namedValueInfo.name, arg, nestedParameter.getNestedParameterType());
		}
		// 兼容空串，若传入的是空串，依旧还是使用默认值（默认值支持占位符和SpEL）
		else if ("".equals(arg) && namedValueInfo.defaultValue != null) {
			arg = resolveStringValue(namedValueInfo.defaultValue);
		}
		// 完成自动化的数据绑定
		if (binderFactory != null) {
			WebDataBinder binder = binderFactory.createBinder(webRequest, null, namedValueInfo.name);
			try {
				// 通过数据绑定器里的Converter转换器把arg转换为指定类型的数值
				arg = binder.convertIfNecessary(arg, parameter.getParameterType(), parameter);
			} catch (ConversionNotSupportedException ex) { // 注意这个异常：MethodArgumentConversionNotSupportedException  类型不匹配的异常
				throw new MethodArgumentConversionNotSupportedException(arg, ex.getRequiredType(),namedValueInfo.name, parameter, ex.getCause());
			} catch (TypeMismatchException ex) { //MethodArgumentTypeMismatchException是TypeMismatchException 的子类
				throw new MethodArgumentTypeMismatchException(arg, ex.getRequiredType(),namedValueInfo.name, parameter, ex.getCause());
			}
		}
		// protected的方法，本类为空实现，交给子类去复写（并不是必须的）
		// 唯独只有PathVariableMethodArgumentResolver把解析处理啊的值存储一下数据到 
		// HttpServletRequest.setAttribute中（若key已经存在也不会存储了）
		handleResolvedValue(arg, namedValueInfo.name, parameter, mavContainer, webRequest);
		return arg;
	}
	
	// 此处有缓存，记录下每一个MethodParameter对象   value是NamedValueInfo值
	private NamedValueInfo getNamedValueInfo(MethodParameter parameter) {
		NamedValueInfo namedValueInfo = this.namedValueInfoCache.get(parameter);
		if (namedValueInfo == null) {
			// createNamedValueInfo是抽象方法，子类必须实现
			namedValueInfo = createNamedValueInfo(parameter);
			// updateNamedValueInfo：这一步就是我们之前说过的为何Spring MVC可以根据参数名封装的方法
			// 如果info.name.isEmpty()的话（注解里没指定名称），就通过`parameter.getParameterName()`去获取参数名~
			// 它还会处理注解指定的defaultValue：`\n\t\.....`等等都会被当作null处理
			// 都处理好后：new NamedValueInfo(name, info.required, defaultValue);（相当于吧注解解析成了此对象嘛~~）
			namedValueInfo = updateNamedValueInfo(parameter, namedValueInfo);
			this.namedValueInfoCache.put(parameter, namedValueInfo);
		}
		return namedValueInfo;
	}

	// 抽象方法 
	protected abstract NamedValueInfo createNamedValueInfo(MethodParameter parameter);
	// 由子类根据名称，去把值拿出来
	protected abstract Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception;
}


```

- RequestParamMethodArgumentResolver

```
// @since 3.1
public class RequestParamMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver implements UriComponentsContributor {

	private static final TypeDescriptor STRING_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(String.class);

	// true:表示参数类型是基本类型,如果是基本类型，即使你不写@RequestParam注解，它也是会走进来处理的
	// fasle:除上以外的,要想它处理就必须标注注解才行哦，比如List等
	// 默认值是false
	private final boolean useDefaultResolution;

	// 此构造只有`MvcUriComponentsBuilder`调用了 传入的false
	public RequestParamMethodArgumentResolver(boolean useDefaultResolution) {
		this.useDefaultResolution = useDefaultResolution;
	}
	// 传入了ConfigurableBeanFactory ，所以它支持处理占位符${...} 并且支持SpEL了
	// 此构造都在RequestMappingHandlerAdapter里调用，最后都会传入true来Catch-all Case  这种设计挺有意思的
	public RequestParamMethodArgumentResolver(@Nullable ConfigurableBeanFactory beanFactory, boolean useDefaultResolution) {
		super(beanFactory);
		this.useDefaultResolution = useDefaultResolution;
	}

	// 此处理器能处理如下Case：
	// 1、所有标注有@RequestParam注解的类型（非Map）/ 注解指定了value值的Map类型（自己提供转换器哦）
	// ======下面都表示没有标注@RequestParam注解了的=======
	// 1、不能标注有@RequestPart注解，否则直接不处理了
	// 2、是上传的request：isMultipartArgument() = true（MultipartFile类型或者对应的集合/数组类型  或者javax.servlet.http.Part对应结合/数组类型）
	// 3、useDefaultResolution=true情况下，"基本类型"也会处理
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (parameter.hasParameterAnnotation(RequestParam.class)) {
			if (Map.class.isAssignableFrom(parameter.nestedIfOptional().getNestedParameterType())) {
				RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
				return (requestParam != null && StringUtils.hasText(requestParam.name()));
			} else {
				return true;
			}
		} else {
			if (parameter.hasParameterAnnotation(RequestPart.class)) {
				return false;
			}
			parameter = parameter.nestedIfOptional();
			if (MultipartResolutionDelegate.isMultipartArgument(parameter)) {
				return true;
			} else if (this.useDefaultResolution) {
				return BeanUtils.isSimpleProperty(parameter.getNestedParameterType());
			} else {
				return false;
			}
		}
	}


	// 没有 @RequestParam注解，也是可以创建出一个NamedValueInfo
	@Override
	protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
		RequestParam ann = parameter.getParameterAnnotation(RequestParam.class);
		return (ann != null ? new RequestParamNamedValueInfo(ann) : new RequestParamNamedValueInfo());
	}

	// 内部类
	private static class RequestParamNamedValueInfo extends NamedValueInfo {
		// 请注意这个默认值：如果你不写@RequestParam，那么就会用这个默认值
		// 注意：required = false （若写了注解，required默认可是true）
		// 因为不写注解的情况下，若是简单类型参数都是交给此处理器处理的。
		// 复杂类型（非简单类型）默认是ModelAttributeMethodProcessor处理的
		public RequestParamNamedValueInfo() {
			super("", false, ValueConstants.DEFAULT_NONE);
		}
		public RequestParamNamedValueInfo(RequestParam annotation) {
			super(annotation.name(), annotation.required(), annotation.defaultValue());
		}
	}

	// 核心方法：根据Name 获取值（普通/文件上传）
	// 并且还有集合、数组等情况
	@Override
	@Nullable
	protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
		HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
		// 这块解析出来的是个MultipartFile或者其集合/数组
		if (servletRequest != null) {
			Object mpArg = MultipartResolutionDelegate.resolveMultipartArgument(name, parameter, servletRequest);
			if (mpArg != MultipartResolutionDelegate.UNRESOLVABLE) {
				return mpArg;
			}
		}
		Object arg = null;
		MultipartRequest multipartRequest = request.getNativeRequest(MultipartRequest.class);
		if (multipartRequest != null) {
			List<MultipartFile> files = multipartRequest.getFiles(name);
			if (!files.isEmpty()) {
				arg = (files.size() == 1 ? files.get(0) : files);
			}
		}
		// 若解析出来值仍旧为null，那处理完文件上传里没有，那就去参数里获取
		// 由此可见：文件上传的优先级是高于请求参数的
		if (arg == null) {
			//小知识点：getParameter()其实本质是getParameterNames()[0]的效果
			// 强调一遍：?ids=1,2,3 结果是["1,2,3"]（兼容方式，不建议使用。注意：只能是逗号分隔）
			// ?ids=1&ids=2&ids=3  结果是[1,2,3]（标准的传值方式，建议使用）
			// 但是Spring MVC这两种都能用List接收  请务必注意他们的区别~~~
			String[] paramValues = request.getParameterValues(name);
			if (paramValues != null) {
				arg = (paramValues.length == 1 ? paramValues[0] : paramValues);
			}
		}
		return arg;
	}

}

```

- RequestHeaderMethodArgumentResolver

```

public class RequestHeaderMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

	// 必须标注@RequestHeader注解，并且不能是Map类型
	// `@RequestHeader Map headers`这样可以接收到所有的请求头啊
	// 其实不是本类的功劳，是`RequestHeaderMapMethodArgumentResolver`的作用
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return (parameter.hasParameterAnnotation(RequestHeader.class) &&
				!Map.class.isAssignableFrom(parameter.nestedIfOptional().getNestedParameterType()));
	}

	// 理解起来很简单：可以单值，也可以List/数组
	@Override
	@Nullable
	protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
		String[] headerValues = request.getHeaderValues(name);
		if (headerValues != null) {
			return (headerValues.length == 1 ? headerValues[0] : headerValues);
		} else {
			return null;
		}
	}
}

```
- ExpressionValueMethodArgumentResolver

```
// @since 3.1
public class ExpressionValueMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {
	// 唯一构造函数  支持占位符、SpEL
	public ExpressionValueMethodArgumentResolver(@Nullable ConfigurableBeanFactory beanFactory) {
		super(beanFactory);
	}
	//必须标注有@Value注解
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(Value.class);
	}
	@Override
	protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
		Value ann = parameter.getParameterAnnotation(Value.class);
		return new ExpressionValueNamedValueInfo(ann);
	}
	private static final class ExpressionValueNamedValueInfo extends NamedValueInfo {
		// 这里name传值为固定值  因为只要你的key不是这个就木有问题
		// required传固定值false
		// defaultValue：取值为annotation.value() --> 它天然支持占位符和SpEL嘛
		private ExpressionValueNamedValueInfo(Value annotation) {
			super("@Value", false, annotation.value());
		}
	}
	// 这里恒返回null，因此即使你的key是@Value
	@Override
	@Nullable
	protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest webRequest) throws Exception {
		// No name to resolve
		return null;
	}
}

```


#### 数据类型是Map的

数据来源同上，只是参数类型是Map

#### 固定参数类型(基于ContentType的消息转换器)

#### ServletRequestMethodArgumentResolver

- supportsParameter //查询支持的参数类型

```
public boolean supportsParameter(MethodParameter parameter) {
		Class<?> paramType = parameter.getParameterType();
		return (WebRequest.class.isAssignableFrom(paramType) || ServletRequest.class.isAssignableFrom(paramType) ||
				MultipartRequest.class.isAssignableFrom(paramType) ||HttpSession.class.isAssignableFrom(paramType) ||
				(pushBuilder != null && pushBuilder.isAssignableFrom(paramType)) ||Principal.class.isAssignableFrom(paramType) ||
				InputStream.class.isAssignableFrom(paramType) ||Reader.class.isAssignableFrom(paramType) ||
				HttpMethod.class == paramType ||Locale.class == paramType ||TimeZone.class == paramType ||ZoneId.class == paramType);
	}

```

- resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory)

```
//webRequest 是 Spring MVC 在处理一个请求过程中代表当前请求的对象，在 Spring MVC 使用某个 HandlerMethodArgumentResolver 解析控制器方法的某个参数时，
//总是会将 webRequest 传递给该 HandlerMethodArgumentResolver。
//mavContainer, webRequest , binderFactory 共同组成了解析指定参数 parameter的一个上下文环境
public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

		Class<?> paramType = parameter.getParameterType();

		// WebRequest / NativeWebRequest / ServletWebRequest
		if (WebRequest.class.isAssignableFrom(paramType)) {
			if (!paramType.isInstance(webRequest)) {
				throw new IllegalStateException("Current request is not of type [" + paramType.getName() + "]: " + webRequest);
			}
			return webRequest;
		}

		// ServletRequest / HttpServletRequest / MultipartRequest / MultipartHttpServletRequest
		//如果参数的类型为  ServletRequest 的可赋值类型时，委托方法 resolveNativeRequest 做具体解析
		if (ServletRequest.class.isAssignableFrom(paramType) || MultipartRequest.class.isAssignableFrom(paramType)) {
			return resolveNativeRequest(webRequest, paramType);
		}

		// HttpServletRequest required for all further argument types
		return resolveArgument(paramType, resolveNativeRequest(webRequest, HttpServletRequest.class));
	}

```

- private <T> T resolveNativeRequest(NativeWebRequest webRequest, Class<T> requiredType)

```
 	private <T> T resolveNativeRequest(NativeWebRequest webRequest, Class<T> requiredType) {
       // requiredType 就是开发人员在控制器方法中指定的request 的类型，在上面的例子中，它就是  ServletRequest,
       // 实际上，它也可以继承自 ServletRequest 的某个子类
		T nativeRequest = webRequest.getNativeRequest(requiredType);
		if (nativeRequest == null) {
			throw new IllegalStateException("Current request is not of type [" + requiredType.getName() + "]: " + webRequest);
		}
		return nativeRequest;
	}


```
- private Object resolveArgument(Class<?> paramType, HttpServletRequest request) throws IOException 

```
private Object resolveArgument(Class<?> paramType, HttpServletRequest request) throws IOException {
		if (HttpSession.class.isAssignableFrom(paramType)) {
			HttpSession session = request.getSession();
			if (session != null && !paramType.isInstance(session)) {
				throw new IllegalStateException(
						"Current session is not of type [" + paramType.getName() + "]: " + session);
			}
			return session;
		}
		else if (pushBuilder != null && pushBuilder.isAssignableFrom(paramType)) {
			return PushBuilderDelegate.resolvePushBuilder(request, paramType);
		}
		else if (InputStream.class.isAssignableFrom(paramType)) {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null && !paramType.isInstance(inputStream)) {
				throw new IllegalStateException(
						"Request input stream is not of type [" + paramType.getName() + "]: " + inputStream);
			}
			return inputStream;
		}
		else if (Reader.class.isAssignableFrom(paramType)) {
			Reader reader = request.getReader();
			if (reader != null && !paramType.isInstance(reader)) {
				throw new IllegalStateException(
						"Request body reader is not of type [" + paramType.getName() + "]: " + reader);
			}
			return reader;
		}
		else if (Principal.class.isAssignableFrom(paramType)) {
			Principal userPrincipal = request.getUserPrincipal();
			if (userPrincipal != null && !paramType.isInstance(userPrincipal)) {
				throw new IllegalStateException(
						"Current user principal is not of type [" + paramType.getName() + "]: " + userPrincipal);
			}
			return userPrincipal;
		}
		else if (HttpMethod.class == paramType) {
			return HttpMethod.resolve(request.getMethod());
		}
		else if (Locale.class == paramType) {
			return RequestContextUtils.getLocale(request);
		}
		else if (TimeZone.class == paramType) {
			TimeZone timeZone = RequestContextUtils.getTimeZone(request);
			return (timeZone != null ? timeZone : TimeZone.getDefault());
		}
		else if (ZoneId.class == paramType) {
			TimeZone timeZone = RequestContextUtils.getTimeZone(request);
			return (timeZone != null ? timeZone.toZoneId() : ZoneId.systemDefault());
		}

		// Should never happen...
		throw new UnsupportedOperationException("Unknown parameter type: " + paramType.getName());
	}

```
