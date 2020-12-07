### Spring 的 getBean()

#### getBean(String name) 

#### doGetBean(final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly);
  
##### 第1步: final String beanName = transformedBeanName(name);     
    
````
传入的参数可以是alias,也可能是FactoryBean的name,所以需要进行解析：
1. 如果是FactoryBean，则去掉修饰符"&"
2. 沿着引用链获取alias对应的最终name
````
##### 第2步:Object sharedInstance = getSingleton(beanName);//检查缓存或者实例工厂中是否有对应的单例
     
##### (早期对象解决循环引用)在创建单例bean的时候会存在依赖注入的情况，而在创建依赖的时候为了避免循环依赖Spring创建bean的原则是不等bean创建完成就会将创建bean的ObjectFactory提前曝光（将对应的ObjectFactory加入到缓存）一旦下一个bean创建需要依赖上一个bean，则直接使用ObjectFactory对象

````
        protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        		// Quick check for existing instance without full singleton lock
                // Cache of singleton objects: bean name to bean instance
                //从单例对象缓存中获取beanName对应的单例对象
        		Object singletonObject = this.singletonObjects.get(beanName);
                //该beanName对应的单例bean正在创建中
        		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
                    //Cache of early singleton objects: bean name to bean instance
                    //从早期单例对象缓存中获取单例对象（之所称成为早期单例对象，是因为earlySingletonObjects里的对象的都是通过提前曝光的ObjectFactory创建出来的，还未进行属性填充等操作)
        			singletonObject = this.earlySingletonObjects.get(beanName);
        			if (singletonObject == null && allowEarlyReference) {
        				synchronized (this.singletonObjects) {
        					// Consistent creation of early reference within full singleton lock
        					singletonObject = this.singletonObjects.get(beanName);
        					if (singletonObject == null) {
        						singletonObject = this.earlySingletonObjects.get(beanName);
        						if (singletonObject == null) {
                                    //Cache of singleton factories: bean name to ObjectFactory
                                    //从单例工厂缓存中获取beanName的单例工厂
                                    //当某些方法需要提前初始化的时候也即允许早期依赖，会调用addSingletonFactory将对应的objectFactory初始化策略存储在singletonFactories中
        							ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);// singletonFactories以beanName为key存储创建bean的工厂
        							if (singletonFactory != null) {
                                        //如果存在单例对象工厂，则通过工厂创建一个单例对象
        								singletonObject = singletonFactory.getObject();
                                        //添加到bean早期实例缓存,并从实例工厂缓存移除
        								this.earlySingletonObjects.put(beanName, singletonObject);
                                        //移除该beanName对应的单例对象工厂，因为该单例工厂已经创建了一个实例对象，并且放到earlySingletonObjects缓存了，因此，后续获取beanName的单例对象，可以通过earlySingletonObjects缓存拿到，不需要在用到该单例工厂
                                        //earlySingletonObjects和singletonFactories互斥
        								this.singletonFactories.remove(beanName);
        							}
        						}
        					}
        				}
        			}
        		}
        		return singletonObject;
        	}
````     
      
##### 第2.1步 如果getSingleton不为空，实例已经存在，返回对应的实例//返回beanName对应的实例对象（主要用于FactoryBean的特殊处理，普通Bean会直接返回sharedInstance本身）
     
-  bean = getObjectForBeanInstance(sharedInstance, name, beanName, null); 
      
