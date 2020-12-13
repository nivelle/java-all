### 创建bean实例

- 创建 bean 的真正逻辑位于 createBean 方法中，该方法的具体实现位于 AbstractAutowireCapableBeanFactory 中

##### 创建单例对象

````
public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
    Assert.notNull(beanName, "'beanName' must not be null");
    //singletonObjects用于缓存beanName与已创建的单例对象的映射关系
    synchronized (this.singletonObjects) {
        Object singletonObject = this.singletonObjects.get(beanName);
        //对应的bean没有加载过
        if (singletonObject == null) { 
            //实例正在创建销毁
            if (this.singletonsCurrentlyInDestruction) {
                // 对应的bean正在其它地方的销毁方法中
                throw new BeanCreationNotAllowedException(beanName,
                        "Singleton bean creation not allowed while singletons of this factory are in destruction (Do not request a bean from a BeanFactory in a destroy method implementation!)");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
            }
 
            // 前置处理，对于需要依赖检测的bean，设置状态为“正在创建中”
            //this.inCreationCheckExclusions.contains(beanName);//忽略依赖检测的
            //this.singletonsCurrentlyInCreation.add(beanName) 
            this.beforeSingletonCreation(beanName);
 
            boolean newSingleton = false;
            boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
            if (recordSuppressedExceptions) {
                this.suppressedExceptions = new LinkedHashSet<Exception>();
            }
            try {
                // 实例化bean
                singletonObject = singletonFactory.getObject();
                newSingleton = true;
            } catch (IllegalStateException ex) {
                // 异常，再次尝试从缓存中获取
                singletonObject = this.singletonObjects.get(beanName);
                if (singletonObject == null) {
                    throw ex;
                }
            } catch (BeanCreationException ex) {
                if (recordSuppressedExceptions) {
                    for (Exception suppressedException : this.suppressedExceptions) {
                        ex.addRelatedCause(suppressedException);
                    }
                }
                throw ex;
            } finally {
                if (recordSuppressedExceptions) {
                    this.suppressedExceptions = null;
                }
                // 后置处理，对于需要依赖检测的bean，移除“正在创建中”的状态
                this.afterSingletonCreation(beanName);
            }
            if (newSingleton) { // 新实例
                // 加入缓存
                this.addSingleton(beanName, singletonObject);
            }
        }
        // 返回实例
        return (singletonObject != NULL_OBJECT ? singletonObject : null);
    }
}
````
#### 第一步：尝试获取实例,给后置处理器一次机会创建

`````
protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)throws BeanCreationException {

		if (logger.isTraceEnabled()) {
			logger.trace("Creating instance of bean '" + beanName + "'");
		}
		RootBeanDefinition mbdToUse = mbd;

		// Make sure bean class is actually resolved at this point, and
		// clone the bean definition in case of a dynamically resolved Class
		// which cannot be stored in the shared merged bean definition.
        //1. 根据设置的class属性或className来解析得到Class引用，会包括解析别名等操作
		Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
		if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
            //如果resolvedClass存在,并且mdb的beanClass类型不是Class，并且mdb的beanClass不为空（则代表beanClass存的是Class的name）,
            //则使用mdb深拷贝一个新的RootBeanDefinition副本，并且将解析的Class赋值给拷贝的RootBeanDefinition副本的beanClass属性,该拷贝副本取代mdb用于后续的操作
			mbdToUse = new RootBeanDefinition(mbd);
			mbdToUse.setBeanClass(resolvedClass);
		}

		// Prepare method overrides.
		try {
            //对Override 属性进行标记和验证，本质上是处理lookup-method和replaced-method
            //标记方法是否有重载
			mbdToUse.prepareMethodOverrides();
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),beanName, "Validation of method overrides failed", ex);
		}

		try {
			// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
            //处理 InstantiationAwareBeanPostProcessor
            //[AnnotationAwareAspectJAutoProxyCreator 后置处理器的使用,返回AOP代理类](./Spring源码解析之aop实现.md) 
			Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
			if (bean != null) {
                //如果处理结果不为null，则直接返回，而不执行后续的createBean;直接返回代理
				return bean;
			}
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,"BeanPostProcessor before instantiation of bean failed", ex);
		}
		try {
            //如果没有后置处理器，doCreateBean创建实例bean
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
           		throw new BeanCreationException(bvvgmbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
		}
	}

`````

