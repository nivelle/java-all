### 一个URL资源服务端可以以多种形式进行响应：即MIME（MediaType）媒体类型。但对于某一个客户端（浏览器、APP、Excel导出…）来说它只需要一种。so这样客户端和服务端就得有一种机制来保证这个事情，这种机制就是内容协商机制。

### 内容协商过程 

客户端发请求时就指明需要的MIME们（比如Http头部的：Accept），服务端根据客户端指定的要求返回合适的形式，并且在响应头中做出说明（如：Content-Type）
1. 若客户端要求的MIME类型服务端提供不了，那就406错误

### 常用请求头、响应头

#### 请求头
- Accept：告诉服务端需要的MIME（一般是多个，比如text/plain，application/json等。*/*表示可以是任何MIME资源）
- Accept-Language：告诉服务端需要的语言（在中国默认是中文嘛，但浏览器一般都可以选择N多种语言，但是是否支持要看服务器是否可以协商）
- Accept-Charset：告诉服务端需要的字符集
- Accept-Encoding：告诉服务端需要的压缩方式（gzip,deflate,br）
#### 响应头
- Content-Type：告诉客户端响应的媒体类型（如application/json、text/html等）
- Content-Language：告诉客户端响应的语言
- Content-Charset：告诉客户端响应的字符集
- Content-Encoding：告诉客户端响应的压缩方式（gzip）

### Spring MVC内容协商

#### Spring MVC实现了HTTP内容协商的同时，又进行了扩展。它支持4种协商方式：

#####  1. HTTP头Accept

Chrome浏览器请求默认发出的Accept是：Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3。
由于我例子使用的是@ResponseBody，因此它不会返回一个view：交给消息转换器处理，因此这就和MediaType以及权重有关了。

消息最终都会交给AbstractMessageConverterMethodProcessor.writeWithMessageConverters()方法：

````
// @since 3.1
AbstractMessageConverterMethodProcessor：
	protected <T> void writeWithMessageConverters( ... ) {
		Object body;
		Class<?> valueType;
		Type targetType;
		...
		HttpServletRequest request = inputMessage.getServletRequest();
		// 这里交给contentNegotiationManager.resolveMediaTypes()  找出客户端可以接受的MediaType 
		// 此处是已经排序好的（根据Q值等等）
		List<MediaType> acceptableTypes = getAcceptableMediaTypes(request);
		// 这是服务端它所能提供出的MediaType们
		List<MediaType> producibleTypes = getProducibleMediaTypes(request, valueType, targetType);
	
		// 协商。 经过一定的排序、匹配  最终匹配出一个合适的MediaType
		// 把待使用的们再次排序，
		MediaType.sortBySpecificityAndQuality(mediaTypesToUse);

		// 最终找出一个最合适的、最终使用的：selectedMediaType 
			for (MediaType mediaType : mediaTypesToUse) {
				if (mediaType.isConcrete()) {
					selectedMediaType = mediaType;
					break;
				} else if (mediaType.isPresentIn(ALL_APPLICATION_MEDIA_TYPES)) {
					selectedMediaType = MediaType.APPLICATION_OCTET_STREAM;
					break;
				}
			}
	}


````
acceptableTypes是客户端通过Accept告知的。
producibleTypes代表着服务端所能提供的类型们。参考这个getProducibleMediaTypes()方法：

````
AbstractMessageConverterMethodProcessor：

	protected List<MediaType> getProducibleMediaTypes( ... ) {
		// 它设值的地方唯一在于：@RequestMapping.producers属性
		// 大多数情况下：我们一般都不会给此属性赋值
		Set<MediaType> mediaTypes = (Set<MediaType>) request.getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
		if (!CollectionUtils.isEmpty(mediaTypes)) {
			return new ArrayList<>(mediaTypes);
		}
		// 大多数情况下：都会走进这个逻辑 --> 从消息转换器中匹配一个合适的出来
		else if (!this.allSupportedMediaTypes.isEmpty()) {
			List<MediaType> result = new ArrayList<>();
			// 从所有的消息转换器中  匹配出一个/多个List<MediaType> result出来
			// 这就代表着：我服务端所能支持的所有的List<MediaType>们了
			for (HttpMessageConverter<?> converter : this.messageConverters) {
				if (converter instanceof GenericHttpMessageConverter && targetType != null) {
					if (((GenericHttpMessageConverter<?>) converter).canWrite(targetType, valueClass, null)) {
						result.addAll(converter.getSupportedMediaTypes());
					}
				}
				else if (converter.canWrite(valueClass, null)) {
					result.addAll(converter.getSupportedMediaTypes());
				}
			}
			return result;
		} else { 
			return Collections.singletonList(MediaType.ALL);
		}
	}


````

可以看到服务端最终能够提供哪些MediaType，来源于消息转换器HttpMessageConverter对类型的支持。
本例的现象：起初返回的是json串，仅仅只需要导入jackson-dataformat-xml后就返回xml了。原因是因为加入MappingJackson2XmlHttpMessageConverter都有这个判断：

````
	private static final boolean jackson2XmlPresent = ClassUtils.isPresent("com.fasterxml.jackson.dataformat.xml.XmlMapper", classLoader);
	
		if (jackson2XmlPresent) {
			addPartConverter(new MappingJackson2XmlHttpMessageConverter());
		}


````
所以默认情况下Spring MVC并不支持application/xml这种媒体格式，所以若不导包协商出来的结果是：application/json。

默认情况下优先级是xml高于json。当然一般都木有xml包，所以才轮到json的。


##### 2.扩展名

基于上面例子：若我访问/test/1.xml返回的是xml，若访问/test/1.json返回的是json；

这种方式使用起来非常的便捷，并且还不依赖于浏览器。但我总结了如下几点使时的注意事项：

1. 扩展名必须是变量的扩展名。比如上例若访问test.json / test.xml就404;@PathVariable的参数类型只能使用通用类型（String/Object），因为接收过来的value值就是1.json/1.xml，所以若用Integer接收将报错类型转换错误

2. 小技巧：我个人建议是这部分不接收（这部分不使用@PathVariable接收），拿出来只为内容协商使用
   
3. 扩展名优先级比Accept要高（并且和使用神马浏览器无关）


##### 3.请求参数

这种协商方式Spring MVC支持，但默认是关闭的，需要显示的打开：

````
@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    	// 支持请求参数协商
        configurer.favorParameter(true);
    }
}


````
请求URL：/test/1?format=xml返回xml；/test/1?format=json返回json。同样的我总结如下几点注意事项：

1. 前两种方式默认是开启的，但此种方式需要手动显示开启
2. 此方式优先级低于扩展名（因此你测试时若想它生效，请去掉url的后缀）

##### 4.固定类型（producers）

它就是利用@RequestMa****pping注解属性produces（可能你平时也在用，但并不知道原因）：

````
@ResponseBody
@GetMapping(value = {"/test/{id}", "/test"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public Person test() { ... }
````

访问：/test/1返回的就是json；即使你已经导入了jackson的xml包，返回的依旧还是json。

它也有它很很很重要的一个注意事项：produces指定的MediaType类型不能和后缀、请求参数、Accept冲突。例如本利这里指定了json格式，如果你这么访问/test/1.xml，或者format=xml，或者Accept不是application/json或者*/* 将无法完成内容协商：http状态码为406