````
      protected Object getObjectForBeanInstance(
     			Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {
     
     		// Don't let calling code try to dereference the factory if the bean isn't a factory.
     		// 带有 & 前缀
     		if (BeanFactoryUtils.isFactoryDereference(name)) {
     			if (beanInstance instanceof NullBean) {
     				return beanInstance;
     			}
     			//判断:如果name以“&”为前缀，但是beanInstance不是FactoryBean，则抛异常
     			if (!(beanInstance instanceof FactoryBean)) {
     				throw new BeanIsNotAFactoryException(beanName, beanInstance.getClass());
     			}
     			if (mbd != null) {
     				mbd.isFactoryBean = true;
     			}
                //本来就是希望获取FactoryBean实例
     			return beanInstance;
     		}
     
     		// Now we have the bean instance, which may be a normal bean or a FactoryBean.
     		// If it's a FactoryBean, we use it to create a bean instance, unless the
     		// caller actually wants a reference to the factory.
     		if (!(beanInstance instanceof FactoryBean)) {
     		    //如果beanInstance不是FactoryBean(也就是普通bean)则直接返回beanInstance
     			return beanInstance;
     		}
     
     		Object object = null;
     		if (mbd != null) {
     			mbd.isFactoryBean = true;
     		}
     		//走到这边，代表beanInstance是FactoryBean，但name不带有“&”前缀，表示想要获取的是FactoryBean创建的对象实例
     		else {
     		    //如果 RootBeanDefinition 为空.则尝试从factoryBeanObjectCache缓存中获取该FactoryBean创建的对象实例
     			object = getCachedObjectForFactoryBean(beanName);
     		}
     		if (object == null) {
     			// Return bean instance from factory.
     			FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
     			// Caches object obtained from FactoryBean if it is a singleton.
     			if (mbd == null && containsBeanDefinition(beanName)) {
     				mbd = getMergedLocalBeanDefinition(beanName);
     			}
     			boolean synthetic = (mbd != null && mbd.isSynthetic());
                //核心代码：getObjectFromFactoryBean,从工厂类获取实例
     			object = getObjectFromFactoryBean(factory, beanName, !synthetic);
     		}
     		return object;
     	}


````
   - object = getObjectFromFactoryBean(factory, beanName, !synthetic);//从FactoryBean获取对象实例
   
````
     protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
     		if (factory.isSingleton() && containsSingleton(beanName)) {
                //如果是单例，且factoryBean已经实例化
     			synchronized (getSingletonMutex()) {
     			    //从FactoryBean创建的单例对象的缓存中获取该bean实例
     				Object object = this.factoryBeanObjectCache.get(beanName);
     				if (object == null) {
     				    //调用FactoryBean的getObject方法获取对象实例
     					object = doGetObjectFromFactoryBean(factory, beanName);
     					// Only post-process and store if not put there already during getObject() call above
     					// (e.g. because of circular reference processing triggered by custom getBean calls)
     					// 从缓存中获取,如果该beanName已经在缓存中存在，则将object替换成缓存中已经创建的
     					Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
     					if (alreadyThere != null) {
     						object = alreadyThere;
     					}
     					else {
     						if (shouldPostProcess) {
     							if (isSingletonCurrentlyInCreation(beanName)) {
     								// Temporarily return non-post-processed object, not storing it yet..
     								return object;
     							}
     							//创建回调函数添加到 singletonsCurrentlyInCreation 缓存
     							beforeSingletonCreation(beanName);
     							try {
     							    //对bean实例进行后置处理，
     							    // AbstractAutowireCapableBeanFactory子类实现: 执行所有已注册的BeanPostProcessor的postProcessAfterInitialization方法
     								object = postProcessObjectFromFactoryBean(object, beanName);
     							}
     							catch (Throwable ex) {
     								throw new BeanCreationException(beanName,"Post-processing of FactoryBean's singleton object failed", ex);
     							}
     							finally {
     							    //创建完成后的回调函数从 singletonsCurrentlyInCreation 缓存中移除
     								afterSingletonCreation(beanName);
     							}
     						}
     						if (containsSingleton(beanName)) {
     						   // 将 当前创建的对象放到 factoryBeanObjectCache 缓存中
     							this.factoryBeanObjectCache.put(beanName, object);
     						}
     					}
     				}
     				return object;
     			}
     		}
     		else {
     			Object object = doGetObjectFromFactoryBean(factory, beanName);
     			if (shouldPostProcess) {
     				try {
     					object = postProcessObjectFromFactoryBean(object, beanName);
     				}
     				catch (Throwable ex) {
     					throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
     				}
     			}
     			return object;
     		}
     	}
          
````

   - private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName) //工厂方法创建bean实例
   
````
private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName) throws BeanCreationException {
    Object object;
    try {
        // 需要权限验证
        if (System.getSecurityManager() != null) {
            AccessControlContext acc = this.getAccessControlContext();
            try {
                object = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                    @Override
                    public Object run() throws Exception {
                        return factory.getObject();
                    }
                }, acc);
            } catch (PrivilegedActionException pae) {
                throw pae.getException();
            }
        } else {
            // 直接调用getObject方法，FactoryBean的getObject方法代理了getBean
            object = factory.getObject();
        }
    } catch (FactoryBeanNotInitializedException ex) {
        throw new BeanCurrentlyInCreationException(beanName, ex.toString());
    } catch (Throwable ex) {
        throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
    }
    // FactoryBean未成功创建对象，或factoryBean实例正在被创建
    if (object == null && this.isSingletonCurrentlyInCreation(beanName)) {
        throw new BeanCurrentlyInCreationException(beanName, "FactoryBean which is currently in creation returned null from getObject");
    }
    return object;
}

````
           
