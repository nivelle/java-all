## Spring 的 initializeBean()


```
    /*
	 * Initialize the given bean instance, applying factory callbacks
	 * as well as init methods and bean post processors.
	 * <p>Called from {@link #createBean} for traditionally defined beans,
	 * and from {@link #initializeBean} for existing bean instances.
	 * @param beanName the bean name in the factory (for debugging purposes)
	 * @param bean the new bean instance we may need to initialize
	 * @param mbd the bean definition that the bean was created with
	 * (can also be {@code null}, if given an existing bean instance)
	 * @return the initialized bean instance (potentially wrapped)
	 * @see BeanNameAware
	 * @see BeanClassLoaderAware
	 * @see BeanFactoryAware
	 * @see #applyBeanPostProcessorsBeforeInitialization
	 * @see #invokeInitMethods
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */

```

- protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd)

  - invokeAwareMethods(beanName, bean);
  
    -  if (bean instanceof BeanNameAware) => ((BeanNameAware) bean).setBeanName(beanName);
    
    -  if (bean instanceof BeanClassLoaderAware) => ((BeanClassLoaderAware) bean).setBeanClassLoader(bcl);
    
    -  if (bean instanceof BeanFactoryAware) => ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
    
  - Object wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
  
    - for (BeanPostProcessor processor : getBeanPostProcessors()) => processor.postProcessBeforeInitialization(result, beanName);
    
  - invokeInitMethods(beanName, wrappedBean, mbd);
    
    -  ((InitializingBean) bean).afterPropertiesSet();//调用afterPropertiesSet方法
    
    -  invokeCustomInitMethod(beanName, bean, mbd);//调用自定义初始化方法
    
       - String initMethodName = mbd.getInitMethodName();//拿到初始化方法的方法名
       
       - final Method initMethod = (mbd.isNonPublicAccessAllowed() ?BeanUtils.findMethod(bean.getClass(), initMethodName) : ClassUtils.getMethodIfAvailable(bean.getClass(), initMethodName));
       
       #### 如果不存在initMethodName对应的方法，并且是强制执行初始化方法(默认为强制), 则抛出异常;如果设置了非强制，找不到则直接返回
       
       - initMethod.invoke(bean);//调用初始化方法
             
  - wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
  
    - for (BeanPostProcessor processor : getBeanPostProcessors()) => processor.postProcessAfterInitialization(result, beanName)
        