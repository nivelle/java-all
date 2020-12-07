### 创建bean实例

#### 第一步：尝试获取实例，给后置处理器一次机会创建
`````
protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
			throws BeanCreationException {

		if (logger.isTraceEnabled()) {
			logger.trace("Creating instance of bean '" + beanName + "'");
		}
		RootBeanDefinition mbdToUse = mbd;

		// Make sure bean class is actually resolved at this point, and
		// clone the bean definition in case of a dynamically resolved Class
		// which cannot be stored in the shared merged bean definition.
        //根据设置的class属性或className来解析得到Class引用
		Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
		if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
            //如果resolvedClass存在，并且mdb的beanClass类型不是Class，并且mdb的beanClass不为空（则代表beanClass存的是Class的name）,
            //则使用mdb深拷贝一个新的RootBeanDefinition副本，并且将解析的Class赋值给拷贝的RootBeanDefinition副本的beanClass属性，该拷贝副本取代mdb用于后续的操作
			mbdToUse = new RootBeanDefinition(mbd);
			mbdToUse.setBeanClass(resolvedClass);
		}

		// Prepare method overrides.
		try {
            //对Override 属性进行标记和验证，本质上是处理lookup-method和replaced-method
			mbdToUse.prepareMethodOverrides();
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
					beanName, "Validation of method overrides failed", ex);
		}

		try {
			// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
            //处理 InstantiationAwareBeanPostProcessor
            //[AnnotationAwareAspectJAutoProxyCreator 后置处理器的使用,返回AOP代理类](./Spring源码解析之aop实现.md) 
			Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
			if (bean != null) {
                //如果处理结果不为null，则直接返回，而不执行后续的createBean;返回代理
				return bean;
			}
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
					"BeanPostProcessor before instantiation of bean failed", ex);
		}

		try {
            //如果没有后置处理器，doCreateBean正常正规的创建实例bean
			Object beanInstance = doCreateBean(beanName, mbdToUse, args);
			if (logger.isTraceEnabled()) {
				logger.trace("Finished creating instance of bean '" + beanName + "'");
			}
			return beanInstance;
		}
		catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
			// A previously detected exception with proper bean creation context already,
			// or illegal singleton state to be communicated up to DefaultSingletonBeanRegistry.
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
		}
	}

`````

##### 处理Override属性

Spring 中并不存在 override-method 的标签，这里的 override 指的是 <lookup-method/> 和 <replaced-method/> 两个标签，之前解析这两个标签时是将这两个标签配置以 MethodOverride 对象的形式记录在 beanDefinition 实例的 methodOverrides 属性中，而这里的处理主要是逐一检查所覆盖的方法是否存在，如果不存在则覆盖无效，如果存在唯一的方法，则覆盖是明确的，标记后期无需依据参数类型以及个数进行推测
````
public void prepareMethodOverrides() throws BeanDefinitionValidationException {
    // 获取之前记录的<lookup-method/>和<replaced-method/>标签配置
    MethodOverrides methodOverrides = this.getMethodOverrides();
    if (!methodOverrides.isEmpty()) {
        Set<MethodOverride> overrides = methodOverrides.getOverrides();
        synchronized (overrides) {
            for (MethodOverride mo : overrides) {
                // 逐一处理
                this.prepareMethodOverride(mo);
            }
        }
    }
}


````

````

protected void prepareMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {
    // 获取指定类中指定方法名的个数
    int count = ClassUtils.getMethodCountForName(this.getBeanClass(), mo.getMethodName());
    if (count == 0) {
        // 无效
        throw new BeanDefinitionValidationException("Invalid method override: no method with name '" + mo.getMethodName() + "' on class [" + getBeanClassName() + "]");
    } else if (count == 1) {
        /*
         * 标记MethodOverride暂未被覆盖，避免参数类型检查的开销
         *
         * 如果一个方法存在多个重载，那么在调用及增强的时候还需要根据参数类型进行匹配来最终确认调用的函数
         * 如果方法只有一个，就在这里设置重载为false，后续可以直接定位方法
         */
        mo.setOverloaded(false);
    }
}


````

##### 处理后置处理器逻辑

````

protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
    Object bean = null;
    if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) { // 表示尚未被解析
        // mbd是程序创建的且存在后置处理器
        if (!mbd.isSynthetic() && this.hasInstantiationAwareBeanPostProcessors()) {
            // 获取最终的class引用，如果是工厂方法则获取工厂所创建的实例类型
            Class<?> targetType = this.determineTargetType(beanName, mbd);
            if (targetType != null) {
                // 调用实例化前置处理器
                bean = this.applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                if (bean != null) {
                    // 调用实例化后置处理器
                    bean = this.applyBeanPostProcessorsAfterInitialization(bean, beanName);
                }
            }
        }
        mbd.beforeInstantiationResolved = (bean != null);
    }
    return bean;
}

````

#### 第二步：没有后置处理器创建代理过程，正常的创建实例过程

````

protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) throws BeanCreationException {
 
    BeanWrapper instanceWrapper = null;
    // 1. 如果是单例，尝试获取对应的BeanWrapper
    if (mbd.isSingleton()) {
        instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
    }
    // 2. bean未实例化，创建 bean 实例
    if (instanceWrapper == null) {
        /*
         * 说明对应的bean还没有创建，用对应的策略（工厂方法、构造函数）创建bean实例，以及简单初始化
         *
         * 将beanDefinition转成BeanWrapper，大致流程如下：
         * 1. 如果存在工厂方法，则使用工厂方法初始化
         * 2. 否则，如果存在多个构造函数，则根据参数确定构造函数，并利用构造函数初始化
         * 3. 否则，使用默认构造函数初始化
         */
        instanceWrapper = this.createBeanInstance(beanName, mbd, args);
    }
    // 从BeanWrapper中获取包装的bean实例
    final Object bean = (instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null);
    // 从BeanWrapper获取包装bean的class引用
    Class<?> beanType = (instanceWrapper != null ? instanceWrapper.getWrappedClass() : null);
    mbd.resolvedTargetType = beanType;
 
    // 3. 应用 MergedBeanDefinitionPostProcessor
    synchronized (mbd.postProcessingLock) {
        if (!mbd.postProcessed) {
            try {
                // 处理 merged bean，Autowired通过此方法实现诸如类型的解析
                this.applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
            } catch (Throwable ex) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Post-processing of merged bean definition failed", ex);
            }
            mbd.postProcessed = true;
        }
    }
 
    // 4. 检查是否需要提前曝光，避免循环依赖，条件：单例 && 允许循环依赖 && 当前bean正在创建中
    boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences && this.isSingletonCurrentlyInCreation(beanName));
    if (earlySingletonExposure) {
        if (logger.isDebugEnabled()) {
            logger.debug("Eagerly caching bean '" + beanName + "' to allow for resolving potential circular references");
        }
        // 为避免循环依赖，在完成bean实例化之前，将对应的ObjectFactory加入bean的工厂
        this.addSingletonFactory(beanName, new ObjectFactory<Object>() {
            @Override
            public Object getObject() throws BeansException {
                // 对bean再一次依赖引用，应用SmartInstantiationAwareBeanPostProcessor
                return getEarlyBeanReference(beanName, mbd, bean);
            }
        });
    }
 
    // 5. 初始化bean实例
    Object exposedObject = bean;
    try {
        // 对bean进行填充，将各个属性值注入，如果存在依赖的bean则进行递归初始化
        this.populateBean(beanName, mbd, instanceWrapper);
        if (exposedObject != null) {
            // 初始化bean，调用初始化方法，比如init-method
            exposedObject = this.initializeBean(beanName, exposedObject, mbd);
        }
    } catch (Throwable ex) {
        if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
            throw (BeanCreationException) ex;
        } else {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
        }
    }
 
    // 6. 再次基于依存关系验证是否存在循环依赖
    if (earlySingletonExposure) { // 提前曝光
        Object earlySingletonReference = this.getSingleton(beanName, false);
        if (earlySingletonReference != null) { // 只有在检测到循环引用的情况下才会不为null
            if (exposedObject == bean) {  // exposedObject没有在初始化中被增强
                exposedObject = earlySingletonReference;
            } else if (!this.allowRawInjectionDespiteWrapping && this.hasDependentBean(beanName)) {
                // 获取依赖的bean的name
                String[] dependentBeans = this.getDependentBeans(beanName);
                Set<String> actualDependentBeans = new LinkedHashSet<String>(dependentBeans.length);
                for (String dependentBean : dependentBeans) {
                    // 检测依赖，记录未完成创建的bean
                    if (!this.removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                        actualDependentBeans.add(dependentBean);
                    }
                }
                /*
                 * 因为bean在创建完成之后，其依赖的bean一定是被创建了的
                 * 如果actualDependentBeans不为空，则说明bean依赖的bean没有完成创建，存在循环依赖
                 */
                if (!actualDependentBeans.isEmpty()) {
                    throw new BeanCurrentlyInCreationException(beanName,
                            "Bean with name '" + beanName + "' has been injected into other beans [" +
                                    StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
                                    "] in its raw version as part of a circular reference, but has eventually been " +
                                    "wrapped. This means that said other beans do not use the final version of the " +
                                    "bean. This is often the result of over-eager type matching - consider using " +
                                    "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
                }
            }
        }
    }
    try {
        // 7. 注册DisposableBean
        this.registerDisposableBeanIfNecessary(beanName, bean, mbd);
    } catch (BeanDefinitionValidationException ex) {
        throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
    }
 
    return exposedObject;
}

