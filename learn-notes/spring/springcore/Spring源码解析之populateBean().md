#### populateBean：属性设置，子类实现：AbstractAutowireCapableBeanFactory

````
protected void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw) {
    //1. 获取bean实例的属性值集合
    PropertyValues pvs = mbd.getPropertyValues();
    //不存在实例化对象
    if (bw == null) {
        if (!pvs.isEmpty()) {
            // null对象，但是存在填充的属性，不合理
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
        } else {
            // null 对象，且没有属性可以填充，直接返回
            return;
        }
    }
 
    boolean continueWithPropertyPopulation = true;
    // 2. 给InstantiationAwareBeanPostProcessors最后一次机会在注入属性前改变bean实例
    //bean不是合成的，并且持有InstantionAwareBeanPostProcessors
    if (!mbd.isSynthetic() && this.hasInstantiationAwareBeanPostProcessors()) {
        for (BeanPostProcessor bp : this.getBeanPostProcessors()) {
            if (bp instanceof InstantiationAwareBeanPostProcessor) {
                InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                // 是否继续填充bean，执行实例化后的后置处理器
                // postProcessAfterInstantiation：如果应该在 bean上面设置属性则返回 true，否则返回 false
                //  一般情况下，应该是返回true 。
                // 返回 false 的话，将会阻止在此 Bean 实例上调用任何后续的 InstantiationAwareBeanPostProcessor 实例
                if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
                    continueWithPropertyPopulation = false;
                    break;
                }
            }
        }
    }
    // 如果处理器指明不需要再继续执行属性注入，则返回
    if (!continueWithPropertyPopulation) {
        return;
    }
 
    // autowire by name or autowire by type
    // spring 自动注入，byType 和 byName
    if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME
            || mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
        MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
        // 根据名称自动注入
        if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
            this.autowireByName(beanName, mbd, bw, newPvs);
        }
        // 根据类型自动注入
        if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
            this.autowireByType(beanName, mbd, bw, newPvs);
        }
        pvs = newPvs;
    }
    //是否已经注册了 InstantiationAwareBeanPostProcessors
    boolean hasInstAwareBpps = this.hasInstantiationAwareBeanPostProcessors();
    //是否进行 依赖检查
    boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);
    if (hasInstAwareBpps || needsDepCheck) {
        PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
        // 3. 在属性注入前应用实例化后置处理器
        if (hasInstAwareBpps) {
            for (BeanPostProcessor bp : this.getBeanPostProcessors()) {
                if (bp instanceof InstantiationAwareBeanPostProcessor) {
                    InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                    // 调用后置处理器的:postProcessPropertyValues方法
                    pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
                    if (pvs == null) {
                        // 处理器中把属性值处理没了，则继续执行属性注入已经没有意义
                        return;
                    }
                }
            }
        }
        // 依赖检查，对应depends-on属性，该属性已经弃用
        if (needsDepCheck) {
            this.checkDependencies(beanName, mbd, filteredPds, pvs);
        }
    }
 
    // 执行属性注入,上面只是把属性写入到beanDefinition中了
    this.applyPropertyValues(beanName, mbd, bw, pvs);
}

````
##### 1. getResolvedAutowireMode //检查注入注入类型

````
/**
	 * Return the resolved autowire code,
	 * (resolving AUTOWIRE_AUTODETECT to AUTOWIRE_CONSTRUCTOR or AUTOWIRE_BY_TYPE).
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_BY_TYPE
	 */
	public int getResolvedAutowireMode() {
		if (this.autowireMode == AUTOWIRE_AUTODETECT) {
			// Work out whether to apply setter autowiring or constructor autowiring.
			// If it has a no-arg constructor it's deemed to be setter autowiring,
			// otherwise we'll try constructor autowiring.
			Constructor<?>[] constructors = getBeanClass().getConstructors();
			for (Constructor<?> constructor : constructors) {
				if (constructor.getParameterCount() == 0) {
					return AUTOWIRE_BY_TYPE; //只有默认构造函数的类型自动注入是根据类型注入
				}
			}
			return AUTOWIRE_CONSTRUCTOR; //构造函数
		}
		else {
			return this.autowireMode;
		}
	}

````


##### 2.1 autowireByName //根据名称注入

````
protected void autowireByName(
			String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {
        //寻找需要注入的属性(非简单属性：非Local,Date,Enum,Void,class 等，以及数组元素是简单类型的属性)
		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
        //遍历propertyName 数组
		for (String propertyName : propertyNames) {
			if (containsBean(propertyName)) {
                //如果依赖的bean没有初始化,则递归初始化相关的bean
				Object bean = getBean(propertyName);
                //记录依赖关系到集合中
				pvs.add(propertyName, bean);
                //属性依赖注入
				registerDependentBean(propertyName, beanName);
				if (logger.isTraceEnabled()) {
					logger.trace("Added autowiring by name from bean name '" + beanName +
							"' via property '" + propertyName + "' to bean named '" + propertyName + "'");
				}
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName +
							"' by name: no matching bean found");
				}
			}
		}
	}

