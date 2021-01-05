
```
protected void initStrategies(ApplicationContext context) {
		initMultipartResolver(context);
		initLocaleResolver(context);
		initThemeResolver(context);
		initHandlerMappings(context);
		initHandlerAdapters(context);
		initHandlerExceptionResolvers(context);
		initRequestToViewNameTranslator(context);
		initViewResolvers(context);
		initFlashMapManager(context);
	}

```
	
#### springBoot 默认配置的组件（文件DispatcherServlet.properties）

文件DispatcherServlet.properties是一个属性文件。每个属性的key是一个策略接口的长名称，而value是key指定的策略接口的多个实现类的长名称，每个类名称之间使用,分割。

```
org.springframework.web.servlet.LocaleResolver=
org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver

org.springframework.web.servlet.ThemeResolver=
org.springframework.web.servlet.theme.FixedThemeResolver

org.springframework.web.servlet.HandlerMapping=
org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping,
org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

org.springframework.web.servlet.HandlerAdapter=
org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter,
org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter,
org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

org.springframework.web.servlet.HandlerExceptionResolver=
org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver,
org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver,
org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver

org.springframework.web.servlet.RequestToViewNameTranslator=
org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator

org.springframework.web.servlet.ViewResolver=
org.springframework.web.servlet.view.InternalResourceViewResolver

org.springframework.web.servlet.FlashMapManager=
org.springframework.web.servlet.support.SessionFlashMapManager

```

	
		
### initMultipartResolver

```
public interface MultipartResolver {
	boolean isMultipart(HttpServletRequest request);
	MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException;
	void cleanupMultipart(MultipartHttpServletRequest request);
}

```

MultipartResolver 用于处理文件上传，当收到请求时 DispatcherServlet#checkMultipart() 方法会调用 MultipartResolver#isMultipart() 方法判断请求中是否包含文件。如果请求数据中包含文件，则调用 MultipartResolver#resolveMultipart() 方法对请求的数据进行解析。
然后将文件数据解析成 MultipartFile 并封装在 MultipartHttpServletRequest(继承了 HttpServletRequest) 对象中，最后传递给 Controller

#### 子类实现

- public class StandardServletMultipartResolver implements MultipartResolver

- public class CommonsMultipartResolver extends CommonsFileUploadSupport implements MultipartResolver, ServletContextAware 

### initLocaleResolver

```
public interface LocaleResolver {
	//根据request对象根据指定的方式获取一个Locale，如果没有获取到，则使用用户指定的默认的Locale
	Locale resolveLocale(HttpServletRequest request);
	//用于实现Locale的切换。比如SessionLocaleResolver获取Locale的方式是从session中读取，但如果
	//户想要切换其展示的样式(由英文切换为中文)，那么这里的setLocale()方法就提供了这样一种可能
	void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale);

}

```

#### 子类实现
  
  //其会将Locale信息存储在session中，如果用户想要修改Locale信息，可以通过修改session中对应属性的值即可；
- public class SessionLocaleResolver extends AbstractLocaleContextResolver
   
  //其会通过用户请求中名称为Accept-Language的header来获取Locale信息，如果想要修改展示的视图，只需要修改该header信息即可
- public class AcceptHeaderLocaleResolver implements LocaleResolver 
  
  //其读取Locale的方式是在session中通过Cookie来获取其指定的Locale的，如果修改了Cookie的值，页面视图也会同步切换； 
- public class CookieLocaleResolver extends CookieGenerator implements LocaleContextResolver 
  // 在声明该resolver时，需要指定一个默认的Locale，在进行Locale获取时，始终返回该Locale，并且调用其setLocale()方法也无法改变其Locale
- public class FixedLocaleResolver extends AbstractLocaleContextResolver

对于Locale的切换，Spring是通过拦截器来实现的，其提供了一个LocaleChangeInterceptor，若要生效，这个Bean需要自己配置


### initThemeResolver

- CookieThemeResolver

- SessionThemeResolver

- FixedThemeResolver

### initHandlerMappings 

作用是根据当前请求的找到对应的 Handler，并将 Handler（执行程序）与一堆 HandlerInterceptor（拦截器,也是他来处理的）封装到 HandlerExecutionChain 对象中。返回给中央调度器

