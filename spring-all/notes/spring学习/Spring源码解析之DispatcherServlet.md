## SpringBoot注册DispatcherServlet

### DispatcherServletAutoConfiguration

 ````
package org.springframework.boot.autoconfigure.web.servlet;

// 省略 imports

/**
 * EnableAutoConfiguration Auto-configuration for the Spring
 * DispatcherServlet. Should work for a standalone application where an embedded
 * web server is already present and also for a deployable application using
 * SpringBootServletInitializer.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Stephane Nicoll
 * @author Brian Clozel
 */
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
// 仅在当前应用是一个 Servlet Web 应用时才生效
@ConditionalOnWebApplication(type = Type.SERVLET)
// 仅在类 DispatcherServlet 存在于 classpath 上时才生效
@ConditionalOnClass(DispatcherServlet.class)
// 在自动配置类 ServletWebServerFactoryAutoConfiguration 应用之后再应用
@AutoConfigureAfter(ServletWebServerFactoryAutoConfiguration.class)
public class DispatcherServletAutoConfiguration {

	/*
	 * The bean name for a DispatcherServlet that will be mapped to the root URL "/"
	 */
	public static final String DEFAULT_DISPATCHER_SERVLET_BEAN_NAME = "dispatcherServlet";

	/*
	 * The bean name for a ServletRegistrationBean for the DispatcherServlet "/"
	 */
	public static final String DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME = 
		"dispatcherServletRegistration";

	// 嵌套配置类
	// 1. 定义 bean DispatcherServlet dispatcherServlet
	// 2. 如果类型为 MultipartResolver 的 bean 存在，为其创建一个别名 multipartResolver
	@Configuration
	// 仅在条件 DefaultDispatcherServletCondition 被满足时才生效 :
	// DefaultDispatcherServletCondition 在类型为 DispatcherServlet 或者名称为 dispatcherServlet
	// 的 bean 不存在时才被满足
	@Conditional(DefaultDispatcherServletCondition.class)
	// 仅在类 ServletRegistration 存在于 classpath 上时才生效
	@ConditionalOnClass(ServletRegistration.class)
	// 确保前缀为 spring.http 的配置参数被加载到 bean HttpProperties
	// 确保前缀为 spring.mvc 的配置参数被加载到 bean WebMvcProperties    
	@EnableConfigurationProperties({ HttpProperties.class, WebMvcProperties.class })
	protected static class DispatcherServletConfiguration {

		private final HttpProperties httpProperties;

		private final WebMvcProperties webMvcProperties;

		public DispatcherServletConfiguration(HttpProperties httpProperties, 
			WebMvcProperties webMvcProperties) {
			this.httpProperties = httpProperties;
			this.webMvcProperties = webMvcProperties;
		}

		 // 定义 bean DispatcherServlet, 使用名称 dispatcherServlet， 这是 Spring MVC 核心的
		 // 前置控制器Servlet，Spring MVC 定义的所有的控制器方法都最终经由该 DispatcherServlet 派发
		@Bean(name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
		public DispatcherServlet dispatcherServlet() {
			DispatcherServlet dispatcherServlet = new DispatcherServlet();
			dispatcherServlet.setDispatchOptionsRequest(
				this.webMvcProperties.isDispatchOptionsRequest());
			dispatcherServlet.setDispatchTraceRequest(
				this.webMvcProperties.isDispatchTraceRequest());
			dispatcherServlet.setThrowExceptionIfNoHandlerFound(
					this.webMvcProperties.isThrowExceptionIfNoHandlerFound());
			dispatcherServlet.setEnableLoggingRequestDetails(
					this.httpProperties.isLogRequestDetails());
			return dispatcherServlet;
		}

		 // 定义 bean MultipartResolver，使用名称 multipartResolver,
		// 该 bean 定义的主要任务不是生成一个新的 MultipartResolver bean 实例，而是在该类型的
		// bean 存在时，给它起一个别名 multipartResolver，因为 DispatcherServlet 内部会固定使用该名字
		@Bean
		// 仅在类型为  MultipartResolver 的 bean 存在时才生效
		@ConditionalOnBean(MultipartResolver.class)
		// 仅在名称为 multipartResolver 的 bean 不存在时才生效
		@ConditionalOnMissingBean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
		public MultipartResolver multipartResolver(MultipartResolver resolver) {
			// Detect if the user has created a MultipartResolver but named it incorrectly
			return resolver;
		}

	}

	// 嵌套配置类
	@Configuration
	// 仅在条件 DispatcherServletRegistrationCondition 满足时才生效
	@Conditional(DispatcherServletRegistrationCondition.class)
	// 仅在类 ServletRegistration 存在于 classpath 上时才生效
	@ConditionalOnClass(ServletRegistration.class)
	// 确保前缀为 spring.mvc 的配置参数被加载到 bean WebMvcProperties
	@EnableConfigurationProperties(WebMvcProperties.class)
	// 导入配置类 DispatcherServletConfiguration
	@Import(DispatcherServletConfiguration.class)
	protected static class DispatcherServletRegistrationConfiguration {

