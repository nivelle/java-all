### Spring 的 getBean()

#### getBean(String name) 

   - doGetBean(final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly);
   
     - final String beanName = transformedBeanName(name);//解析beanName，主要是解析别名、去掉FactoryBean的前缀“&”
     
     - Object sharedInstance = getSingleton(beanName);//尝试从缓存中获取beanName对应的实例
     
       #### 涉及的缓存(早期对象解决循环引用)
       
         1. singletonObjects 缓存：beanName -> 单例 bean 对象。
       
         2. earlySingletonObjects 缓存：beanName -> 单例 bean 对象，该缓存存放的是早期单例 bean 对象，可以理解成还未进行属性填充、初始化。
       
         3. singletonFactories 缓存：beanName -> ObjectFactory。
       
         4. singletonsCurrentlyInCreation 缓存：当前正在创建单例 bean 对象的 beanName 集合。
                
     - Object singletonObject = this.singletonObjects.get(beanName);//从单例对象缓存中获取beanName对应的单例对象;如果单例对象缓存中没有，并且该beanName对应的单例bean正在创建中
       
       #### 从早期单例对象缓存中获取单例对象（之所称成为早期单例对象，是因为earlySingletonObjects里的对象的都是通过提前曝光的ObjectFactory创建出来的，还未进行属性填充等操作)如果在早期单例对象缓存中也没有，并且允许创建早期单例对象引用

       - singletonObject = this.earlySingletonObjects.get(beanName);
              
       - ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);//从单例工厂缓存中获取beanName的单例工厂
       
       - singletonObject = singletonFactory.getObject();//如果存在单例对象工厂，则通过工厂创建一个单例对象
       
       - this.earlySingletonObjects.put(beanName, singletonObject);//将通过单例对象工厂创建的单例对象，放到早期单例对象缓存中
       
       - this.singletonFactories.remove(beanName);//移除该beanName对应的单例对象工厂，因为该单例工厂已经创建了一个实例对象，并且放到earlySingletonObjects缓存了，因此，后续获取beanName的单例对象，可以通过earlySingletonObjects缓存拿到，不需要在用到该单例工厂
       
     - bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);//返回beanName对应的实例对象（主要用于FactoryBean的特殊处理，普通Bean会直接返回sharedInstance本身）
     
       - 判断:如果name以“&”为前缀，但是beanInstance不是FactoryBean，则抛异常
       
       - !(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name);//如果beanInstance不是FactoryBean（也就是普通bean），则直接返回beanInstance 或者 如果beanInstance是FactoryBean，并且name以“&”为前缀，则直接返回beanInstance（以“&”为前缀代表想获取的是FactoryBean本身）
       
       #### 走到这边，代表beanInstance是FactoryBean，但name不带有“&”前缀，表示想要获取的是FactoryBean创建的对象实例
       
       - object = getCachedObjectForFactoryBean(beanName);//如果 RootBeanDefinition 为空，则尝试从factoryBeanObjectCache缓存中获取该FactoryBean创建的对象实例
       
       - getMergedLocalBeanDefinition(beanName);// RootBeanDefinition为空，但是该bean的BeanDefinition在缓存中存在，则获取该bean的MergedBeanDefinition
       
       - object = getObjectFromFactoryBean(factory, beanName, !synthetic);//从FactoryBean获取对象实例
       
         #### factory.isSingleton() && containsSingleton(beanName);//判断是否是单例而且已经存在于单例对象缓存中
         
         - Object object = this.factoryBeanObjectCache.get(beanName);//从FactoryBean创建的单例对象的缓存中获取该bean实例
         
         - object = doGetObjectFromFactoryBean(factory, beanName);//调用FactoryBean的getObject方法获取对象实例
         
         - Object alreadyThere = this.factoryBeanObjectCache.get(beanName);//从缓存中获取,如果该beanName已经在缓存中存在，则将object替换成缓存中的
         
         - 缓存中不存在: object = postProcessObjectFromFactoryBean(object, beanName);//对bean实例进行后置处理，执行所有已注册的BeanPostProcessor的postProcessAfterInitialization方法
         
         - this.factoryBeanObjectCache.put(beanName, (object != null ? object : NULL_OBJECT));//将beanName和object放到factoryBeanObjectCache缓存中
         
         #### 判断如果不是 单例或者缓存中不存在此bean
         
         - 	Object object = doGetObjectFromFactoryBean(factory, beanName);//调用FactoryBean的getObject方法获取对象实例
         
         -  object = postProcessObjectFromFactoryBean(object, beanName);//对bean实例进行后置处理，执行所有已注册的BeanPostProcessor的postProcessAfterInitialization方法
       
     - isPrototypeCurrentlyInCreation(beanName);//scope为prototype的循环依赖校验：如果beanName已经正在创建Bean实例中，而此时我们又要再一次创建beanName的实例，则代表出现了循环依赖，需要抛出异常。
     
     - BeanFactory parentBeanFactory = getParentBeanFactory();//获取parentBeanFactory
     
     - parentBeanFactory != null && !containsBeanDefinition(beanName);//如果parentBeanFactory存在，并且beanName在当前BeanFactory不存在Bean定义，则尝试从parentBeanFactory中获取bean实例
     
     - String nameToLookup = originalBeanName(name);//将别名解析成真正的beanName
     
     - parentBeanFactory.getBean(nameToLookup, requiredType);//尝试在parentBeanFactory中获取bean对象实例
     
     - markBeanAsCreated(beanName);//如果不是仅仅做类型检测，而是创建bean实例，这里要将beanName放到alreadyCreated缓存
     
     - final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);//根据beanName重新获取MergedBeanDefinition（步骤6将MergedBeanDefinition删除了，这边获取一个新的）
     
     - checkMergedBeanDefinition(mbd, beanName, args);//检查MergedBeanDefinition
     
     - String[] dependsOn = mbd.getDependsOn();//拿到当前bean依赖的bean名称集合，在实例化自己之前，需要先实例化自己依赖的bean
     
       #### 循环处理依赖的bean
       
       - registerDependentBean(dep, beanName);//将dep和beanName的依赖关系注册到缓存中 -》 dependentBeanMap;dependenciesForBeanMap key和value呼唤
       
       - getBean(dep);//获取依赖的实例
       
     - mbd.isSingleton();//如果是单例模式
     
       -  sharedInstance = getSingleton(beanName, new ObjectFactory<Object>());//scope为singleton的bean创建（新建了一个ObjectFactory，并且重写了getObject方法）
       
       -  bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
       
     - mbd.isPrototype();//如果是原型模式
     
       - beforePrototypeCreation(beanName);//创建实例前的操作（将beanName保存到prototypesCurrentlyInCreation缓存中）
      
       - 创建Bean实例:prototypeInstance = createBean(beanName, mbd, args); 
       
         ##### 子类实现: AbstractAutowireCapableBeanFactory
       
         - Class<?> resolvedClass = resolveBeanClass(mbd, beanName);//获取类的全限定名
         
         #### 如果resolvedClass存在，并且mdb的beanClass类型不是Class，并且mdb的beanClass不为空（则代表beanClass存的是Class的name）,则使用mdb深拷贝一个新的RootBeanDefinition副本，并且将解析的Class赋值给拷贝的RootBeanDefinition副本的beanClass属性，该拷贝副本取代mdb用于后续的操作
         
         - mbdToUse = new RootBeanDefinition(mbd);
         
         - mbdToUse.setBeanClass(resolvedClass);
         
         - mbdToUse.prepareMethodOverrides();//验证及准备覆盖的方法（对override属性进行标记及验证）
          
           ### 实例化前的处理，给InstantiationAwareBeanPostProcessor一个机会返回代理对象来替代真正的bean实例，达到“短路”效果
                      
         - Object bean = resolveBeforeInstantiation(beanName, mbdToUse); //Give BeanPostProcessors a chance to return a proxy instead of the target bean ins返回的是代理对象;            [Spring源码解析之AOP实现.md](AnnotationAwareAspectJAutoProxyCreator 后置处理器的使用,返回AOP代理类)
           [AnnotationAwareAspectJAutoProxyCreator 后置处理器的使用,返回AOP代理类](./Spring源码解析之AOP实现.md) 
           
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