##### 1.1 处理Override属性

Spring 中并不存在 override-method 的标签，这里的 override 指的是 <lookup-method/> 和 <replaced-method/> 两个标签,
之前解析这两个标签时是将这两个标签配置以 MethodOverride 对象的形式记录在 beanDefinition 实例的 methodOverrides 属性中,
而这里的处理主要是逐一检查所覆盖的方法是否存在，如果不存在则覆盖无效，如果存在唯一的方法，则覆盖是明确的，标记后期无需依据参数类型以及个数进行推测

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
##### 1.1.1 方法复写默认为true,如果仅仅一个方法，则设置 MethodOverride为false

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

##### 1.2 处理后置处理器逻辑

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
        //将应用过后置处理器的ben赋值给 RootBeanDefinition 的 beforeInstantiationResolved
        mbd.beforeInstantiationResolved = (bean != null);
    }
    return bean;
}

````

#### 第二步: 没有后置处理器创建代理过程，正常的创建实例过程

1. 如果是单例，尝试从缓存中获取 bean 的包装器 BeanWrapper

2. 如果不存在对应的 Wrapper，则说明 bean 未被实例化，创建 bean 实例

3. 应用 MergedBeanDefinitionPostProcessor

4. 检查是否需要提前曝光，避免循环依赖

5. 初始化 bean 实例

6. 再次基于依存关系验证是否存在循环依赖

7. 注册 DisposableBean