##### 第2.2步 单例实例不存在,需要创建


  ````
  {
  			// Fail if we're already creating this bean instance:
  			// We're assumably within a circular reference.
            // 只有在单例模式下才会尝试解决循环依赖问题,对于原型模式，如果存在循环依赖，也就是满足this.isPrototypeCurrentlyInCreation(beanName)，抛出异常:BeanCurrentlyInCreationException(beanName)
  			if (isPrototypeCurrentlyInCreation(beanName)) {
  				throw new BeanCurrentlyInCreationException(beanName);
  			}
  
  			// Check if bean definition exists in this factory.
            //获取parentBeanFactory
  			BeanFactory parentBeanFactory = getParentBeanFactory();
            //如果在beanDefinitionMap中（即所有已经加载的类中）不包含目标bean，则尝试从parentBeanFactory中获取
  			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
  				// Not found -> check parent.
                // 获取name对应的真正beanName，如果是factoryBean，则加上“&”前缀
  				String nameToLookup = originalBeanName(name);
  				if (parentBeanFactory instanceof AbstractBeanFactory) {
                    //递归到BeanFactory中寻找
  					return ((AbstractBeanFactory) parentBeanFactory).doGetBean(nameToLookup, requiredType, args, typeCheckOnly);
  				}
  				else if (args != null) {
  					// Delegation to parent with explicit args.
  					return (T) parentBeanFactory.getBean(nameToLookup, args);
  				}
  				else if (requiredType != null) {
  					// No args -> delegate to standard getBean method.
  					return parentBeanFactory.getBean(nameToLookup, requiredType);
  				}
  				else {
  					return (T) parentBeanFactory.getBean(nameToLookup);
  				}
  			}
            //如果不仅仅是做类型检查，标记bean的状态已经创建，即将beanName加入alreadCreated集合中
  			if (!typeCheckOnly) {
  				markBeanAsCreated(beanName);
  			}
  
  			try {
                //将存储XML配置的GenericBeanDefinition实例转换成RootBeanDefinition实例，方便后续处理，如果存在父bean,则同时合并父bean的相关属性
  				RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                //检查bean是否是抽象的，如果是则抛出异常
  				checkMergedBeanDefinition(mbd, beanName, args);
  
  				// Guarantee initialization of beans that the current bean depends on.
                // 拿到当前bean依赖的bean名称集合，在实例化自己之前，需要先实例化自己依赖的bean
  				String[] dependsOn = mbd.getDependsOn();
  				if (dependsOn != null) {
  					for (String dep : dependsOn) {
                        //检查dep是否依赖beanName,从而导致循环依赖
  						if (isDependent(beanName, dep)) {
  							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
  									"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
  						}
                        //将dep和beanName的依赖关系注册到缓存中 -》 dependentBeanMap;dependenciesForBeanMap key和value 互换
  						registerDependentBean(dep, beanName);
  						try {
                            //递归调用，获取依赖的实例
  							getBean(dep);
  						}
  						catch (NoSuchBeanDefinitionException ex) {
  							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
  									"'" + beanName + "' depends on missing bean '" + dep + "'", ex);
  						}
  					}
  				}
  
  				// Create bean instance.
                //如果是单例模式
  				if (mbd.isSingleton()) {
                    //scope为singleton的bean创建(新建了一个ObjectFactory,并且重写了getObject方法)
  					sharedInstance = getSingleton(beanName, () -> {
  						try {
                            //创建bean实例
  							return createBean(beanName, mbd, args);
  						}
  						catch (BeansException ex) {
  							// Explicitly remove instance from singleton cache: It might have been put there
  							// eagerly by the creation process, to allow for circular reference resolution.
  							// Also remove any beans that received a temporary reference to the bean.
  							destroySingleton(beanName);
  							throw ex;
  						}
  					});
  					bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
  				}
                //如果是原型模式
  				else if (mbd.isPrototype()) {
  					// It's a prototype -> create a new instance.
  					Object prototypeInstance = null;
  					try {
                        //创建实例前的操作（将beanName保存到prototypesCurrentlyInCreation缓存中）
  						beforePrototypeCreation(beanName);
  						prototypeInstance = createBean(beanName, mbd, args);
  					}
  					finally {
  						afterPrototypeCreation(beanName);
  					}
  					bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
  				}
                //其他模式
  				else {
  					String scopeName = mbd.getScope();
  					if (!StringUtils.hasLength(scopeName)) {
  						throw new IllegalStateException("No scope name defined for bean ´" + beanName + "'");
  					}
  					Scope scope = this.scopes.get(scopeName);
  					if (scope == null) {
  						throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
  					}
  					try {
  						Object scopedInstance = scope.get(beanName, () -> {
  							beforePrototypeCreation(beanName);
  							try {
  								return createBean(beanName, mbd, args);
  							}
  							finally {
  								afterPrototypeCreation(beanName);
  							}
  						});
  						bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
  					}
  					catch (IllegalStateException ex) {
  						throw new BeanCreationException(beanName,
  								"Scope '" + scopeName + "' is not active for the current thread; consider " +
  								"defining a scoped proxy for this bean if you intend to refer to it from a singleton",
  								ex);
  					}
  				}
  			}
  			catch (BeansException ex) {
  				cleanupAfterBeanCreationFailure(beanName);
  				throw ex;
  			}
  		}


  ````
  
