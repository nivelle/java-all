## Spring 的 createBeanInstance()

- Class<?> beanClass = resolveBeanClass(mbd, beanName);//解析bean的类型信息

#### beanClass不为空 && beanClass不是公开类（不是public修饰） && 该bean不允许访问非公共构造函数和方法，则抛异常

- if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed());

#### 如果存在工厂方法则使用工厂方法实例化bean对象

- if (mbd.getFactoryMethodName() != null)

#### boolean resolved = false;(构造函数或工厂方法是否已经解析过)

- resolved = true;//如果resolvedConstructorOrFactoryMethod缓存不为空，则将resolved标记为已解析

#### boolean autowireNecessary = false;(是否需要自动注入（即是否需要解析构造函数参数)）

- autowireNecessary = mbd.constructorArgumentsResolved;

#### 如果已经解析过，则使用resolvedConstructorOrFactoryMethod缓存里解析好的构造函数方法

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


  
 
#### 否则使用默认的构造函数进行bean的实例化

- instantiateBean(beanName, mbd);

  //应用后置处理器SmartInstantiationAwareBeanPostProcessor，拿到bean的候选构造函数
- Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);

  - Constructor<?>[] ctors = ibp.determineCandidateConstructors(beanClass, beanName);//使用@Autowire注解修饰构造函数，则该构造函数在这边会被AutowiredAnnotationBeanPostProcessor找到

#### 如果ctors不为空 || mbd的注入方式为AUTOWIRE_CONSTRUCTOR || mdb定义了构造函数的参数值 || args不为空，则执行构造函数自动注入

- return autowireConstructor(beanName, mbd, ctors, args);

- instantiateBean(beanName, mbd);//没有特殊处理，则使用默认的构造函数进行bean的实例化

        