		private final WebMvcProperties webMvcProperties;

		private final MultipartConfigElement multipartConfig;

		// MultipartConfigElement 的配置可以参考 MultipartAutoConfiguration
		public DispatcherServletRegistrationConfiguration(WebMvcProperties webMvcProperties,
				ObjectProvider<MultipartConfigElement> multipartConfigProvider) {
			this.webMvcProperties = webMvcProperties;
			this.multipartConfig = multipartConfigProvider.getIfAvailable();
		}

		// 定义 bean DispatcherServletRegistrationBean，名称为 dispatcherServletRegistration
		 // DispatcherServletRegistrationBean 继承自 ServletRegistrationBean,
		 // 该 bean dispatcherServletRegistration 的目的是将 DispatcherServletConfiguration 配置类中
		// 所定义的 bean DispatcherServlet dispatcherServlet 注册到 Servlet 容器
		@Bean(name = DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
		// 仅在类型为  DispatcherServlet，名称为 dispatcherServlet 的 bean 存在时才生效
		@ConditionalOnBean(value = DispatcherServlet.class, name = 
				DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
		public DispatcherServletRegistrationBean dispatcherServletRegistration(
				DispatcherServlet dispatcherServlet) {
			DispatcherServletRegistrationBean registration = 
					new DispatcherServletRegistrationBean(dispatcherServlet,
					this.webMvcProperties.getServlet().getPath());
			registration.setName(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
			registration.setLoadOnStartup(this.webMvcProperties.getServlet().getLoadOnStartup());
			if (this.multipartConfig != null) {
				registration.setMultipartConfig(this.multipartConfig);
			}
			return registration;
		}

	}

	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	private static class DefaultDispatcherServletCondition extends SpringBootCondition {

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, 
				AnnotatedTypeMetadata metadata) {
			ConditionMessage.Builder message = 
					ConditionMessage.forCondition("Default DispatcherServlet");
			ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
			List<String> dispatchServletBeans = Arrays
					.asList(beanFactory.getBeanNamesForType(DispatcherServlet.class, false, false));
			if (dispatchServletBeans.contains(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)) {
				return ConditionOutcome
						.noMatch(message.found("dispatcher servlet bean")
						.items(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
			}
			if (beanFactory.containsBean(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)) {
				return ConditionOutcome.noMatch(
						message.found("non dispatcher servlet bean")
						.items(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
			}
			if (dispatchServletBeans.isEmpty()) {
				return ConditionOutcome.match(message.didNotFind("dispatcher servlet beans")
				.atAll());
			}
			return ConditionOutcome.match(
				message.found("dispatcher servlet bean", "dispatcher servlet beans")
					.items(Style.QUOTE, dispatchServletBeans)
					.append("and none is named " + DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
		}

	}

	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	private static class DispatcherServletRegistrationCondition extends SpringBootCondition {

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, 
				AnnotatedTypeMetadata metadata) {
			ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
			ConditionOutcome outcome = checkDefaultDispatcherName(beanFactory);
			if (!outcome.isMatch()) {
				return outcome;
			}
			return checkServletRegistration(beanFactory);
		}

		private ConditionOutcome checkDefaultDispatcherName(
				ConfigurableListableBeanFactory beanFactory) {
			List<String> servlets = Arrays
					.asList(beanFactory.getBeanNamesForType(DispatcherServlet.class, false, false));
			boolean containsDispatcherBean = beanFactory.containsBean(
					DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
			if (containsDispatcherBean && 
				!servlets.contains(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)) {
				return ConditionOutcome.noMatch(
						startMessage().found("non dispatcher servlet")
						.items(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
			}
			return ConditionOutcome.match();
		}

		private ConditionOutcome checkServletRegistration(
				ConfigurableListableBeanFactory beanFactory) {
			ConditionMessage.Builder message = startMessage();
			List<String> registrations = Arrays
					.asList(beanFactory.getBeanNamesForType(
										ServletRegistrationBean.class, false, false));
			boolean containsDispatcherRegistrationBean = beanFactory
					.containsBean(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
			if (registrations.isEmpty()) {
				if (containsDispatcherRegistrationBean) {
					return ConditionOutcome.noMatch(message.found("non servlet registration bean")
							.items(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
				}
				return ConditionOutcome.match(message.didNotFind("servlet registration bean")
					.atAll());
			}
			if (registrations.contains(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)) {
				return ConditionOutcome.noMatch(message.found("servlet registration bean")
						.items(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
			}
			if (containsDispatcherRegistrationBean) {
				return ConditionOutcome.noMatch(message.found("non servlet registration bean")
						.items(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
			}
			return ConditionOutcome.match(message.found("servlet registration beans")
					.items(Style.QUOTE, registrations)
					.append("and none is named " 
							+ DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
		}

		private ConditionMessage.Builder startMessage() {
			return ConditionMessage.forCondition("DispatcherServlet Registration");
		}

	}

}


 
````