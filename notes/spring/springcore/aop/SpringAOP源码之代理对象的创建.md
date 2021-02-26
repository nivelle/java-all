### 1、postProcessBeforeInstantiation方法

- 创建bean之前会先尝试返回一个代理对象而执行resolveBeforeInstantiation操作。该方法就会遍历所有后置处理器，调用InstantiationAwareBeanPostProcessor类型的后置处理器的postProcessBeforeInstantiation方法。

- 正好容器中@EnableAspectJAutoProxy为我们添加了该类型的后置处理器。所以每次单实例bean创建的时候都会调用该方法。

````
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		// 先尝试从缓存中获得该bean
		Object cacheKey = getCacheKey(beanClass, beanName);
		if (beanName == null || !this.targetSourcedBeans.contains(beanName)) {
			// 当前bean是否已经存在被增强的bean当中。(advicebean中保存了所以已经被增强的bean)
			if (this.advisedBeans.containsKey(cacheKey)) {
				return null;
			}
			//回调子类重写的方法
			//isInfrastructureClass(beanClass)判断是否是切面的基础类如:Advice Pointcut Advisor AopInfrastructureBean 否是切面（@Aspect）
			//shouldSkip(beanClass,beanName)是否需要跳过
		    //1、获得所有的增强器List<Advisor>遍历并且判断类型
			if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
				this.advisedBeans.put(cacheKey, Boolean.FALSE);
				return null;
			}
			if (beanName != null) {
			TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
			if (targetSource != null) {
				this.targetSourcedBeans.add(beanName);
				// 返回指定类是否需要增强
				Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
				Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
				this.proxyTypes.put(cacheKey, proxy.getClass());
				return proxy;
			}
		}
		//返回null
		return null;
	}

		}

````

- 2、postProcessAfterInstantiation方法

当postProcessBeforeInstantiation返回的不为null 的时候才会执行下面的方法（创建Machine 对象时返回的是null所有不会调用）

````
public boolean postProcessAfterInstantiation(Object bean, String beanName) {
		return true;
	}

````
- 3、postProcessBeforeInitialization方法

因为尝试返回一个代理对象失败，所以将执行doCreateBean方法。创建一个bean。然后在创建完成初始化之前会调用
applyBeanPostProcessorsBeforeInitialization方法，这个方法的内部就是，遍历所有后置处理器调用他们的postProcessBeforeInitialization方法。当然也会调用AnnotationAwareAspectJAutoProxyCreator的postProcessBeforeInitialization方法。他就会执行在每次创建bean的时候执行下面操作。

``````
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

``````

- 4、postProcessAfterInitialization方法

在调用完invokeInitMethods(beanName, wrappedBean, mbd); 初始化方法之后，也会遍历所有的后置处理器，执行他们的后置方法。所以每个bean创建的时候都会调用AnnotationAwareAspectJAutoProxyCreator的postProcessAfterInitialization方法。在这里也就是尝试创建一个代理对象。
前面都是准备工作。

``````
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean != null) {
			Object cacheKey = getCacheKey(bean.getClass(), beanName);
			if (!this.earlyProxyReferences.contains(cacheKey)) {
				// 尝试进行包装 返回代理对象
				return wrapIfNecessary(bean, beanName, cacheKey);
			}
		}
		return bean;
	}

	 //$####################################wrapIfNecessary方法#######################################################
	protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
		if (beanName != null && this.targetSourcedBeans.contains(beanName)) {
			return bean;
		}
		if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
			return bean;
		}
		if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
			this.advisedBeans.put(cacheKey, Boolean.FALSE);
			return bean;
		}
		//获得当前所有的增强器 找到候选的增强器 并且获得能被使用的增强器。返回拍好序的增强器链
		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
		if (specificInterceptors != DO_NOT_PROXY) {
		    //保存当前bean在advisedBeans中；
			this.advisedBeans.put(cacheKey, Boolean.TRUE);
			//如果当前bean需要增强，创建当前bean的代理对象,spring自动决定创建哪种
			//JdkDynamicAopProxy(config);jdk动态代理；
 * 			//ObjenesisCglibAopProxy(config);cglib的动态代理；
			Object proxy = createProxy(
					bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean))；
			this.proxyTypes.put(cacheKey, proxy.getClass());
			//返回代理对象
			return proxy;
		}
		this.advisedBeans.put(cacheKey, Boolean.FALSE);
		return bean;
	}



``````


-------

### 小结:一下这个包装方法wrapIfNecessary

```````````
1）、获取当前bean的所有增强器（通知方法）  Object[]  specificInterceptors
   1、找到候选的所有的增强器（找哪些通知方法是需要切入当前bean方法的）
   2、获取到能在bean使用的增强器。
   3、给增强器排序
2）、保存当前bean在advisedBeans中；
3）、如果当前bean需要增强，创建当前bean的代理对象；
1）、获取所有增强器（通知方法）
2）、保存到proxyFactory
3）、创建代理对象：Spring自动决定
JdkDynamicAopProxy(config);jdk动态代理；
ObjenesisCglibAopProxy(config);cglib的动态代理；
4）、给容器中返回当前组件使用cglib增强了的代理对象；
```````````

------------
版权声明：本文为CSDN博主「莫失莫忘hh」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/weixin_43732955/article/details/99121404