````

#### createBeanInstance

````

protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {
    Class<?> beanClass = this.resolveBeanClass(mbd, beanName); // 解析class引用
    if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
        throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
    }
 
    /*1. 如果工厂方法不为空，则使用工厂方法进行实例化*/
    if (mbd.getFactoryMethodName() != null) {
        return this.instantiateUsingFactoryMethod(beanName, mbd, args);
    }
 
    /*2. 利用构造函数进行实例化，解析并确定目标构造函数*/
    boolean resolved = false;
    boolean autowireNecessary = false;
    if (args == null) {
        synchronized (mbd.constructorArgumentLock) {
            // 一个类可能有多个构造函数，需要根据参数来确定具体的构造函数
            if (mbd.resolvedConstructorOrFactoryMethod != null) {
                resolved = true;
                autowireNecessary = mbd.constructorArgumentsResolved;
            }
        }
    }
    // 如果已经解析过，则使用已经确定的构造方法
    if (resolved) {
        if (autowireNecessary) {
            // 依据构造函数注入
            return this.autowireConstructor(beanName, mbd, null, null);
        } else {
            // 使用默认构造函数构造
            return this.instantiateBean(beanName, mbd);
        }
    }
 
    // 需要根据参数决定使用哪个构造函数
    Constructor<?>[] ctors = this.determineConstructorsFromBeanPostProcessors(beanClass, beanName);
    if (ctors != null ||
            mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR || // 构造函数注入
            mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) { // 存在构造函数配置
        // 构造函数自动注入
        return this.autowireConstructor(beanName, mbd, ctors, args);
    }
 
    /*3. 使用默认的构造函数*/
    return this.instantiateBean(beanName, mbd);
}

````

### Spring 的 createBeanInstance()

- Class<?> beanClass = resolveBeanClass(mbd, beanName);//解析bean的类型信息

##### beanClass不为空 && beanClass不是公开类（不是public修饰） && 该bean不允许访问非公共构造函数和方法，则抛异常

- if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed());

##### 如果存在工厂方法则使用工厂方法实例化bean对象

- if (mbd.getFactoryMethodName() != null)

##### boolean resolved = false;(构造函数或工厂方法是否已经解析过)

- resolved = true;//如果resolvedConstructorOrFactoryMethod缓存不为空，则将resolved标记为已解析

##### boolean autowireNecessary = false;(是否需要自动注入（即是否需要解析构造函数参数)）

- autowireNecessary = mbd.constructorArgumentsResolved;

##### 如果已经解析过，则使用resolvedConstructorOrFactoryMethod缓存里解析好的构造函数方法

