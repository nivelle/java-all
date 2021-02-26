#### AOP实现自动注解
#### EnableAspectJAutoProxy 开启AOP的注解
````
@EnableAspectJAutoProxy
@Import(AspectJAutoProxyRegistrar.class) implement ImportBeanDefinitionRegistrar
````
##### registerBeanDefinitions();

###### 目的是创建类: AnnotationAwareAspectJAutoProxyCreator 其实也就是在refresh()中创建 [refresh中 registerBeanPostProcessors 中处理 ](./Spring源码解析之refresh()方法.md)

##### 创建 AspectJAnnotationAutoProxyCreator

[![yz3Bwj.md.png](https://s3.ax1x.com/2021/02/26/yz3Bwj.md.png)](https://imgtu.com/i/yz3Bwj)

#### 第一步：注册: 

- 一个AOP的工具类,这个工具类的主要作用是把AnnotationAwareAspectJAutoProxyCreator这个类定义为BeanDefinition放到spring容器中,这是通过实现ImportBeanDefinitionRegistrar接口来装载的
  
- AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);
  
    - registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry, null);
````
Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			BeanDefinition apcDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
			if (!cls.getName().equals(apcDefinition.getBeanClassName())) {
				int currentPriority = findPriorityForClass(apcDefinition.getBeanClassName());
				int requiredPriority = findPriorityForClass(cls);
				//三种:如果有权限大于当前AnnotationAwareAspectJAutoProxyCreator的类则使用权限大的类
				if (currentPriority < requiredPriority) {
					apcDefinition.setBeanClassName(cls.getName());
				}
			}
			return null;
		}
		RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
		beanDefinition.setSource(source);
		beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);//值最小但是排序最靠前
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);//角色设置为基础设施类 ROLE_APPLICATION=0；ROLE_SUPPORT=1；ROLE_INFRASTRUCTURE = 2
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
		return beanDefinition;

````

- findPriorityForClass

````
static {
		// Set up the escalation list...
		APC_PRIORITY_LIST.add(InfrastructureAdvisorAutoProxyCreator.class);
		APC_PRIORITY_LIST.add(AspectJAwareAdvisorAutoProxyCreator.class);
		APC_PRIORITY_LIST.add(AnnotationAwareAspectJAutoProxyCreator.class);
	}

````

#### 第二步：获取注解上的参数

- AnnotationAttributes enableAspectJAutoProxy =AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);

````
 enableAspectJAutoProxy!=null

    - AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);

    - definition.getPropertyValues().add("proxyTargetClass", Boolean.TRUE);

    - AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);

    - definition.getPropertyValues().add("exposeProxy", Boolean.TRUE);

````

  - proxyTargetClass:控制aop的具体实现方式,为true 的话使用cglib,为false的话使用java的Proxy,默认为false
  
  - exposeProxy:控制代理的暴露方式,解决内部调用不能使用代理的场景，默认为false.

-----

#### AnnotationAwareAspectJAutoProxyCreator

#### 子类: AbstractAutoProxyCreator extends ProxyProcessorSupport implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware

-  public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException

````

 if (!StringUtils.hasLength(beanName) || !this.targetSourcedBeans.contains(beanName)) {
            ////当前bean是否已经存在被增强的bean当中,如果已经存在直接返回。(advicebean中保存了所以已经被增强的bean)
            if (this.advisedBeans.containsKey(cacheKey)) {
                return null;
            }
            //isInfrastructureClass(beanClass)判断是否是切面的基础类如：Advice Pointcut Advisor AopInfrastructureBean 否是切面（@Aspect）
            //shouldSkip(beanClass, beanName)是否需要跳过
            if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
                //添加进advisedBeansConcurrentHashMap<k=Object,v=Boolean>
                //标记是否需要增强实现，这里基础构建bean不需要代理，都置为false，供后面postProcessAfterInitialization实例化后使用。
                this.advisedBeans.put(cacheKey, Boolean.FALSE);
                return null;
            }
        }
        // Create proxy here if we have a custom TargetSource.
        // Suppresses unnecessary default instantiation of the target bean:
        // The TargetSource will handle target instances in a custom fashion.
        TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
        if (targetSource != null) {
            if (StringUtils.hasLength(beanName)) {
                this.targetSourcedBeans.add(beanName);
            }
            //获取切面
            Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
            Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
            this.proxyTypes.put(cacheKey, proxy.getClass());
            return proxy;
        }

        return null;

````

- public boolean postProcessAfterInstantiation(Object bean, String beanName); //当postProcessBeforeInstantiation返回的不为null 的时候才会执行下面的方法

- public Object postProcessBeforeInitialization(Object bean, String beanName);

``````

