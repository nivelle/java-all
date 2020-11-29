
### @EnableWebMvc

```
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
//@EnableWebMvc仅仅导入一个类DelegatingWebMvcConfiguration,而DelegatingWebMvcConfiguration则继承自WebMvcConfigurationSupport。
//WebMvcConfigurationSupport的作用是提供缺省的Spring MVC配置，具体体现形式是向容器登记一组Spring MVC运行时使用的bean。
@Import(DelegatingWebMvcConfiguration.class) 
public @interface EnableWebMvc {
}
```
### DelegatingWebMvcConfiguration

````

public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {

	private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();


	@Autowired(required = false)
	public void setConfigurers(List<WebMvcConfigurer> configurers) {
		if (!CollectionUtils.isEmpty(configurers)) {
			this.configurers.addWebMvcConfigurers(configurers);
		}
	}
	
    @Nullable
   	private ApplicationContext applicationContext;//当前类实现了接口ApplicationContextAware用于记录ApplicationContext，该变量就是对应该接口用于记录ApplicationContext的变量
   
   	@Nullable
   	private ServletContext servletContext;//当前类实现了接口ServletContextAware用于记录ServletContext，该变量就是对应该接口用于记录ServletContext的变量
   
	
}

```


### WebMvcConfigurationSupport