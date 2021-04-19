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

#####  HTTP头Accept

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


##### 扩展名

基于上面例子：若我访问/test/1.xml返回的是xml，若访问/test/1.json返回的是json；

这种方式使用起来非常的便捷，并且还不依赖于浏览器。但我总结了如下几点使时的注意事项：

1. 扩展名必须是变量的扩展名。比如上例若访问test.json / test.xml就404;@PathVariable的参数类型只能使用通用类型（String/Object），因为接收过来的value值就是1.json/1.xml，所以若用Integer接收将报错类型转换错误

2. 小技巧：我个人建议是这部分不接收（这部分不使用@PathVariable接收），拿出来只为内容协商使用
   
3. 扩展名优先级比Accept要高（并且和使用神马浏览器无关）


##### 请求参数

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

- 固定类型（producers）

它就是利用@RequestMapping注解属性produces（可能你平时也在用，但并不知道原因）：

````
@ResponseBody
@GetMapping(value = {"/test/{id}", "/test"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public Person test() { ... }
````

访问：/test/1返回的就是json；即使你已经导入了jackson的xml包，返回的依旧还是json。

它也有它很很很重要的一个注意事项：produces指定的MediaType类型不能和后缀、请求参数、Accept冲突。例如本利这里指定了json格式，如果你这么访问/test/1.xml，或者format=xml，或者Accept不是application/json或者*/* 将无法完成内容协商：http状态码为406