````
protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) throws BeanCreationException {
 
    BeanWrapper instanceWrapper = null;
    // 1. 如果是单例,尝试获取对应的BeanWrapper,同时从缓存中移除
    if (mbd.isSingleton()) {
        instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
    }
    // 2. 重点：实例化=》bean未实例化,创建 bean 实例
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
                // 处理 merged bean Autowired通过此方法实现诸如类型的解析
                this.applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
            } catch (Throwable ex) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Post-processing of merged bean definition failed", ex);
            }
            mbd.postProcessed = true;
        }
    }
 
    // 4. 检查是否需要提前曝光，避免循环依赖,条件: 单例 && 允许循环依赖 && 当前bean正在创建中
    //方法的逻辑是先判断是否允许提前曝光,如果当前为单例 bean,且程序制定允许循环引用,同时当前 bean 正处于创建中,则会将创建 bean 的 ObjectFactory 对象加入到用于保存 beanName 和创建 bean 的工厂之间的关系的集合中
    boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences && this.isSingletonCurrentlyInCreation(beanName));
    if (earlySingletonExposure) {
        if (logger.isDebugEnabled()) {
            logger.debug("Eagerly caching bean '" + beanName + "' to allow for resolving potential circular references");
        }
        // 为避免循环依赖,在完成bean实例化之前,将对应的ObjectFactory加入bean的创建工厂缓存
        this.addSingletonFactory(beanName, new ObjectFactory<Object>() {
            @Override
            public Object getObject() throws BeansException {
                // 对bean再一次依赖引用,应用SmartInstantiationAwareBeanPostProcessor
                return getEarlyBeanReference(beanName, mbd, bean);
            }
        });
    }
 
    // 5. 重点=》初始化:初始化bean实例
    Object exposedObject = bean;
    try {
        // 对bean进行填充，将各个属性值注入，如果存在依赖的bean则进行递归初始化
        this.populateBean(beanName, mbd, instanceWrapper);
        if (exposedObject != null) {
            // 初始化bean,调用初始化方法，比如init-method
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
        //获取指定实例的提前曝光的引用
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
                    throw new BeanCurrentlyInCreationException(beanName,"Bean with name '" + beanName + "' has been injected into other beans [" +
                    StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +"] in its raw version as part of a circular reference, but has eventually been " +
                    "wrapped. This means that said other beans do not use the final version of the " +"bean. This is often the result of over-eager type matching - consider using " +
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

#### 2.1 :createBeanInstance 创建实例

1. 如果存在工厂方法，则使用工厂方法初始化

2. 否则，如果存在多个构造函数，则根据参数确定构造函数，并利用构造函数初始化

3. 否则，使用默认构造函数初始化

````
protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {
    //解析class引用
    Class<?> beanClass = this.resolveBeanClass(mbd, beanName);
    if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
        throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
    }
 
    //1. 如果工厂方法不为空，则使用工厂方法进行实例化，否则使用构造函数
    if (mbd.getFactoryMethodName() != null) {
        return this.instantiateUsingFactoryMethod(beanName, mbd, args);
    }
 
    //2. 利用构造函数进行实例化,解析并确定目标构造函数
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
    // 如果已经解析过,则使用已经确定的构造方法
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
    if (ctors != null || mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR || // 构造函数注入
            mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) { // 存在构造函数配置
        // 构造函数自动注入
        return this.autowireConstructor(beanName, mbd, ctors, args);
    }
 
    //3. 使用默认的构造函数
    return this.instantiateBean(beanName, mbd);
}

````
##### 2.1.1 instantiateUsingFactoryMethod 工厂方法执行实例化过程

````
public BeanWrapper instantiateUsingFactoryMethod(final String beanName, final RootBeanDefinition mbd, final Object[] explicitArgs) {
    // 创建并初始化 BeanWrapper
    BeanWrapperImpl bw = new BeanWrapperImpl();
    this.beanFactory.initBeanWrapper(bw);
 
    Object factoryBean; // 工厂
    Class<?> factoryClass; // 工厂所指代的类
    boolean isStatic;  // 是不是静态工厂
    String factoryBeanName = mbd.getFactoryBeanName(); // 获取factory-bean
    if (factoryBeanName != null) {
        /*
         * 存在factory-bean，说明是非静态工厂
         *
         * <bean id="my-bean-simple-factory" class="org.zhenchao.factory.MyBeanSimpleFactory"/>
         * <bean id="my-bean-1" factory-bean="my-bean-simple-factory" factory-method="create"/>
         */
        if (factoryBeanName.equals(beanName)) {
            throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName, "factory-bean reference points back to the same bean definition");
        }
        // 获取工厂bean实例
        factoryBean = this.beanFactory.getBean(factoryBeanName);
        if (factoryBean == null) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "factory-bean '" + factoryBeanName + "' (or a BeanPostProcessor involved) returned null");
        }
        // 已经存在的单例不需要由工厂主动创建
        if (mbd.isSingleton() && this.beanFactory.containsSingleton(beanName)) {
            throw new IllegalStateException("About-to-be-created singleton instance implicitly appeared through the creation of the factory bean that its bean definition points to");
        }
        factoryClass = factoryBean.getClass();
        isStatic = false;
    } else {
        /*
         * 不存在factory-bean，说明是静态工厂
         *
         * <bean id="my-bean-2" class="org.zhenchao.factory.MyBeanStaticFactory" factory-method="create"/>
         */
        if (!mbd.hasBeanClass()) {
            throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName, "bean definition declares neither a bean class nor a factory-bean reference");
        }
        factoryBean = null;
        factoryClass = mbd.getBeanClass();
        isStatic = true;
    }
 
    Method factoryMethodToUse = null;
    ArgumentsHolder argsHolderToUse = null;
    Object[] argsToUse = null;
    if (explicitArgs != null) {
        // getBean时传递的构造参数
        argsToUse = explicitArgs;
    } else {
        // 如果没有则需要进行解析
        Object[] argsToResolve = null;
        // 尝试从缓存中获取
        synchronized (mbd.constructorArgumentLock) {
            factoryMethodToUse = (Method) mbd.resolvedConstructorOrFactoryMethod;
            if (factoryMethodToUse != null && mbd.constructorArgumentsResolved) {
                // 存在已经解析过的工厂方法
                argsToUse = mbd.resolvedConstructorArguments;
                if (argsToUse == null) {
                    // 获取待解析的构造参数
                    argsToResolve = mbd.preparedConstructorArguments;
                }
            }
        }
        // 缓存命中
        if (argsToResolve != null) {
            // 解析参数值，必要的话会进行类型转换
            argsToUse = this.resolvePreparedArguments(beanName, mbd, bw, factoryMethodToUse, argsToResolve);
        }
    }
 
    // 缓存未命中，则基于参数来解析决策确定的工厂方法
    if (factoryMethodToUse == null || argsToUse == null) {
        // 获取候选的工厂方法
        factoryClass = ClassUtils.getUserClass(factoryClass);
        Method[] rawCandidates = this.getCandidateMethods(factoryClass, mbd);
        List<Method> candidateSet = new ArrayList<Method>();
        for (Method candidate : rawCandidates) {
            if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate)) {
                candidateSet.add(candidate);
            }
        }
        Method[] candidates = candidateSet.toArray(new Method[candidateSet.size()]);
        // 对候选方法进行排序，public在前，参数多的在前
        AutowireUtils.sortFactoryMethods(candidates);
 
        ConstructorArgumentValues resolvedValues = null;
        boolean autowiring = (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        int minTypeDiffWeight = Integer.MAX_VALUE;
        Set<Method> ambiguousFactoryMethods = null;
 
        int minNrOfArgs;
        if (explicitArgs != null) {
            // getBean时明确指定了参数
            minNrOfArgs = explicitArgs.length;
        } else {
            // 没有指定，则从beanDefinition实例中解析
            ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
            resolvedValues = new ConstructorArgumentValues();
            // 获取解析到的参数个数
            minNrOfArgs = this.resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
        }
 
        LinkedList<UnsatisfiedDependencyException> causes = null;
        // 遍历候选方法
        for (Method candidate : candidates) {
            Class<?>[] paramTypes = candidate.getParameterTypes();
            if (paramTypes.length >= minNrOfArgs) {
                ArgumentsHolder argsHolder;
                if (resolvedValues != null) {
                    // Resolved constructor arguments: type conversion and/or autowiring necessary.
                    try {
                        String[] paramNames = null;
                        // 获取参数名称探测去
                        ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
                        if (pnd != null) {
                            // 获取候选方法的参数名称列表
                            paramNames = pnd.getParameterNames(candidate);
                        }
                        // 依据参数名称和类型创建参数持有对象
                        argsHolder = this.createArgumentArray(
                                beanName, mbd, resolvedValues, bw, paramTypes, paramNames, candidate, autowiring);
                    } catch (UnsatisfiedDependencyException ex) {
                        if (this.beanFactory.logger.isTraceEnabled()) {
                            this.beanFactory.logger.trace("Ignoring factory method [" + candidate + "] of bean '" + beanName + "': " + ex);
                        }
                        // 异常，尝试下一个候选工厂方法
                        if (causes == null) {
                            causes = new LinkedList<UnsatisfiedDependencyException>();
                        }
                        causes.add(ex);
                        continue;
                    }
                } else {
                    if (paramTypes.length != explicitArgs.length) {
                        // 参数个数不等于期望的参数个数
                        continue;
                    }
                    argsHolder = new ArgumentsHolder(explicitArgs);
                }
 
                // 检测是否有不确定的候选方法存在（比如不同方法的参数存在继承关系）
                int typeDiffWeight = (mbd.isLenientConstructorResolution() ?
                        argsHolder.getTypeDifferenceWeight(paramTypes) : argsHolder.getAssignabilityWeight(paramTypes));
                // 选择最近似的候选方法
                if (typeDiffWeight < minTypeDiffWeight) {
                    factoryMethodToUse = candidate;
                    argsHolderToUse = argsHolder;
                    argsToUse = argsHolder.arguments;
                    minTypeDiffWeight = typeDiffWeight;
                    ambiguousFactoryMethods = null;
                } else if (factoryMethodToUse != null && typeDiffWeight == minTypeDiffWeight &&
                        !mbd.isLenientConstructorResolution() &&
                        paramTypes.length == factoryMethodToUse.getParameterTypes().length &&
                        !Arrays.equals(paramTypes, factoryMethodToUse.getParameterTypes())) {
                    if (ambiguousFactoryMethods == null) {
                        ambiguousFactoryMethods = new LinkedHashSet<Method>();
                        ambiguousFactoryMethods.add(factoryMethodToUse);
                    }
                    ambiguousFactoryMethods.add(candidate);
                }
            }
        }
 
        if (factoryMethodToUse == null) {
            // 无法确定具体的工厂方法
            if (causes != null) {
                UnsatisfiedDependencyException ex = causes.removeLast();
                for (Exception cause : causes) {
                    this.beanFactory.onSuppressedException(cause);
                }
                throw ex;
            }
            List<String> argTypes = new ArrayList<String>(minNrOfArgs);
            if (explicitArgs != null) {
                for (Object arg : explicitArgs) {
                    argTypes.add(arg != null ? arg.getClass().getSimpleName() : "null");
                }
            } else {
                Set<ValueHolder> valueHolders = new LinkedHashSet<ValueHolder>(resolvedValues.getArgumentCount());
                valueHolders.addAll(resolvedValues.getIndexedArgumentValues().values());
                valueHolders.addAll(resolvedValues.getGenericArgumentValues());
                for (ValueHolder value : valueHolders) {
                    String argType = (value.getType() != null ? ClassUtils.getShortName(value.getType()) :
                            (value.getValue() != null ? value.getValue().getClass().getSimpleName() : "null"));
                    argTypes.add(argType);
                }
            }
            String argDesc = StringUtils.collectionToCommaDelimitedString(argTypes);
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "No matching factory method found: " +  (mbd.getFactoryBeanName() != null ?  "factory bean '" + mbd.getFactoryBeanName() + "'; " : "") +
                            "factory method '" + mbd.getFactoryMethodName() + "(" + argDesc + ")'. " + "Check that a method with the specified name " +
                            (minNrOfArgs > 0 ? "and arguments " : "") + "exists and that it is " +  (isStatic ? "static" : "non-static") + ".");
        } else if (void.class == factoryMethodToUse.getReturnType()) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Invalid factory method '" + mbd.getFactoryMethodName() + "': needs to have a non-void return type!");
        } else if (ambiguousFactoryMethods != null) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Ambiguous factory method matches found in bean '" + beanName + "' " + "(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities): " +  ambiguousFactoryMethods);
        }
 
        if (explicitArgs == null && argsHolderToUse != null) {
            // 缓存
            argsHolderToUse.storeCache(mbd, factoryMethodToUse);
        }
    }
 
    // 利用工厂方法创建bean实例
    try {
        Object beanInstance;
        if (System.getSecurityManager() != null) {
            final Object fb = factoryBean;
            final Method factoryMethod = factoryMethodToUse;
            final Object[] args = argsToUse;
            beanInstance = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    return beanFactory.getInstantiationStrategy().instantiate(
                            mbd, beanName, beanFactory, fb, factoryMethod, args);
                }
            }, beanFactory.getAccessControlContext());
        } else {
            // 基于java反射调用工厂bean的指定方法依据给定的参数进行实例化
            beanInstance = this.beanFactory.getInstantiationStrategy().instantiate(
                    mbd, beanName, this.beanFactory, factoryBean, factoryMethodToUse, argsToUse);
        }
 
        if (beanInstance == null) {
            return null;
        }
        bw.setBeanInstance(beanInstance);
        return bw;
    } catch (Throwable ex) {
        throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Bean instantiation via factory method failed", ex);
    }
}
````