因为尝试返回一个代理对象失败，所以将执行doCreateBean方法。创建一个bean。
然后在创建完成初始化之前会调用applyBeanPostProcessorsBeforeInitialization方法，这个方法的内部就是，遍历所有后置处理器调用他们的postProcessBeforeInitialization方法。
当然也会调用AnnotationAwareAspectJAutoProxyCreator的postProcessBeforeInitialization方法。他就会执行在每次创建bean的时候执行下面操作。

``````

- public Object postProcessAfterInitialization(@Nullable Object bean, String beanName);

- 在调用完invokeInitMethods(beanName, wrappedBean, mbd); 初始化方法之后，也会遍历所有的后置处理器，执行他们的后置方法。
  所以每个bean创建的时候都会调用AnnotationAwareAspectJAutoProxyCreator的postProcessAfterInitialization方法。在这里也就是尝试创建一个代理对象。

````
if (bean != null) {
			Object cacheKey = getCacheKey(bean.getClass(), beanName);
			if (this.earlyProxyReferences.remove(cacheKey) != bean) {
				return wrapIfNecessary(bean, beanName, cacheKey);
			}
		}
		return bean;
````

````
protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
        // 如果是用户自定义获取实例,不需要增强处理，直接返回
		if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
			return bean;
		}
		//查询map缓存，标记过false,不需要增强直接返回
		if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
			return bean;
		}
		//判断一遍springAOP基础构建类，标记过false,不需要增强直接返回
		if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
			this.advisedBeans.put(cacheKey, Boolean.FALSE);
			return bean;
		}

		// Create proxy if we have advice.
		// 获得当前所有的增强器找到候选的增强器 并且获得能被使用的增强器。返回排好序的增强器链
		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
		// 如果存在增强
		if (specificInterceptors != DO_NOT_PROXY) {
		    //标记增强为TRUE,表示需要增强实现
			this.advisedBeans.put(cacheKey, Boolean.TRUE);
			//生成增强代理类
			Object proxy = createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
			this.proxyTypes.put(cacheKey, proxy.getClass());
			return proxy;
		}
        //如果不存在增强，标记false,作为缓存，再次进入提高效率
		this.advisedBeans.put(cacheKey, Boolean.FALSE);
		return bean;
	}

````


#### protected Object createProxy(Class<?> beanClass, @Nullable String beanName,@Nullable Object[] specificInterceptors, TargetSource targetSource)

````
	protected Object createProxy(Class<?> beanClass, @Nullable String beanName,
			@Nullable Object[] specificInterceptors, TargetSource targetSource) {

		if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
		    //如果是ConfigurableListableBeanFactory接口（咱们DefaultListableBeanFactory就是该接口的实现类）则，暴露目标类
		    //给beanFactory->beanDefinition定义一个属性：k=AutoProxyUtils.originalTargetClass,v=需要被代理的bean class
			AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName, beanClass);
		}

		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.copyFrom(this);
        //如果不是代理目标类,如果beanFactory定义了代理目标类（CGLIB）
		if (!proxyFactory.isProxyTargetClass()) {
		    //如果beanFactory定义了代理目标类（CGLIB）
			if (shouldProxyTargetClass(beanClass, beanName)) {
				proxyFactory.setProxyTargetClass(true);//代理工厂设置代理目标类
			}
			else {
			    //否则设置代理接口（JDK）
				evaluateProxyInterfaces(beanClass, proxyFactory);
			}
		}
        //把拦截器包装成增强（通知）
		Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
		//设置进代理工厂
		proxyFactory.addAdvisors(advisors);
		proxyFactory.setTargetSource(targetSource);
		//空方法，留给子类拓展用，典型的spring的风格，喜欢处处留后路
		customizeProxyFactory(proxyFactory);
        //用于控制代理工厂是否还允许再次添加通知，默认为false（表示不允许）
		proxyFactory.setFrozen(this.freezeProxy);
		if (advisorsPreFiltered()) {//默认false，上面已经前置过滤了匹配的增强Advisor
			proxyFactory.setPreFiltered(true);
		}
        //代理工厂获取代理对象的核心方法
		return proxyFactory.getProxy(getProxyClassLoader());
	}
````

##### proxyFactory.getProxy 创建代理类

````
public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
        if (NativeDetector.inNativeImage() || !config.isOptimize() && !config.isProxyTargetClass() && !this.hasNoUserSuppliedProxyInterfaces(config)) {
            return new JdkDynamicAopProxy(config);
        } else {
            Class<?> targetClass = config.getTargetClass();
            if (targetClass == null) {
                throw new AopConfigException("TargetSource cannot determine target class: Either an interface or a target is required for proxy creation.");
            } else {
                return (AopProxy)(!targetClass.isInterface() && !Proxy.isProxyClass(targetClass) ? new ObjenesisCglibAopProxy(config) : new JdkDynamicAopProxy(config));
            }
        }
    }

````