### HeaderContentNegotiationStrategy

- Accept Header解析：它根据请求头Accept来协商。

````
public class HeaderContentNegotiationStrategy implements ContentNegotiationStrategy {
	@Override
	public List<MediaType> resolveMediaTypes(NativeWebRequest request) throws HttpMediaTypeNotAcceptableException {
	
		// 我的Chrome浏览器值是：[text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3]
		// postman的值是：[*/*]
		String[] headerValueArray = request.getHeaderValues(HttpHeaders.ACCEPT);
		if (headerValueArray == null) {
			return MEDIA_TYPE_ALL_LIST;
		}

		List<String> headerValues = Arrays.asList(headerValueArray);
		try {
			List<MediaType> mediaTypes = MediaType.parseMediaTypes(headerValues);
			// 排序
			MediaType.sortBySpecificityAndQuality(mediaTypes);
			// 最后Chrome浏览器的List如下：
			// 0 = {MediaType@6205} "text/html"
			// 1 = {MediaType@6206} "application/xhtml+xml"
			// 2 = {MediaType@6207} "image/webp"
			// 3 = {MediaType@6208} "image/apng"
			// 4 = {MediaType@6209} "application/signed-exchange;v=b3"
			// 5 = {MediaType@6210} "application/xml;q=0.9"
			// 6 = {MediaType@6211} "*/*;q=0.8"
			return !CollectionUtils.isEmpty(mediaTypes) ? mediaTypes : MEDIA_TYPE_ALL_LIST;
		} catch (InvalidMediaTypeException ex) {
			throw new HttpMediaTypeNotAcceptableException("Could not parse 'Accept' header " + headerValues + ": " + ex.getMessage());
		}
	}
}