```
private void initHandlerMappings(ApplicationContext context) {
        //初始化记录 HandlerMapping 对象的属性变量为null
		this.handlerMappings = null;
		//根据属性 detectAllHandlerMappings 决定是检测所有的 HandlerMapping对象,还是使用指定名称的 HandlerMapping； 对象默认是true按照类型获取handlerMapping组件
		if (this.detectAllHandlerMappings) {
			// Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList<>(matchingBeans.values());
				// We keep HandlerMappings in sorted order.
				//排序，关于这里的排序，可以参考   WebMvcConfigurationSupport 类中对各种 HandlerMapping bean进行定义时所使用的 order 属性，顺序属性很关键，因为它涉及到 HandlerMapping 使用时的优先级
				AnnotationAwareOrderComparator.sort(this.handlerMappings);
			}
		}
		else { //获取名称为  handlerMapping 的 HandlerMapping bean 并记录到 handlerMappings
			try {
				HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
				this.handlerMappings = Collections.singletonList(hm);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerMapping later.
			}
		}

		// Ensure we have at least one HandlerMapping, by registering
		// a default HandlerMapping if no other mappings are found.
		// 如果上面步骤从容器获取 HandlerMapping 失败，则使用缺省策略创建 HandlerMapping 对象记录到handlerMappings
		if (this.handlerMappings == null) {
			this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerMappings declared for servlet '" + getServletName() +"': using default strategies from DispatcherServlet.properties");
			}
		}
	}

```

### initHandlerAdapters(适配器)

```
private void initHandlerAdapters(ApplicationContext context) {
        // 初始化记录 handlerAdapters 对象的属性变量为null 初始化记录 handlerAdap 初始化记录 handlerAdapters 对象的属性变量为null   ters 对象的属性变量为null
		this.handlerAdapters = null;
        //根据属性 detectAllHandlerAdapters 决定是检测所有的 HandlerAdapter 对象，还是使用指定名称的 HandlerAdapter 对象
		if (this.detectAllHandlerAdapters) {
			// Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
			//从容器及其祖先容器查找所有类型为 HandlerAdapter 的 HandlerAdapter 对象，记录到   handlerAdapters 并排序
			Map<String, HandlerAdapter> matchingBeans =BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerAdapters = new ArrayList<>(matchingBeans.values());
				// We keep HandlerAdapters in sorted order.
				// 排序
				AnnotationAwareOrderComparator.sort(this.handlerAdapters);
			}
		}
		else {
			try {
			    //获取名称为  handlerAdapter 的 HandlerAdapter bean 并记录到 handlerAdapters
				HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
				this.handlerAdapters = Collections.singletonList(ha);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerAdapter later.
			}
		}

		// Ensure we have at least some HandlerAdapters, by registering
		// default HandlerAdapters if no other adapters are found.
		// 如果上面步骤从容器获取 HandlerAdapter 失败，则使用缺省策略创建 HandlerAdapter 对象记录到handlerAdapters
		// 默认有三种: 1.HttpRequestHandlerAdapter; 2.SimpleControllerHandlerAdapter; 3.RequestMappingHandlerAdapter
		if (this.handlerAdapters == null) {
			this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerAdapters declared for servlet '" + getServletName() +"': using default strategies from DispatcherServlet.properties");
			}
		}
	}

```

### initHandlerExceptionResolvers

此组件的作用是根据异常设置ModelAndView，之后再交给render方法进行渲染。

``` 
private void initHandlerExceptionResolvers(ApplicationContext context) {
        //初始化记录 handlerExceptionResolvers 对象的属性变量为null
		this.handlerExceptionResolvers = null;
        //根据属性 detectAllHandlerExceptionResolvers 决定是检测所有的 HandlerExceptionResolver 对象，还是使用指定名称的 HandlerExceptionResolver 对象
		if (this.detectAllHandlerExceptionResolvers) {
			// Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
				// We keep HandlerExceptionResolvers in sorted order.
				// 排序
				AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
			}
		}
		else {
			try {
			    //获取名称为  handlerExceptionResolver 的 HandlerExceptionResolver bean 并记录到 handlerExceptionResolvers
				HandlerExceptionResolver her =context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
				this.handlerExceptionResolvers = Collections.singletonList(her);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, no HandlerExceptionResolver is fine too.
			}
		}
		// 如果上面步骤从容器获取 HandlerExceptionResolver 失败，则使用缺省策略创建 HandlerExceptionResolver 对象记录到 handlerExceptionResolvers	
		// 默认有三种: 1.ExceptionHandlerExceptionResolver 2.ResponseStatusExceptionResolver 3.DefaultHandlerExceptionResolver
		if (this.handlerExceptionResolvers == null) {
			this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerExceptionResolvers declared in servlet '" + getServletName() +"': using default strategies from DispatcherServlet.properties");
			}
		}
	}


```

### initRequestToViewNameTranslator

Spring MVC是通过ViewName来找到对应的视图的，而此接口的作用就是从request中获取viewName。

```
public interface RequestToViewNameTranslator {
	@Nullable
	String getViewName(HttpServletRequest request) throws Exception;
}

```
- 子类：DefaultRequestToViewNameTranslator