#### 第三步: 检查类型

````

        // Check if required type matches the type of the actual bean instance.
        // 检查需要的类型是否符合bean的实际类型，对应getBean时指定的requireType
		if (requiredType != null && !requiredType.isInstance(bean)) {
			try {
                //执行类型转换，转换成期望的类型
				T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
				if (convertedBean == null) {
					throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
				}
				return convertedBean;
			}
			catch (TypeMismatchException ex) {
				if (logger.isTraceEnabled()) {
					logger.trace("Failed to convert bean '" + name + "' to required type '" +
							ClassUtils.getQualifiedName(requiredType) + "'", ex);
				}
				throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
			}
		}
		return (T) bean;


````    






















      
   #### 循环处理依赖的bean
              
   - getBean(dep);//
              
  
   - 创建Bean实例:prototypeInstance = createBean(beanName, mbd, args); 
       
         ##### 子类实现: AbstractAutowireCapableBeanFactory
       
         - Class<?> resolvedClass = resolveBeanClass(mbd, beanName);//获取类的全限定名
         
         #### 
         
         - mbdToUse = new RootBeanDefinition(mbd);
         
         - mbdToUse.setBeanClass(resolvedClass);
         
         - mbdToUse.prepareMethodOverrides();//验证及准备覆盖的方法（对override属性进行标记及验证）
          
           ### 实例化前的处理，给InstantiationAwareBeanPostProcessor一个机会返回代理对象来替代真正的bean实例，达到“短路”效果
                      
         - Object bean = resolveBeforeInstantiation(beanName, mbdToUse); //Give BeanPostProcessors a chance to return a proxy instead of the target bean ins返回的是代理对象;            [Spring源码解析之AOP实现.md](AnnotationAwareAspectJAutoProxyCreator 后置处理器的使用,返回AOP代理类)
           
           
           #### if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors());//mbd不是合成的，并且BeanFactory中存在InstantiationAwareBeanPostProcessor
           
           - Class<?> targetType = determineTargetType(beanName, mbd);//解析beanName对应的Bean实例的类型

           #### 如果返回的bean不为空，会跳过Spring默认的实例化过程所以只能在这里调用BeanPostProcessor实现类的postProcessAfterInitialization方法
           
           - bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);//实例化前的后置处理器应用（处理InstantiationAwareBeanPostProcessor）
        
           - bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
           
           - mbd.beforeInstantiationResolved = (bean != null);//如果bean不为空，则将beforeInstantiationResolved赋值为true，代表在实例化之前已经解析
           
         ### 未短路情况下,创建Bean实例（真正创建Bean的方法）

         - Object beanInstance = doCreateBean(beanName, mbdToUse, args);
         
            - BeanWrapper instanceWrapper = null;//新建Bean包装类
            
            - instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);//如果是FactoryBean，则需要先移除未完成的FactoryBean实例的缓存
            
            ### 根据beanName、mbd、args，使用对应的策略创建Bean实例，并返回包装类BeanWrapper 
        
            - instanceWrapper = createBeanInstance(beanName, mbd, args); [createBeanInstance](./Spring源码解析之createBeanInstance()方法.md)
            
            - final Object bean = (instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null);//拿到创建好的Bean实例
            
            - Class<?> beanType = (instanceWrapper != null ? instanceWrapper.getWrappedClass() : null);//拿到Bean实例的类型
            
            #### // Allow post-processors to modify the merged bean definition.
            
            ### 应用后置处理器MergedBeanDefinitionPostProcessor，允许修改MergedBeanDefinition,Autowired 注解正是通过此方法实现注入类型的预解析
            
            - applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName); 
            
            ### 判断是否需要提早曝光实例：单例 && 允许循环依赖 && 当前bean正在创建中
            
            - boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences && isSingletonCurrentlyInCreation(beanName));
            
            - addSingletonFactory(beanName, new ObjectFactory<Object>();//提前曝光beanName的ObjectFactory，用于解决循环引用
            
              - getEarlyBeanReference(beanName, mbd, bean);//应用后置处理器SmartInstantiationAwareBeanPostProcessor，允许返回指定bean的早期引用，若没有则直接返回bean
            
            - Object exposedObject = bean;//Initialize the bean instance.  初始化bean实例。
            
            ### 对bean进行属性填充;其中，可能存在依赖于其他bean的属性，则会递归初始化依赖的bean实例
            - populateBean(beanName, mbd, instanceWrapper);
            
              - PropertyValues pvs = mbd.getPropertyValues();//返回此bean的属性值
               
               // Give any InstantiationAwareBeanPostProcessors the opportunity to modify the state of the bean before properties are set. This can be used, for example,
               // to support styles of field injection.
               - boolean continueWithPropertyPopulation = true;
               
               #### 如果mbd不是合成的 && 存在InstantiationAwareBeanPostProcessor，则遍历处理InstantiationAwareBeanPostProcessor
               
               - !ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName);//在bean实例化后，属性填充之前被调用，允许修改bean的属性，如果返回false，则跳过之后的属性填充
               
               - continueWithPropertyPopulation = false;//如果返回false，将continueWithPropertyPopulation赋值为false，代表要跳过之后的属性填充
               
               - autowireByName(beanName, mbd, bw, newPvs);//解析autowireByName的注入
               
               - autowireByType(beanName, mbd, bw, newPvs);//解析autowireByType的注入
               
               - boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();//BeanFactory是否注册过InstantiationAwareBeanPostProcessors
               
               - boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);//是否需要依赖检查

               #### if (hasInstAwareBpps || needsDepCheck)
               
               - PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
               
               - pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);//应用后置处理器InstantiationAwareBeanPostProcessor的方法postProcessPropertyValues，进行属性填充前的再次处理。例子：现在最常用的@Autowire属性注入就是这边注入依赖的bean实例对象
               
               - checkDependencies(beanName, mbd, filteredPds, pvs);// 依赖检查，对应depends-on属性
               
               - applyPropertyValues(beanName, mbd, bw, pvs); 

            - exposedObject = initializeBean(beanName, exposedObject, mbd);//对bean进行初始化 [spring initializeBean()方法](./Spring源码解析之initializeBean()方法.md)
            
            #### 如果允许提前曝光实例，则进行循环依赖检查;earlySingletonReference 只有在当前解析的bean存在循环依赖的情况下才会不为空
            ####(如果exposedObject在initializeBean方法中被增强 && 不允许在循环引用的情况下使用注入原始bean实例 && 当前bean有被其他bean依赖)
            
            - Object earlySingletonReference = getSingleton(beanName, false);
            
            - exposedObject = earlySingletonReference;//如果exposedObject没有在initializeBean方法中被增强，则不影响之前的循环引用
                        
            - String[] dependentBeans = getDependentBeans(beanName);//拿到依赖当前bean的所有bean的beanName数组
            
            - !removeSingletonIfCreatedForTypeCheckOnly(dependentBean);//尝试移除这些bean的实例，因为这些bean依赖的bean已经被增强了，他们依赖的bean相当于脏数据
            
            #### 跑出异常:BeanCurrentlyInCreationException: Bean with name '" + beanName + "' has been injected into other beans 
            -  actualDependentBeans.add(dependentBean);//移除失败的添加到 actualDependentBeans,如果集合不为空则跑出异常
            
            #### 注册用于销毁的bean，执行销毁操作的有三种：自定义destroy方法、DisposableBean接口、DestructionAwareBeanPostProcessor
            
            - registerDisposableBeanIfNecessary(beanName, bean, mbd);
            
            - return exposedObject;   //完成创建并返回
            
       - afterPrototypeCreation(beanName);//创建实例后的操作（将创建完的beanName从prototypesCurrentlyInCreation缓存中移除）
      
       - bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
     
     - 其他模式（request,response,session）
     
       - String scopeName = mbd.getScope();
       
       - final Scope scope = this.scopes.get(scopeName);
       
       - beforePrototypeCreation(beanName);//创建实例前的操作（将beanName保存到prototypesCurrentlyInCreation缓存中）
       
       - createBean(beanName, mbd, args);//创建实例
       
       - afterPrototypeCreation(beanName);//创建实例后的操作（将创建完的beanName从prototypesCurrentlyInCreation缓存中移除）
       
       - bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd)//创建Bean实例
       
     - if (requiredType != null && bean != null && !requiredType.isInstance(bean))
     
     - getTypeConverter().convertIfNecessary(bean, requiredType);//类型不对，则尝试转换bean类型
     
     - 返回创建出来的bean实例对象