````

可以看到，如果没有传递Accept，则默认使用MediaType.ALL 也就是*/*

### AbstractMappingContentNegotiationStrategy

````
// @since 3.2 它是个协商策略抽象实现，同时也有了扩展名+MediaType对应关系的能力
public abstract class AbstractMappingContentNegotiationStrategy extends MappingMediaTypeFileExtensionResolver implements ContentNegotiationStrategy {

	// Whether to only use the registered mappings to look up file extensions,
	// or also to use dynamic resolution (e.g. via {@link MediaTypeFactory}.
	// org.springframework.http.MediaTypeFactory是Spring5.0提供的一个工厂类
	// 它会读取/org/springframework/http/mime.types这个文件，里面有记录着对应关系
	private boolean useRegisteredExtensionsOnly = false;
	// Whether to ignore requests with unknown file extension. Setting this to
	// 默认false：若认识不认识的扩展名，抛出异常：HttpMediaTypeNotAcceptableException
	private boolean ignoreUnknownExtensions = false;

	// 唯一构造函数
	public AbstractMappingContentNegotiationStrategy(@Nullable Map<String, MediaType> mediaTypes) {
		super(mediaTypes);
	}

	// 实现策略接口方法
	@Override
	public List<MediaType> resolveMediaTypes(NativeWebRequest webRequest) throws HttpMediaTypeNotAcceptableException {
		// getMediaTypeKey：抽象方法(让子类把扩展名这个key提供出来)
		return resolveMediaTypeKey(webRequest, getMediaTypeKey(webRequest));
	}

	public List<MediaType> resolveMediaTypeKey(NativeWebRequest webRequest, @Nullable String key) throws HttpMediaTypeNotAcceptableException {
		if (StringUtils.hasText(key)) {
			// 调用父类方法：根据key去查找出一个MediaType出来
			MediaType mediaType = lookupMediaType(key); 
			// 找到了就return就成（handleMatch是protected的空方法~~~  子类目前没有实现的）
			if (mediaType != null) {
				handleMatch(key, mediaType); // 回调
				return Collections.singletonList(mediaType);
			}

			// 若没有对应的MediaType，交给handleNoMatch处理（默认是抛出异常，见下面）
			// 注意：handleNoMatch如果通过工厂找到了，那就addMapping()保存起来（相当于注册上去）
			mediaType = handleNoMatch(webRequest, key);
			if (mediaType != null) {
				addMapping(key, mediaType);
				return Collections.singletonList(mediaType);
			}
		}
		return MEDIA_TYPE_ALL_LIST; // 默认值：所有
	}

	// 此方法子类ServletPathExtensionContentNegotiationStrategy有复写
	@Nullable
	protected MediaType handleNoMatch(NativeWebRequest request, String key) throws HttpMediaTypeNotAcceptableException {

		// 若不是仅仅从注册里的拿，那就再去MediaTypeFactory里看看~~~  找到了就返回
		if (!isUseRegisteredExtensionsOnly()) {
			Optional<MediaType> mediaType = MediaTypeFactory.getMediaType("file." + key);
			if (mediaType.isPresent()) {
				return mediaType.get();
			}
		}

		// 忽略找不到，返回null吧  否则抛出异常：HttpMediaTypeNotAcceptableException
		if (isIgnoreUnknownExtensions()) {
			return null;
		}
		throw new HttpMediaTypeNotAcceptableException(getAllMediaTypes());
	}
}


````
通过file extension/query param来协商的抽象实现类。在了解它之前，有必要先插队先了解MediaTypeFileExtensionResolver它的作用：

MediaTypeFileExtensionResolver：MediaType和路径扩展名解析策略的接口，例如将 .json 解析成 application/json 或者反向解析

#### 子类实现： MappingMediaTypeFileExtensionResolver

````
public class MappingMediaTypeFileExtensionResolver implements MediaTypeFileExtensionResolver {

	// key是lowerCaseExtension，value是对应的mediaType
	private final ConcurrentMap<String, MediaType> mediaTypes = new ConcurrentHashMap<>(64);
	// 和上面相反，key是mediaType，value是lowerCaseExtension（显然用的是多值map）
	private final MultiValueMap<MediaType, String> fileExtensions = new LinkedMultiValueMap<>();
	// 所有的扩展名（List非set哦~）
	private final List<String> allFileExtensions = new ArrayList<>();

	...
	public Map<String, MediaType> getMediaTypes() {
		return this.mediaTypes;
	}
	// protected 方法
	protected List<MediaType> getAllMediaTypes() {
		return new ArrayList<>(this.mediaTypes.values());
	}
	// 给extension添加一个对应的mediaType
	// 采用ConcurrentMap是为了避免出现并发情况下导致的一致性问题
	protected void addMapping(String extension, MediaType mediaType) {
		MediaType previous = this.mediaTypes.putIfAbsent(extension, mediaType);
		if (previous == null) {
			this.fileExtensions.add(mediaType, extension);
			this.allFileExtensions.add(extension);
		}
	}

	// 接口方法：拿到指定的mediaType对应的扩展名们~
	@Override
	public List<String> resolveFileExtensions(MediaType mediaType) {
		List<String> fileExtensions = this.fileExtensions.get(mediaType);
		return (fileExtensions != null ? fileExtensions : Collections.emptyList());
	}
	@Override
	public List<String> getAllFileExtensions() {
		return Collections.unmodifiableList(this.allFileExtensions);
	}

	// protected 方法：根据扩展名找到一个MediaType~（当然可能是找不到的）
	@Nullable
	protected MediaType lookupMediaType(String extension) {
		return this.mediaTypes.get(extension.toLowerCase(Locale.ENGLISH));
	}
}

````

### ParameterContentNegotiationStrategy

````
public class ParameterContentNegotiationStrategy extends AbstractMappingContentNegotiationStrategy {
	// 请求参数默认的key是format，你是可以设置和更改的。(set方法)
	private String parameterName = "format";

	// 唯一构造
	public ParameterContentNegotiationStrategy(Map<String, MediaType> mediaTypes) {
		super(mediaTypes);
	}
	... // 生路get/set

	// 小Tips：这里调用的是getParameterName()而不是直接用属性名，以后建议大家设计框架也都这么使用 虽然很多时候效果是一样的，但更符合使用规范
	@Override
	@Nullable
	protected String getMediaTypeKey(NativeWebRequest request) {
		return request.getParameter(getParameterName());
	}
}


````

### PathExtensionContentNegotiationStrategy

``````
public class PathExtensionContentNegotiationStrategy extends AbstractMappingContentNegotiationStrategy {

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	// 它额外提供了一个空构造
	public PathExtensionContentNegotiationStrategy() {
		this(null);
	}
	// 有参构造
	public PathExtensionContentNegotiationStrategy(@Nullable Map<String, MediaType> mediaTypes) {
		super(mediaTypes);
		setUseRegisteredExtensionsOnly(false);
		setIgnoreUnknownExtensions(true); // 注意：这个值设置为了true
		this.urlPathHelper.setUrlDecode(false); // 不需要解码（url请勿有中文）
	}

	// @since 4.2.8  可见Spring MVC允许你自己定义解析的逻辑
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		this.urlPathHelper = urlPathHelper;
	}


	@Override
	@Nullable
	protected String getMediaTypeKey(NativeWebRequest webRequest) {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		if (request == null) {
			return null;
		}

		// 借助urlPathHelper、UriUtils从URL中把扩展名解析出来
		String path = this.urlPathHelper.getLookupPathForRequest(request);
		String extension = UriUtils.extractFileExtension(path);
		return (StringUtils.hasText(extension) ? extension.toLowerCase(Locale.ENGLISH) : null);
	}

	// 子类ServletPathExtensionContentNegotiationStrategy有使用和复写
	// 它的作用是面向Resource找到这个资源对应的MediaType ~
	@Nullable
	public MediaType getMediaTypeForResource(Resource resource) { ... }
}


``````

### FixedContentNegotiationStrategy

``````
public class FixedContentNegotiationStrategy implements ContentNegotiationStrategy {
	private final List<MediaType> contentTypes;

	// 构造函数：必须指定MediaType
	// 一般通过@RequestMapping.produces这个注解属性指定（可指定多个）
	public FixedContentNegotiationStrategy(MediaType contentType) {
		this(Collections.singletonList(contentType));
	}
	// @since 5.0
	public FixedContentNegotiationStrategy(List<MediaType> contentTypes) {
		this.contentTypes = Collections.unmodifiableList(contentTypes);
	}
}

``````