- return autowireConstructor(beanName, mbd, null, null);

  - BeanWrapperImpl bw = new BeanWrapperImpl();//定义bean包装类
  
  - Constructor<?> constructorToUse = null;//最终用于实例化的构造函数
  
  - ArgumentsHolder argsHolderToUse = null;//最终用于实例化的参数Holder
  
  - Object[] argsToUse = null;//最终用于实例化的构造函数参数

  #### 如果explicitArgs不为空，则构造函数的参数直接使用explicitArgs通过getBean方法调用时，显示指定了参数，则explicitArgs就不为null

  - argsToUse = explicitArgs;
  
  #### 否则尝试从缓存中获取已经解析过的构造函数参数
  
  - Object[] argsToResolve = null;
  
  - constructorToUse = (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;//拿到缓存中已解析的构造函数或工厂方法
  
  #### 如果constructorToUse不为空 && mbd标记了构造函数参数已解析
  
  - if (constructorToUse != null && mbd.constructorArgumentsResolved)
  
  - argsToUse = mbd.resolvedConstructorArguments;//从缓存中获取已解析的构造函数参数

  - if (argsToUse == null)
  
  #### 如果resolvedConstructorArguments为空，则从缓存中获取准备用于解析的构造函数参数(constructorArgumentsResolved 为true时，resolvedConstructorArguments,preparedConstructorArguments必然有一个缓存了构造函数的参数)
  - argsToResolve = mbd.preparedConstructorArguments;
  
  - if (argsToResolve != null)
  
  #### 如果argsToResolve不为空，则对构造函数参数进行解析，如给定方法的构造函数 A(int,int)则通过此方法后就会把配置中的("1","1")转换为(1,1)
  - argsToUse = resolvePreparedArguments(beanName, mbd, bw, constructorToUse, argsToResolve);
  
  #### 如果入参chosenCtors不为空，则将chosenCtors的构造函数作为候选者
  -  if (constructorToUse == null || argsToUse == null);
  -  beanClass = mbd.getBeanClass();
  -  candidates = (mbd.isNonPublicAccessAllowed() ? beanClass().getDeclaredConstructors() : beanClass.getConstructors())

##### 否则使用默认的构造函数进行bean的实例化

- instantiateBean(beanName, mbd);

  //应用后置处理器SmartInstantiationAwareBeanPostProcessor，拿到bean的候选构造函数
- Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);

  - Constructor<?>[] ctors = ibp.determineCandidateConstructors(beanClass, beanName);//使用@Autowire注解修饰构造函数，则该构造函数在这边会被AutowiredAnnotationBeanPostProcessor找到

##### 如果ctors不为空 || mbd的注入方式为AUTOWIRE_CONSTRUCTOR || mdb定义了构造函数的参数值 || args不为空，则执行构造函数自动注入

- return autowireConstructor(beanName, mbd, ctors, args);

- instantiateBean(beanName, mbd);//没有特殊处理，则使用默认的构造函数进行bean的实例化

````
protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {
    Class<?> beanClass = this.resolveBeanClass(mbd, beanName); // 解析class引用
    if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
        throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
    }
 
    /*1. 如果工厂方法不为空，则使用工厂方法进行实例化*/
    if (mbd.getFactoryMethodName() != null) {
        return this.instantiateUsingFactoryMethod(beanName, mbd, args);
    }
 
    /*2. 利用构造函数进行实例化，解析并确定目标构造函数*/
    boolean resolved = false;
    boolean autowireNecessary = false;
    if (args == null) {
        synchronized (mbd.constructorArgumentLock) {
            // 一个类可能有多个构造函数，需要根据参数来确定具体的构造函数
            if (mbd.resolvedConstructorOrFactoryMethod != null) {
                resolved = true;
                autowireNecessary = mbd.constructorArgumentsResolved;
            }
        }
    }
    // 如果已经解析过，则使用已经确定的构造方法
    if (resolved) {
        if (autowireNecessary) {
            // 依据构造函数注入
            return this.autowireConstructor(beanName, mbd, null, null);
        } else {
            // 使用默认构造函数构造
            return this.instantiateBean(beanName, mbd);
        }
    }
 
    // 需要根据参数决定使用哪个构造函数
    Constructor<?>[] ctors = this.determineConstructorsFromBeanPostProcessors(beanClass, beanName);
    if (ctors != null ||
            mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR || // 构造函数注入
            mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) { // 存在构造函数配置
        // 构造函数自动注入
        return this.autowireConstructor(beanName, mbd, ctors, args);
    }
 
    /*3. 使用默认的构造函数*/
    return this.instantiateBean(beanName, mbd);
}

````



        