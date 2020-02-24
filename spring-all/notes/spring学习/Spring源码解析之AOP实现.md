## AOP实现自动注解

### @EnableAspectJAutoProxy

#### @Import(AspectJAutoProxyRegistrar.class) implement ImportBeanDefinitionRegistrar

#### registerBeanDefinitions();//目的是创建类: AnnotationAwareAspectJAutoProxyCreator 其实也就是在refresh()中创建 [refresh中 registerBeanPostProcessors中处理 ](./Spring源码解析之refresh()方法.md)

   - AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);
    
      - registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry, null);
        
        - return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source);
        
        **registry.containsBeanDefinition(internalAutoProxyCreator) == true** 
        
        - BeanDefinition apcDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
        
          - if (!cls.getName().equals(apcDefinition.getBeanClassName()))
            
            - int currentPriority = findPriorityForClass(apcDefinition.getBeanClassName());
            
            - int requiredPriority = findPriorityForClass(cls);
            
            **if (currentPriority < requiredPriority)//如果有权限大于当前AnnotationAwareAspectJAutoProxyCreator的类则使用权限大的类**
            
              - apcDefinition.setBeanClassName(cls.getName());

        **registry.containsBeanDefinition(internalAutoProxyCreator) == false**
        
        - RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
        
        - beanDefinition.setSource(source);
        
        - beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE); //值最小但是排序最靠前
        
        - beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        
        - registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);//org.springframework.aop.config.internalAutoProxyCreator 
    - AnnotationAttributes enableAspectJAutoProxy =
      				AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
      				
     **enableAspectJAutoProxy!=null** 
   				
     - AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
    
      - definition.getPropertyValues().add("proxyTargetClass", Boolean.TRUE);
      
     - AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
    
      - definition.getPropertyValues().add("exposeProxy", Boolean.TRUE);

### AnnotationAwareAspectJAutoProxyCreator

### 子类: AbstractAutoProxyCreator extends ProxyProcessorSupport implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware

- public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName)

  - Object cacheKey = getCacheKey(beanClass, beanName);
  
  - if (beanName == null || !this.targetSourcedBeans.contains(beanName))
  
  - if (this.advisedBeans.containsKey(cacheKey));//当前bean是否已经存在被增强的bean当中。(advicebean中保存了所以已经被增强的bean)
  
  #### if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName));
    
    //isInfrastructureClass(beanClass)判断是否是切面的基础类如：Advice Pointcut Advisor AopInfrastructureBean 否是切面（@Aspect）
    //shouldSkip(beanClass, beanName)是否需要跳过

  - TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
  
  #### if (targetSource != null) 
  
  - this.targetSourcedBeans.add(beanName);
  
  - Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
  
  - Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
  
  - this.proxyTypes.put(cacheKey, proxy.getClass());
  
- public boolean postProcessAfterInstantiation(Object bean, String beanName);//当postProcessBeforeInstantiation返回的不为null 的时候才会执行下面的方法

- public Object postProcessBeforeInitialization(Object bean, String beanName);

``````

因为尝试返回一个代理对象失败，所以将执行doCreateBean方法。创建一个bean。
然后在创建完成初始化之前会调用applyBeanPostProcessorsBeforeInitialization方法，这个方法的内部就是，遍历所有后置处理器调用他们的postProcessBeforeInitialization方法。当然也会调用AnnotationAwareAspectJAutoProxyCreator的postProcessBeforeInitialization方法。他就会执行在每次创建bean的时候执行下面操作。

``````

- public Object postProcessAfterInitialization(@Nullable Object bean, String beanName);


````
在调用完invokeInitMethods(beanName, wrappedBean, mbd); 初始化方法之后，也会遍历所有的后置处理器，执行他们的后置方法。所以每个bean创建的时候都会调用AnnotationAwareAspectJAutoProxyCreator的postProcessAfterInitialization方法。在这里也就是尝试创建一个代理对象。

````

  - Object cacheKey = getCacheKey(bean.getClass(), beanName);
  
  #### if (this.earlyProxyReferences.remove(cacheKey) != bean)
  
  - return wrapIfNecessary(bean, beanName, cacheKey);
  
    - if (beanName != null && this.targetSourcedBeans.contains(beanName)) return bean;
    
    - if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey)))  return bean;
    
    - if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) 
      
      - this.advisedBeans.put(cacheKey, Boolean.FALSE);  return bean;
    
    - Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);//获得当前所有的增强器 找到候选的增强器 并且获得能被使用的增强器。返回拍好序的增强器链
    
    - if (specificInterceptors != DO_NOT_PROXY);
    
    - this.advisedBeans.put(cacheKey, Boolean.TRUE);
    
    - Object proxy = createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
    
    - this.proxyTypes.put(cacheKey, proxy.getClass());
    
#### protected Object createProxy(Class<?> beanClass, @Nullable String beanName,@Nullable Object[] specificInterceptors, TargetSource targetSource)

- if (this.beanFactory instanceof ConfigurableListableBeanFactory)//如果是ConfigurableListableBeanFactory接口（咱们DefaultListableBeanFactory就是该接口的实现类）则，暴露目标类

  - AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName, beanClass);//给beanFactory->beanDefinition定义一个属性：k=AutoProxyUtils.originalTargetClass,v=需要被代理的bean class

-  ProxyFactory proxyFactory = new ProxyFactory();

-  proxyFactory.copyFrom(this);

- if (!proxyFactory.isProxyTargetClass())

  - if (shouldProxyTargetClass(beanClass, beanName))
    
    - proxyFactory.setProxyTargetClass(true);//代理工厂设置代理目标类
  
  - else 
  
    -evaluateProxyInterfaces(beanClass, proxyFactory);//否则设置代理接口（JDK）
    
- Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);//把拦截器包装成增强（通知）

- proxyFactory.addAdvisors(advisors);


