
### springBoot aop自动注解

````
package org.springframework.boot.autoconfigure.aop;
/**
 * org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration for Spring's AOP support. Equivalent to enabling
 * org.springframework.context.annotation.EnableAspectJAutoProxy in your
 * configuration.
 * 
 * The configuration will not be activated if spring.aop.auto=false. The
 * proxyTargetClass attribute will be true, by default, but can be
 * overridden by specifying spring.aop.proxy-target-class=false.
 *
 */
@Configuration
// 仅在这些类存在于 classpath 时生效
@ConditionalOnClass({ EnableAspectJAutoProxy.class, Aspect.class, Advice.class,AnnotatedElement.class })
// 仅在属性 spring.aop.auto 缺失或者明确指定为 true 时生效         
@ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
public class AopAutoConfiguration {

	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = false)
	// 在配置参数 spring.aop.proxy-target-class 值被明确设置为 false 时生效
	@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", 
		havingValue = "false", matchIfMissing = false)
	public static class JdkDynamicAutoProxyConfiguration {

	}

    
	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = true)
	// 在配置参数 spring.aop.proxy-target-class 缺失或者值被明确设置为 true 时生效    
	@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", 
		havingValue = "true", matchIfMissing = true)
	public static class CglibAutoProxyConfiguration {

	}

}

````

#### @EnableAspectJAutoProxy

##### 通过AopConfigUtils注册了一个AOP代理对象创建器bean到容器:

- bean名称: org.springframework.aop.config.internalAutoProxyCreator

- bean类型: AnnotationAwareAspectJAutoProxyCreator

- 属性 order：HIGHEST_PRECEDENCE

- bean角色：ROLE_INFRASTRUCTURE (基础设施)



````
class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

	/**
	 * Register, escalate, and configure the AspectJ auto proxy creator based on the value
	 * of the @{@link EnableAspectJAutoProxy#proxyTargetClass()} attribute on the importing
	 * {@code @Configuration} class.
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
       
        //创建名字为internalAutoProxyCreator的bean AnnotationAwareAspectJAutoProxyCreator
		AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);

		AnnotationAttributes enableAspectJAutoProxy = AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
		if (enableAspectJAutoProxy != null) {
			if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}

}

````

#### AnnotationAwareAspectJAutoProxyCreator //创建代理类

````

@SuppressWarnings("serial")
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {

	@Nullable
	private List<Pattern> includePatterns;

	@Nullable
	private AspectJAdvisorFactory aspectJAdvisorFactory;

	@Nullable
	private BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;


	/**
	 * Set a list of regex patterns, matching eligible @AspectJ bean names.
	 * Default is to consider all @AspectJ beans as eligible.
	 */
	public void setIncludePatterns(List<String> patterns) {
		this.includePatterns = new ArrayList<>(patterns.size());
		for (String patternText : patterns) {
			this.includePatterns.add(Pattern.compile(patternText));
		}
	}

	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
		Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory must not be null");
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}

	@Override
	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// 使用基类方法初始化 beanFactory,真正的逻辑时构造一个能够从容器中获取所有
		// Spring Advisor bean的BeanFactoryAdvisorRetrievalHelper
		super.initBeanFactory(beanFactory);

		// 下面的逻辑是构造 aspectJAdvisorsBuilder , 用于通过反射方法从容器中获取
		// 所有带有@AspectJ注解的 beans
		if (this.aspectJAdvisorFactory == null) {
			this.aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory(beanFactory);
		}
		this.aspectJAdvisorsBuilder =
				new BeanFactoryAspectJAdvisorsBuilderAdapter(beanFactory, this.aspectJAdvisorFactory);
	}


	// 找到容器中所有的Spring Advisor beans 和 @AspectJ 注解的 beans，都封装成
	// Advisor 形式返回一个列表
	@Override
	protected List<Advisor> findCandidateAdvisors() {
		// Add all the Spring advisors found according to superclass rules.
		// 使用基类Spring Advisor查找机制查找容器中所有的Spring Advisor
		// 其实就是父类使用所构造的BeanFactoryAdvisorRetrievalHelper从
		// 容器中获取所有的Spring Advisor beans。
		List<Advisor> advisors = super.findCandidateAdvisors();
		// Build Advisors for all AspectJ aspects in the bean factory.
		// 使用aspectJAdvisorsBuilder从容器中获取所有@AspectJ 注解的bean，并将它们
		// 包装成Advisor
		if (this.aspectJAdvisorsBuilder != null) {
			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		}

		// 现在列表advisors包含容器中所有的Spring Advisor beans 和 @AspectJ 注解的 beans,
		// 现在它们都以 Advisor 的形式存在
		return advisors;
	}

	// 判断指定的类是否是一个基础设置类:
	// 1. 如果父类的 isInfrastructureClass 断定它是一个基础设施类，就认为它是;
	// 2. 如果这是一个@Aspect注解的类，也认为这是一个基础设施类;
	// 一旦某个bean类被认为是基础设施类，那么将不会对该类实施代理机制	
	@Override
	protected boolean isInfrastructureClass(Class<?> beanClass) {
		// Previously we setProxyTargetClass(true) in the constructor, but that has too
		// broad an impact. Instead we now override isInfrastructureClass to avoid proxying
		// aspects. I'm not entirely happy with that as there is no good reason not
		// to advise aspects, except that it causes advice invocation to go through a
		// proxy, and if the aspect implements e.g the Ordered interface it will be
		// proxied by that interface and fail at runtime as the advice method is not
		// defined on the interface. We could potentially relax the restriction about
		// not advising aspects in the future.
		return (super.isInfrastructureClass(beanClass) || (this.aspectJAdvisorFactory != null && this.aspectJAdvisorFactory.isAspect(beanClass)));
	}

	/**
	 * Check whether the given aspect bean is eligible for auto-proxying.
	 * If no aop:include elements were used then "includePatterns" will be
	 * null and all beans are included. If "includePatterns" is non-null,
	 * then one of the patterns must match.
	 * 检查给定名称的bean是否是符合条件的Aspect bean。这里是依据属性 includePatterns 做检查的，
	 * 当 includePatterns 属性为 null 时，总是返回 true。
	 */
	protected boolean isEligibleAspectBean(String beanName) {
		if (this.includePatterns == null) {
			return true;
		}
		else {
			for (Pattern pattern : this.includePatterns) {
				if (pattern.matcher(beanName).matches()) {
					return true;
				}
			}
			return false;
		}
	}


	/**
	 * Subclass of BeanFactoryAspectJAdvisorsBuilderAdapter that delegates to
	 * surrounding AnnotationAwareAspectJAutoProxyCreator facilities.
	 */
	private class BeanFactoryAspectJAdvisorsBuilderAdapter extends BeanFactoryAspectJAdvisorsBuilder {

		public BeanFactoryAspectJAdvisorsBuilderAdapter(ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {

			super(beanFactory, advisorFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AnnotationAwareAspectJAutoProxyCreator.this.isEligibleAspectBean(beanName);
		}
	}

}


````