````
##### 2.2 autowireByType//根据类型注入

````
protected void autowireByType(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {
    //获取TypeConverter实例 使用自定义的TypeConverter,用于取代默认的PropertyEditor
    TypeConverter converter = this.getCustomTypeConverter();
    if (converter == null) {
        converter = bw;
    }
 
    Set<String> autowiredBeanNames = new LinkedHashSet<String>(4);
    // 寻找需要注入的属性
    String[] propertyNames = this.unsatisfiedNonSimpleProperties(mbd, bw);
    for (String propertyName : propertyNames) {
        try {
            //获得用来注入属性的PropertyDescriptor
            PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
            //不装配Object类型
            if (Object.class != pd.getPropertyType()) {
                // 获取指定属性的set方法
                MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
                boolean eager = !PriorityOrdered.class.isAssignableFrom(bw.getWrappedClass());
                DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);
                /*
                 * 解析指定beanName的属性所匹配的值，并把解析到的属性存储在autowiredBeanNames中，当属性存在多个封装的bean时，比如：
                 *
                 * @Autowired
                 * private List<A> list
                 *
                 * 将会找到所有匹配A类型的bean，并将其注入
                 */
                Object autowiredArgument = this.resolveDependency(desc, beanName, autowiredBeanNames, converter);
                if (autowiredArgument != null) {
                    pvs.add(propertyName, autowiredArgument);
                }
                for (String autowiredBeanName : autowiredBeanNames) {
                    // 记录bean之间的依赖关系
                    this.registerDependentBean(autowiredBeanName, beanName);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Autowiring by type from bean name '" + beanName + "' via property '" + propertyName + "' to bean named '" + autowiredBeanName + "'");
                    }
                }
                autowiredBeanNames.clear();
            }
        } catch (BeansException ex) {
            throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, propertyName, ex);
        }
    }
}

````
##### 3 resolveDependency //解析依赖的属性

````
public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {
        //获取并初始化参数名称探测器
		descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());
		if (Optional.class == descriptor.getDependencyType()) {
            //支持java8的java.util.Optional
			return createOptionalDependency(descriptor, requestingBeanName);
		}
		else if (ObjectFactory.class == descriptor.getDependencyType() ||
				ObjectProvider.class == descriptor.getDependencyType()) {
            //ObjectFactory类注入的特殊处理
			return new DependencyObjectProvider(descriptor, requestingBeanName);
		}
		else if (javaxInjectProviderClass == descriptor.getDependencyType()) {
			return new Jsr330Factory().createDependencyProvider(descriptor, requestingBeanName);
		}
		else {
			Object result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(
					descriptor, requestingBeanName);
			if (result == null) {
                //通用处理逻辑
				result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter);
			}
			return result;
		}
	}

````

##### 3.1 doResolveDependency

