## Spring 的 createBean()

-  getBean(String name) 

   - doGetBean(final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly);
   
     - final String beanName = transformedBeanName(name);//解析beanName，主要是解析别名、去掉FactoryBean的前缀“&”
     
     - Object sharedInstance = getSingleton(beanName);//尝试从缓存中获取beanName对应的实例
     
       #### 涉及的缓存(早期对象解决循环引用)
       
         1. singletonObjects 缓存：beanName -> 单例 bean 对象。
       
         2. earlySingletonObjects 缓存：beanName -> 单例 bean 对象，该缓存存放的是早期单例 bean 对象，可以理解成还未进行属性填充、初始化。
       
         3. singletonFactories 缓存：beanName -> ObjectFactory。
       
         4. singletonsCurrentlyInCreation 缓存：当前正在创建单例 bean 对象的 beanName 集合。
                
     - Object singletonObject = this.singletonObjects.get(beanName);//从单例对象缓存中获取beanName对应的单例对象;如果单例对象缓存中没有，并且该beanName对应的单例bean正在创建中
       
       - singletonObject = this.earlySingletonObjects.get(beanName);
       //从早期单例对象缓存中获取单例对象（之所称成为早期单例对象，是因为earlySingletonObjects里的对象的都是通过提前曝光的ObjectFactory创建出来的，还未进行属性填充等操作)
       //如果在早期单例对象缓存中也没有，并且允许创建早期单例对象引用
       
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
      
       - prototypeInstance = createBean(beanName, mbd, args);//创建Bean实例
      
       - afterPrototypeCreation(beanName);//创建实例后的操作（将创建完的beanName从prototypesCurrentlyInCreation缓存中移除）
      
       - bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
     
     - 其他模式（request,response,session）
     
       - String scopeName = mbd.getScope();
       
       - final Scope scope = this.scopes.get(scopeName);
       
       - beforePrototypeCreation(beanName);//创建实例前的操作（将beanName保存到prototypesCurrentlyInCreation缓存中）
       
       - createBean(beanName, mbd, args);//创建实例
       
       -  afterPrototypeCreation(beanName);//创建实例后的操作（将创建完的beanName从prototypesCurrentlyInCreation缓存中移除）
       
       -  bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd)//创建Bean实例
       
     - if (requiredType != null && bean != null && !requiredType.isInstance(bean))
     
     - getTypeConverter().convertIfNecessary(bean, requiredType);//类型不对，则尝试转换bean类型
     
     - 返回创建出来的bean实例对象
















  



       
       




