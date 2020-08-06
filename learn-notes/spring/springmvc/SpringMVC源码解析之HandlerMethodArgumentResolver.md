
### HandlerMethodArgumentResolver

#### 基于Name

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

#### 数据类型是Map的

数据来源同上，只是参数类型是Map


#### 固定参数类型

#### 基于ContentType的消息转换器

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