````
public Object doResolveDependency(
        DependencyDescriptor descriptor, String beanName, Set<String> autowiredBeanNames, TypeConverter typeConverter)
        throws BeansException {
 
    InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
    try {
        // 快速处理
        Object shortcut = descriptor.resolveShortcut(this);
        if (shortcut != null) {
            return shortcut;
        }
 
        // 1. 如果是@Value注解，获取并返回对应的值
        Class<?> type = descriptor.getDependencyType();
        Object value = this.getAutowireCandidateResolver().getSuggestedValue(descriptor);
        if (value != null) {
            if (value instanceof String) {
                String strVal = this.resolveEmbeddedValue((String) value);
                BeanDefinition bd = (beanName != null && this.containsBean(beanName) ? this.getMergedBeanDefinition(beanName) : null);
                // 如果value是一个表达式，则解析表达式所指代的值
                value = this.evaluateBeanDefinitionString(strVal, bd);
            }
            // 类型转换并返回
            TypeConverter converter = (typeConverter != null ? typeConverter : this.getTypeConverter());
            return (descriptor.getField() != null ?
                    converter.convertIfNecessary(value, type, descriptor.getField()) :
                    converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
        }
 
        // 2. 尝试解析数组、集合类型
        Object multipleBeans = this.resolveMultipleBeans(descriptor, beanName, autowiredBeanNames, typeConverter);
        if (multipleBeans != null) {
            return multipleBeans;
        }
 
        // 3. 获取匹配类型的bean实例
        Map<String, Object> matchingBeans = this.findAutowireCandidates(beanName, type, descriptor);
        if (matchingBeans.isEmpty()) {
            if (descriptor.isRequired()) {
                this.raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
            }
            return null;
        }
 
        String autowiredBeanName;
        Object instanceCandidate;
        // 4. 存在多个匹配项
        if (matchingBeans.size() > 1) {
            // 基于优先级配置来唯一确定注入的bean @Primary @Priority
            autowiredBeanName = this.determineAutowireCandidate(matchingBeans, descriptor);
            if (autowiredBeanName == null) {
                if (descriptor.isRequired() || !this.indicatesMultipleBeans(type)) {
                    return descriptor.resolveNotUnique(type, matchingBeans);
                } else {
                    return null;
                }
            }
            instanceCandidate = matchingBeans.get(autowiredBeanName);
        } else {
            // 存在唯一的匹配
            Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
            autowiredBeanName = entry.getKey();
            instanceCandidate = entry.getValue();
        }
 
        if (autowiredBeanNames != null) {
            autowiredBeanNames.add(autowiredBeanName);
        }
        // 如果不是目标bean实例（比如工厂bean），需要进一步获取所指带的实例
        return (instanceCandidate instanceof Class ?
                descriptor.resolveCandidate(autowiredBeanName, type, this) : instanceCandidate);
    } finally {
        ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
    }
}

````

##### 3.2 

在这一步才真正将 bean 的所有属性全部注入到 bean 实例中，之前虽然已经创建了实例，但是属性仍然存在于 beanDefinition 实例中，applyPropertyValues 会将相应属性转换成 bean 中对应属性的真实类型注入到对应属性上：

````
protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
    //属性集合为空就没必要解析了
    if (pvs == null || pvs.isEmpty()) {
        return;
    }
 
    MutablePropertyValues mpvs = null;
    List<PropertyValue> original;
    //设置BeanWrapperImpl的SecurityContext属性
    if (System.getSecurityManager() != null) {
        if (bw instanceof BeanWrapperImpl) {
            ((BeanWrapperImpl) bw).setSecurityContext(this.getAccessControlContext());
        }
    }
    //MutablePropertyValues类型属性
    if (pvs instanceof MutablePropertyValues) {
        mpvs = (MutablePropertyValues) pvs;
        if (mpvs.isConverted()) {
            // 之前已经被转换为对应的类型，那么可以直接设置到beanWrapper
            try {
                bw.setPropertyValues(mpvs);
                return;
            } catch (BeansException ex) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Error setting property values", ex);
            }
        }
        // 未被转换，记录到original进行转换
        original = mpvs.getPropertyValueList();
    } else {
        // 否则，使用原始的属性获取方法
        original = Arrays.asList(pvs.getPropertyValues());
    }
    //获取自定义类型转换器
    TypeConverter converter = this.getCustomTypeConverter();
    if (converter == null) {
        //不存在则使用默认的类型转换器
        converter = bw;
    }
 
    // 获取对应的解析器
    BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);
 
    List<PropertyValue> deepCopy = new ArrayList<PropertyValue>(original.size());
    boolean resolveNecessary = false;
    // 遍历属性，将属性转换成对应类的属性类型
    for (PropertyValue pv : original) {
        if (pv.isConverted()) {
            deepCopy.add(pv);
        } else {
            // 执行类型转换
            String propertyName = pv.getName();
            Object originalValue = pv.getValue();
            //转换属性值，例如将引用转为IOC容器中实例话对象因，对属性值的解析
            Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
            //转换之后的属性值
            Object convertedValue = resolvedValue;
            boolean convertible = bw.isWritableProperty(propertyName) && !PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
            //需要转换，使用用户自定义的转换器进行转换
            if (convertible) {
                // 转换
                convertedValue = this.convertForProperty(resolvedValue, propertyName, bw, converter);
            }
            //存储转换后的属性值，避免每次属性注入时的转换工作
            if (resolvedValue == originalValue) {
                if (convertible) {
                    //保存值
                    pv.setConvertedValue(convertedValue);
                }
                deepCopy.add(pv);
              //属性是可转换的，且属性原始值是字符串类型，且属性的原始类型值不是动态生成的字符串，且属性的原始值不是集合或者数组类型
            } else if (convertible && originalValue instanceof TypedStringValue &&
                    !((TypedStringValue) originalValue).isDynamic() &&
                    !(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue))) {
                // 保存值 并保存pv
                pv.setConvertedValue(convertedValue);
                deepCopy.add(pv);
            } else {
                // 未解析完全，标记需要解析
                resolveNecessary = true;
                deepCopy.add(new PropertyValue(pv, convertedValue));
            }
        }
    }
    if (mpvs != null && !resolveNecessary) {
        mpvs.setConverted(); // 标记为已全部转换
    }
 
    try {
        // 深拷贝，进行属性依赖注入，依赖注入的最后一步
        bw.setPropertyValues(new MutablePropertyValues(deepCopy));
    } catch (BeansException ex) {
        throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Error setting property values", ex);
    }
}
````