##### 2.2.1 构造方法执行实例化过程

```
public BeanWrapper autowireConstructor(
        final String beanName, final RootBeanDefinition mbd, Constructor<?>[] chosenCtors, final Object[] explicitArgs) {
    // 创建并初始化BeanWrapper
    BeanWrapperImpl bw = new BeanWrapperImpl();
    this.beanFactory.initBeanWrapper(bw);
 
    Constructor<?> constructorToUse = null;
    ArgumentsHolder argsHolderToUse = null;
    Object[] argsToUse = null;
    if (explicitArgs != null) {
        // 调用getBean时明确指定了构造参数explicitArgs
        argsToUse = explicitArgs;
    } else {
        // 尝试从缓存中获取
        Object[] argsToResolve = null;
        synchronized (mbd.constructorArgumentLock) {
            constructorToUse = (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;
            if (constructorToUse != null && mbd.constructorArgumentsResolved) {
                // 找到了缓存的构造方法
                argsToUse = mbd.resolvedConstructorArguments;
                if (argsToUse == null) {
                    // 配置的构造函数参数
                    argsToResolve = mbd.preparedConstructorArguments;
                }
            }
        }
        // 如果缓存中存在
        if (argsToResolve != null) {
            // 解析参数类型，将字符串值转换为真实的值，缓存中的值可能是原始值也可能是解析后的值
            argsToUse = this.resolvePreparedArguments(beanName, mbd, bw, constructorToUse, argsToResolve);
        }
    }
 
    // 没有缓存则从配置文件中解析
    if (constructorToUse == null) {
        // 需要解析确定具体的构造方法
        boolean autowiring = (chosenCtors != null ||
                mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        ConstructorArgumentValues resolvedValues = null;
 
        int minNrOfArgs; // 记录解析到的参数个数
        if (explicitArgs != null) {
            minNrOfArgs = explicitArgs.length;
        } else {
            // 提取配置的构造函数参数值
            ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
            resolvedValues = new ConstructorArgumentValues(); // 用于承载解析后的构造函数参数的值
            // 解析构造参数
            minNrOfArgs = this.resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
        }
 
        // 获取候选的构造方法集合
        Constructor<?>[] candidates = chosenCtors;
        if (candidates == null) {
            Class<?> beanClass = mbd.getBeanClass();
            try {
                candidates = (mbd.isNonPublicAccessAllowed() ? beanClass.getDeclaredConstructors() : beanClass.getConstructors());
            } catch (Throwable ex) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Resolution of declared constructors on bean Class [" + beanClass.getName() + "] from ClassLoader [" + beanClass.getClassLoader() + "] failed", ex);
            }
        }
 
        // 排序构造函数，public在前，参数多的在前
        AutowireUtils.sortConstructors(candidates);
 
        int minTypeDiffWeight = Integer.MAX_VALUE;
        Set<Constructor<?>> ambiguousConstructors = null;
        LinkedList<UnsatisfiedDependencyException> causes = null;
 
        for (Constructor<?> candidate : candidates) {
            Class<?>[] paramTypes = candidate.getParameterTypes();
            if (constructorToUse != null && argsToUse.length > paramTypes.length) {
                // 已经找到目标构造函数，或者已有的构造函数的参数个数已经小于期望的个数
                break;
            }
            if (paramTypes.length < minNrOfArgs) {
                // 参数个数不相等
                continue;
            }
 
            ArgumentsHolder argsHolder;
            if (resolvedValues != null) {
                // 有参数则根据对应的值构造对应参数类型的参数
                try {
                    // 从注解上获得参数名称
                    String[] paramNames = ConstructorPropertiesChecker.evaluate(candidate, paramTypes.length);
                    if (paramNames == null) {
                        // 获取参数名称探测器
                        ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
                        if (pnd != null) {
                            // 获取构造方法的参数名称列表
                            paramNames = pnd.getParameterNames(candidate);
                        }
                    }
                    // 根据数值和类型创建参数持有者
                    argsHolder = this.createArgumentArray(
                            beanName, mbd, resolvedValues, bw, paramTypes, paramNames, this.getUserDeclaredConstructor(candidate), autowiring);
                } catch (UnsatisfiedDependencyException ex) {
                    if (this.beanFactory.logger.isTraceEnabled()) {
                        this.beanFactory.logger.trace("Ignoring constructor [" + candidate + "] of bean '" + beanName + "': " + ex);
                    }
                    // 尝试下一个构造函数
                    if (causes == null) {
                        causes = new LinkedList<UnsatisfiedDependencyException>();
                    }
                    causes.add(ex);
                    continue;
                }
            } else {
                // 参数个数不匹配
                if (paramTypes.length != explicitArgs.length) {
                    continue;
                }
                // 构造函数没有参数的情况
                argsHolder = new ArgumentsHolder(explicitArgs);
            }
 
            // 探测是否有不确定性的构造函数存在，不如不同构造函数的参数为继承关系
            int typeDiffWeight = (mbd.isLenientConstructorResolution() ?
                    argsHolder.getTypeDifferenceWeight(paramTypes) : argsHolder.getAssignabilityWeight(paramTypes));
            // 如果当前的不确定性构造函数最接近期望值，则选择作为构造函数
            if (typeDiffWeight < minTypeDiffWeight) {
                constructorToUse = candidate;
                argsHolderToUse = argsHolder;
                argsToUse = argsHolder.arguments;
                minTypeDiffWeight = typeDiffWeight;
                ambiguousConstructors = null;
            } else if (constructorToUse != null && typeDiffWeight == minTypeDiffWeight) {
                if (ambiguousConstructors == null) {
                    ambiguousConstructors = new LinkedHashSet<Constructor<?>>();
                    ambiguousConstructors.add(constructorToUse);
                }
                ambiguousConstructors.add(candidate);
            }
        }
 
        if (constructorToUse == null) {
            if (causes != null) {
                UnsatisfiedDependencyException ex = causes.removeLast();
                for (Exception cause : causes) {
                    this.beanFactory.onSuppressedException(cause);
                }
                throw ex;
            }
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Could not resolve matching constructor (hint: specify index/type/name arguments for simple parameters to avoid type ambiguities)");
        } else if (ambiguousConstructors != null && !mbd.isLenientConstructorResolution()) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Ambiguous constructor matches found in bean '" + beanName + "' (hint: specify index/type/name arguments for simple parameters to avoid type ambiguities): " + ambiguousConstructors);
        }
 
        if (explicitArgs == null) {
            // 将解析的构造函数加入缓存
            argsHolderToUse.storeCache(mbd, constructorToUse);
        }
    }
 
    // 依据构造方法进行实例化
    try {
        Object beanInstance;
        if (System.getSecurityManager() != null) {
            final Constructor<?> ctorToUse = constructorToUse;
            final Object[] argumentsToUse = argsToUse;
            beanInstance = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    return beanFactory.getInstantiationStrategy().instantiate(
                            mbd, beanName, beanFactory, ctorToUse, argumentsToUse);
                }
            }, beanFactory.getAccessControlContext());
        } else {
            // 基于反射创建对象
            beanInstance = this.beanFactory.getInstantiationStrategy().instantiate(mbd, beanName, this.beanFactory, constructorToUse, argsToUse);
        }
 
        // 将构造的实例加入BeanWrapper
        bw.setBeanInstance(beanInstance);
        return bw;
    } catch (Throwable ex) {
        throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Bean instantiation via constructor failed", ex);
    }
}

```

#### 核心1:实例化bean实例(属性注入) [属性注入](./Spring源码解析之populateBean().md)   

#### 核心2:实例化bean实例(执行初始化方法) [初始化](./Spring源码解析之initializeBean()方法.md)     