```
   /**
	 * Translates the request URI of the incoming {@link HttpServletRequest}
	 * into the view name based on the configured parameters.
	 * @see org.springframework.web.util.UrlPathHelper#getLookupPathForRequest
	 * @see #transformPath
	 */
	@Override
	public String getViewName(HttpServletRequest request) {
		String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
		return (this.prefix + transformPath(lookupPath) + this.suffix);
	}
	
	   /**
    	 * Transform the request URI (in the context of the webapp) stripping
    	 * slashes and extensions, and replacing the separator as required.
    	 * @param lookupPath the lookup path for the current request,as determined by the UrlPathHelper
    	 * @return the transformed path, with slashes and extensions stripped if desired
    	 */
    	@Nullable
    	protected String transformPath(String lookupPath) {
    		String path = lookupPath;
    		if (this.stripLeadingSlash && path.startsWith(SLASH)) {
    			path = path.substring(1);
    		}
    		if (this.stripTrailingSlash && path.endsWith(SLASH)) {
    			path = path.substring(0, path.length() - 1);
    		}
    		if (this.stripExtension) {
    			path = StringUtils.stripFilenameExtension(path);
    		}
    		if (!SLASH.equals(this.separator)) {
    			path = StringUtils.replace(path, SLASH, this.separator);
    		}
    		return path;
    	}

```

### initViewResolvers

```
public interface ViewResolver {
	@Nullable
	View resolveViewName(String viewName, Locale locale) throws Exception;
}

```
- public abstract class AbstractCachingViewResolver extends WebApplicationObjectSupport implements ViewResolver //基于缓存的抽象视图解析器

- public class UrlBasedViewResolver extends AbstractCachingViewResolver implements Ordered//实现了缓存,提供了prefix suffix拼接的url视图解析器

- public class InternalResourceViewResolver extends UrlBasedViewResolver//基于url的内部资源视图解析器

- public class XmlViewResolver extends AbstractCachingViewResolver implements Ordered, InitializingBean, DisposableBean //基于xml的缓存视图解析器

- public class BeanNameViewResolver extends WebApplicationObjectSupport implements ViewResolver, Ordered // beanName来自容器,并且不支持缓存

- public class ResourceBundleViewResolver extends AbstractCachingViewResolver implements Ordered, InitializingBean, DisposableBean // 资源配置文件

- public class FreeMarkerViewResolver extends AbstractTemplateViewResolver //freeMarkerView

- public class AjaxThymeleafViewResolver extends ThymeleafViewResolver//AjaxThymeleaf

### initFlashMapManager

```
public interface FlashMapManager {

	/**
	 * Find a FlashMap saved by a previous request that matches to the current
	 * request, remove it from underlying storage, and also remove other
	 * expired FlashMap instances.
	 * <p>This method is invoked in the beginning of every request in contrast
	 * to {@link #saveOutputFlashMap}, which is invoked only when there are
	 * flash attributes to be saved - i.e. before a redirect.
	 * @param request the current request
	 * @param response the current response
	 * @return a FlashMap matching the current request or {@code null}
	 */
	@Nullable
	FlashMap retrieveAndUpdate(HttpServletRequest request, HttpServletResponse response);

	/**
	 * Save the given FlashMap, in some underlying storage and set the start
	 * of its expiration period.
	 * <p><strong>NOTE:</strong> Invoke this method prior to a redirect in order
	 * to allow saving the FlashMap in the HTTP session or in a response
	 * cookie before the response is committed.
	 * @param flashMap the FlashMap to save
	 * @param request the current request
	 * @param response the current response
	 */
	void saveOutputFlashMap(FlashMap flashMap, HttpServletRequest request, HttpServletResponse response);




```

##### 获取默认的组件(handlerMapping,handlerAdapter,handlerExceptionResolvers,viewResolvers,themResolver,localResolver,FlashMapManager,RequestToViewNameTranslator)
```
//该方法使用指定的策略接口 strategyInterface 创建一组策略对象。上面的方法initHandlerMappings就是使用该方法创建了一组缺省的HandlerMapping策略对象	
protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
        //策略接口长名称作为 key
		String key = strategyInterface.getName();
		//这里 defaultStrategies 是一个类静态属性，指向classpath resource 文件 DispatcherServlet.properties 该行获取策略接口对应的实现类,是','分割的实现类的长名称
		String value = defaultStrategies.getProperty(key);
		if (value != null) {
			String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
			List<T> strategies = new ArrayList<>(classNames.length);
			for (String className : classNames) {
				try {
				    // 获取策略接口实现类
					Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
					//创建该策略接口实现类的对象
					Object strategy = createDefaultStrategy(context, clazz);
					strategies.add((T) strategy);
				}
				catch (ClassNotFoundException ex) {
					throw new BeanInitializationException("Could not find DispatcherServlet's default strategy class [" + className +"] for interface [" + key + "]", ex);
				}
				catch (LinkageError err) {
					throw new BeanInitializationException("Unresolvable class definition for DispatcherServlet's default strategy class [" +className + "] for interface [" + key + "]", err);
				}
			}
			return strategies;
		}
		else {
			return new LinkedList<>();
		}
	}

```
