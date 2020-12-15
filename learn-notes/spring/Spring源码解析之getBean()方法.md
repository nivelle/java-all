### Spring 的 getBean()
1. 获取参数 name 对应的真正的 beanName
2. 检查缓存或者实例工厂中是否有对应的单例，若存在则进行实例化并返回对象，否则继续往下执行
3. 执行 prototype 类型依赖检查，防止循环依赖
4. 如果当前 beanFactory 中不存在需要的 bean，则尝试从 parentBeanFactory 中获取
5. 将之前解析过程返得到的 GenericBeanDefinition 对象合并为 RootBeanDefinition 对象，便于后续处理
6. 如果存在依赖的 bean，则进行递归加载
7. 依据当前 bean 的作用域对 bean 进行实例化
8. 如果对返回 bean 类型有要求，则进行类型检查，并按需做类型转换
9. 返回 bean 实例

#### getBean(String name) -》doGetBean(final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly);
  
##### 第1步: final String beanName = transformedBeanName(name); //传入的参数可以是alias,也可能是FactoryBean的name,所以需要进行解析：

- 如果是FactoryBean，则去掉修饰符"&"

- 沿着引用链获取alias对应的最终name

##### 第2.1步: Object sharedInstance = getSingleton(beanName);//尝试从单例集合中获取目标bean
     
##### (早期对象解决循环引用)在创建单例bean的时候会存在依赖注入的情况，而在创建依赖的时候为了避免循环依赖Spring创建bean的原则是不等bean创建完成就会将创建bean的ObjectFactory提前曝光（将对应的ObjectFactory加入到缓存）一旦下一个bean创建需要依赖上一个bean，则直接使用ObjectFactory对象

````
//allowEarlyReference = true //允许对bean的早期依赖
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        // Quick check for existing instance without full singleton lock
        // Cache of singleton objects: bean name to bean instance
        // 从单例对象缓存中获取beanName对应的单例对象
        // singletonObjects以beanName为key存储bean实例
        Object singletonObject = this.singletonObjects.get(beanName);
        //缓存中为空 同时 该beanName对应的单例bean正在创建中
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
                            //singletonFactories以beanName为key存储创建bean的工厂
                            ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                            if (singletonFactory != null) {
                                //如果存在单例对象工厂函数，则通过工厂创建一个单例对象
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
##### 重点
1. singletonObjects：用于保存 beanName 和 bean 实例之间的关系

2. singletonFactories：用于保存 beanName 和创建 bean 的工厂之间的关系

3. earlySingletonObjects：也是保存 beanName 和 bean 实例之间的关系，不同于 singletonObjects，当一个bean的实例放置于其中后，当bean还在创建过程中就可以通过 getBean 方法获取到

4. registeredSingletons：用来保存当前所有已注册的 bean
      
##### 第2.2步: bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);//从 bean 实例中获取目标对象

- 如果getSingleton不为空，实例已经存在，返回对应的实例；返回beanName对应的实例对象（主要用于FactoryBean的特殊处理，普通Bean会直接返回sharedInstance本身）
  
- 该方法的主要目的是判断当前 bean 实例是否是 FactoryBean，如果是 FactoryBean 实例，同时用户希望获取的是真正的 bean 实例（即 name 不是以 “&” 开头），此时就需要由 FactoryBean 实例创建目标 bean 实例    
````
rotected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {
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
     //走到这边,代表beanInstance是FactoryBean,但name不带有“&”前缀,表示想要获取的是FactoryBean创建的对象实例
     else {
         //重点: 如果 RootBeanDefinition 为空.则尝试从 factoryBeanObjectCache 缓存中获取该FactoryBean创建的对象实例
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
         //重点 核心代码：getObjectFromFactoryBean,从工厂类获取实例
         object = getObjectFromFactoryBean(factory, beanName, !synthetic);
     }
     return object;
 }


````
##### 第2.2.1步: object = getObjectFromFactoryBean(factory, beanName, !synthetic);//从FactoryBean获取对象实例
````
 protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
     		if (factory.isSingleton() && containsSingleton(beanName)) {
                //如果是单例,且factoryBean已经实例化
     			synchronized (getSingletonMutex()) {
     			    //从FactoryBean创建的单例对象的缓存中获取该bean实例
     				Object object = this.factoryBeanObjectCache.get(beanName);
     				if (object == null) {
     				    //调用FactoryBean的getObject方法获取对象实例
     					object = doGetObjectFromFactoryBean(factory, beanName);
     					// Only post-process and store if not put there already during getObject() call above
     					// (e.g. because of circular reference processing triggered by custom getBean calls)
     					// 从缓存中获取,如果该beanName已经在缓存中存在，则将object替换成缓存中已经创建的,说明已经被创建过了
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
                                //singletonsCurrentlyInCreation 将beanName设置为创建中
     							beforeSingletonCreation(beanName);
     							try {
     							    // 对bean实例进行后置处理，
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
     						   // 将当前创建的对象放到 factoryBeanObjectCache 缓存中
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

##### 第2.2.3步: private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName) //工厂方法创建bean实例

- 当我们在调用 BeanFactory 的 getBean 方法不加 “&” 获取 bean 实例时，这个时候 getBean 可以看做是 getObject 方法的代理方法，而具体调用就在 doGetObjectFromFactoryBean 方法中
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
    // FactoryBean未成功 创建对象，或factoryBean实例正在被创建
    if (object == null && this.isSingletonCurrentlyInCreation(beanName)) {
        throw new BeanCurrentlyInCreationException(beanName, "FactoryBean which is currently in creation returned null from getObject");
    }
    return object;
}

````
           
##### 第3步: 单例实例不存在,需要创建 [创建bean实例](./Spring源码解析之createBean()方法.md)

````
  {
  			// Fail if we're already creating this bean instance:
  			// We're assumably within a circular reference.
            // 只有在单例模式下才会尝试解决循环依赖问题,对于原型模式，如果存在循环依赖，
            // 也就是满足this.isPrototypeCurrentlyInCreation(beanName),抛出异常:BeanCurrentlyInCreationException(beanName)
  			if (isPrototypeCurrentlyInCreation(beanName)) {
  				throw new BeanCurrentlyInCreationException(beanName);
  			}
  			// Check if bean definition exists in this factory.
            //获取parentBeanFactory
  			BeanFactory parentBeanFactory = getParentBeanFactory();
            //如果在beanDefinitionMap中（即所有已经加载的类中）不包含目标bean，则尝试从parentBeanFactory中获取
  			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
  				// Not found -> check parent.
                // 获取name对应的真正beanName,如果是factoryBean,则加上“&”前缀
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
                // 拿到当前bean依赖的bean名称集合,在实例化自己之前,需要先实例化自己依赖的bean
  				String[] dependsOn = mbd.getDependsOn();
  				if (dependsOn != null) {
  					for (String dep : dependsOn) {
                        //检查dep是否依赖beanName,从而导致循环依赖
  						if (isDependent(beanName, dep)) {
  							throw new BeanCreationException(mbd.getResourceDescription(), beanName,"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
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
                            //使用createBean方法来创建bean实例
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
  
#### 第四步: 检查类型&类型转换

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