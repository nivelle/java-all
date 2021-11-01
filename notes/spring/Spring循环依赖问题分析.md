老生常谈，循环依赖！顾名思义嘛，就是你依赖我，我依赖你，然后就造成了循环依赖了！由于A中注入B，B中注入A导致的吗？

看起来没毛病，然而，却没有说清楚问题！甚至会让你觉得你是不清楚spring的循环依赖的！

那么，spring的循环依赖到底是啥玩意？

来看个循环依赖注入失败的例子：

bean1: userService

复制代码
@Service
public class LoginServiceImpl implements LoginService {

//    @Resource
private UserService userService;

    public LoginServiceImpl() {
        // 无参构造器是给默认创建的bean使用的
    }

    @Autowired
    public LoginServiceImpl(UserService userService) {
        this.userService = userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public Integer login(UserInfo userInfo) {
        System.out.println("login...");
        return 0;
    }

    public void logAction(String userId, String action) throws Exception {
        UserInfo userInfo = userService.getUser(userId);
        System.out.println("logged..." + userInfo + ",action: " + action);
    }
}
复制代码
bean2: loginService

复制代码
@Service
public class UserServiceImpl implements UserService {

//    @Resource
private LoginService loginService;

    public UserServiceImpl() {
        // 无参构造器是给默认创建的bean使用的
    }
//    @Autowired
public UserServiceImpl(LoginService loginService) {
this.loginService = loginService;
}
@Autowired
public void setLoginService(LoginService loginService) {
this.loginService = loginService;
}

    public Integer addUser(UserInfo userInfo) throws Exception {
        userInfo.setId("111");
        System.out.println("user added. " + userInfo);
        // 登录
        loginService.login(userInfo);
        return 1;
    }

    public UserInfo getUser(String id) throws Exception {
        UserInfo userInfo = null;
        if("111".equals(id)) {
            userInfo = new UserInfo();
            userInfo.setId(id);
            userInfo.setAge(22);
            userInfo.setName("mocking");
            return userInfo;
        }
        throw new Exception("err:" + ErrorCodeEnum.API_CALL_ERROR.getCode() + "," + ErrorCodeEnum.API_CALL_ERROR.getErrMsg());
    }
}
复制代码
然后再建一个测试类，来测试以上代码！

复制代码
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUserForLoopDI() throws Exception {
        UserInfo userInfo = new UserInfo();
        userInfo.setId("12");
        userInfo.setAddress("sdfds");
        userService.addUser(userInfo);
        System.out.println("ok");
    }
}
复制代码
applicationContext.xml 中只要开启扫描即可！

    <context:annotation-config/>
    <context:component-scan base-package="com.xx"></context:component-scan>


如上例子运行，就可以得到一个循环依赖的失败错误了！

不过，你不一定能运行进来，因为如例子还要依赖于一个事实，那就 loginService 要在 userService 之前被扫描到，而不同的jvm上，可能spring得到的扫描顺序不一致，如果想要100%失败，则换成两个类都是构造器注入就可以了！

错误堆栈样例如下：（Is there an unresolvable circular reference?）


复制代码
java.lang.IllegalStateException: Failed to load ApplicationContext
at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:124)
at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:83)
at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:117)
at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:83)
at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:230)
at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.createTest(SpringJUnit4ClassRunner.java:228)
at org.springframework.test.context.junit4.SpringJUnit4ClassRunner$1.runReflectiveCall(SpringJUnit4ClassRunner.java:287)
at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.methodBlock(SpringJUnit4ClassRunner.java:289)
at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.runChild(SpringJUnit4ClassRunner.java:247)
at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.runChild(SpringJUnit4ClassRunner.java:94)
at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
at org.springframework.test.context.junit4.statements.RunBeforeTestClassCallbacks.evaluate(RunBeforeTestClassCallbacks.java:61)
at org.springframework.test.context.junit4.statements.RunAfterTestClassCallbacks.evaluate(RunAfterTestClassCallbacks.java:70)
at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.run(SpringJUnit4ClassRunner.java:191)
at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'loginService' defined in class path resource [applicationContext.xml]: Cannot resolve reference to bean 'userService' while setting constructor argument; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'userService' defined in class path resource [applicationContext.xml]: Cannot resolve reference to bean 'loginService' while setting bean property 'loginService'; nested exception is org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'loginService': Requested bean is currently in creation: Is there an unresolvable circular reference?
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveReference(BeanDefinitionValueResolver.java:359)
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveValueIfNecessary(BeanDefinitionValueResolver.java:108)
at org.springframework.beans.factory.support.ConstructorResolver.resolveConstructorArguments(ConstructorResolver.java:648)
at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:145)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1198)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1100)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:511)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:481)
at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:312)
at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:230)
at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:308)
at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:197)
at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:761)
at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:867)
at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:543)
at org.springframework.test.context.support.AbstractGenericContextLoader.loadContext(AbstractGenericContextLoader.java:128)
at org.springframework.test.context.support.AbstractGenericContextLoader.loadContext(AbstractGenericContextLoader.java:60)
at org.springframework.test.context.support.AbstractDelegatingSmartContextLoader.delegateLoading(AbstractDelegatingSmartContextLoader.java:281)
at org.springframework.test.context.support.AbstractDelegatingSmartContextLoader.loadContext(AbstractDelegatingSmartContextLoader.java:249)
at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:98)
at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:116)
... 24 more
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'userService' defined in class path resource [applicationContext.xml]: Cannot resolve reference to bean 'loginService' while setting bean property 'loginService'; nested exception is org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'loginService': Requested bean is currently in creation: Is there an unresolvable circular reference?
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveReference(BeanDefinitionValueResolver.java:359)
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveValueIfNecessary(BeanDefinitionValueResolver.java:108)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.applyPropertyValues(AbstractAutowireCapableBeanFactory.java:1534)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.populateBean(AbstractAutowireCapableBeanFactory.java:1281)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:551)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:481)
at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:312)
at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:230)
at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:308)
at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:197)
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveReference(BeanDefinitionValueResolver.java:351)
... 44 more
Caused by: org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'loginService': Requested bean is currently in creation: Is there an unresolvable circular reference?
at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.beforeSingletonCreation(DefaultSingletonBeanRegistry.java:347)
at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:223)
at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:308)
at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:197)
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveReference(BeanDefinitionValueResolver.java:351)
... 54 more

java.lang.IllegalStateException: Failed to load ApplicationContext

    at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:124)
    at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:83)
    at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.injectDependencies(DependencyInjectionTestExecutionListener.java:117)
    at org.springframework.test.context.support.DependencyInjectionTestExecutionListener.prepareTestInstance(DependencyInjectionTestExecutionListener.java:83)
    at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:230)
    at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.createTest(SpringJUnit4ClassRunner.java:228)
    at org.springframework.test.context.junit4.SpringJUnit4ClassRunner$1.runReflectiveCall(SpringJUnit4ClassRunner.java:287)
    at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
    at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.methodBlock(SpringJUnit4ClassRunner.java:289)
    at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.runChild(SpringJUnit4ClassRunner.java:247)
    at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.runChild(SpringJUnit4ClassRunner.java:94)
    at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
    at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
    at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
    at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
    at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
    at org.springframework.test.context.junit4.statements.RunBeforeTestClassCallbacks.evaluate(RunBeforeTestClassCallbacks.java:61)
    at org.springframework.test.context.junit4.statements.RunAfterTestClassCallbacks.evaluate(RunAfterTestClassCallbacks.java:70)
    at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
    at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.run(SpringJUnit4ClassRunner.java:191)
    at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
    at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
    at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
    at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
    at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'loginService' defined in class path resource [applicationContext.xml]: Cannot resolve reference to bean 'userService' while setting constructor argument; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'userService' defined in class path resource [applicationContext.xml]: Cannot resolve reference to bean 'loginService' while setting bean property 'loginService'; nested exception is org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'loginService': Requested bean is currently in creation: Is there an unresolvable circular reference?
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveReference(BeanDefinitionValueResolver.java:359)
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveValueIfNecessary(BeanDefinitionValueResolver.java:108)
at org.springframework.beans.factory.support.ConstructorResolver.resolveConstructorArguments(ConstructorResolver.java:648)
at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:145)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1198)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1100)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:511)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:481)
at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:312)
at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:230)
at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:308)
at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:197)
at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:761)
at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:867)
at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:543)
at org.springframework.test.context.support.AbstractGenericContextLoader.loadContext(AbstractGenericContextLoader.java:128)
at org.springframework.test.context.support.AbstractGenericContextLoader.loadContext(AbstractGenericContextLoader.java:60)
at org.springframework.test.context.support.AbstractDelegatingSmartContextLoader.delegateLoading(AbstractDelegatingSmartContextLoader.java:281)
at org.springframework.test.context.support.AbstractDelegatingSmartContextLoader.loadContext(AbstractDelegatingSmartContextLoader.java:249)
at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:98)
at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:116)
... 24 more
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'userService' defined in class path resource [applicationContext.xml]: Cannot resolve reference to bean 'loginService' while setting bean property 'loginService'; nested exception is org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'loginService': Requested bean is currently in creation: Is there an unresolvable circular reference?
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveReference(BeanDefinitionValueResolver.java:359)
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveValueIfNecessary(BeanDefinitionValueResolver.java:108)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.applyPropertyValues(AbstractAutowireCapableBeanFactory.java:1534)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.populateBean(AbstractAutowireCapableBeanFactory.java:1281)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:551)
at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:481)
at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:312)
at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:230)
at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:308)
at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:197)
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveReference(BeanDefinitionValueResolver.java:351)
... 44 more
Caused by: org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'loginService': Requested bean is currently in creation: Is there an unresolvable circular reference?
at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.beforeSingletonCreation(DefaultSingletonBeanRegistry.java:347)
at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:223)
at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:308)
at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:197)
at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveReference(BeanDefinitionValueResolver.java:351)
... 54 more
复制代码
好了，看完效果，是时候来分析下问题了。

1. 为什么会出现问题？

因为加载流程如下：

1. 首先加载 loginService, 然后构造器被调用，然后发现依赖了 userService;
2. 依赖注入，先去加载 userService，加载userService后，需要对其属性进行依赖注入，然后发现了 loginService 需要被注入；
3. 当去加载 loginService 的时候，发现 loginService 正在创建中，所以这个实例只能认为创建失败了，否则将会导致更多未知问题；
4. 同理，多个构造器互相注入失败问题更严重；

2. 循环依赖失败是不是只要 ABA 就一定会导致失败？（注： 非单例对象一定不会导致循环依赖）

1. 按正常说是这样的，但是spring已经解决这个问题了；
2. spring 解决方案为，只为单例提供解决方案也只能为单例解决问题；
3. 创建A单例时，放入缓存，然后依赖注入B；
4. 依赖注入B时，发现需要依赖注入A，然后去加载A，此时从缓存中发现A正在加载中，于是直接从缓存得到A，完成自身的依赖注入；
5. B依赖完成后，返回给A，A再把B注入到自身域中；
6. B中的A也自然而然的完成了初始化动作；

其中，构造器注入的单例的循环依赖是无法解决的，因为在构造器注入时，本身的实例无法生成；如果强行使用，将导致不安全的发布，从而导致各种未知的问题！
3. 源码如何实现？
   1. 首先，我们来看下什么情况下会抛出循环依赖异常？

复制代码
// 只有单例的创建会存在循环依赖问题
// org.springframework.beans.factory.support.DefaultSingletonBeanRegistry

    /** Set of registered singletons, containing the bean names in registration order */
    private final Set<String> registeredSingletons = new LinkedHashSet<String>(256);

    /** Names of beans that are currently in creation */
    private final Set<String> singletonsCurrentlyInCreation =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(16));

    /** Names of beans currently excluded from in creation checks */
    private final Set<String> inCreationCheckExclusions =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(16));

    protected void beforeSingletonCreation(String beanName) {
        if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.add(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName);
        }
    }

复制代码
从上面的代码可以看出，抛出循环依赖问题会抛出有两种前提:

1. 没有包含在创建检查的排除列表中；

2. 没有被排除，则检查是不是第一次被调用创建，如果单例不是第一次被调用创建，则不能再创建了（否则就不是单例了）；

所以，避免循环依赖有个出口，那就是提前把单例放到检查排除列表中！

那么什么时候会被加入到创建检查排除列表？

复制代码
public void setCurrentlyInCreation(String beanName, boolean inCreation) {
Assert.notNull(beanName, "Bean name must not be null");
if (!inCreation) {
this.inCreationCheckExclusions.add(beanName);
}
else {
this.inCreationCheckExclusions.remove(beanName);
}
}

复制代码
具体如何设置呢？其实普通的使用不会用到这个功能，只会在一些增强点作这些工作。



所以，spring 如何避免循环依赖失败？



复制代码
// org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean()

    /**
     * Actually create the specified bean. Pre-creation processing has already happened
     * at this point, e.g. checking {@code postProcessBeforeInstantiation} callbacks.
     * <p>Differentiates between default bean instantiation, use of a
     * factory method, and autowiring a constructor.
     * @param beanName the name of the bean
     * @param mbd the merged bean definition for the bean
     * @param args explicit arguments to use for constructor or factory method invocation
     * @return a new instance of the bean
     * @throws BeanCreationException if the bean could not be created
     * @see #instantiateBean
     * @see #instantiateUsingFactoryMethod
     * @see #autowireConstructor
     */
    protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args)
            throws BeanCreationException {

        // 第一步，创建bean实例
        // Instantiate the bean.
        BeanWrapper instanceWrapper = null;
        if (mbd.isSingleton()) {
            instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
        }
        if (instanceWrapper == null) {
            instanceWrapper = createBeanInstance(beanName, mbd, args);
        }
        final Object bean = (instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null);
        Class<?> beanType = (instanceWrapper != null ? instanceWrapper.getWrappedClass() : null);
        mbd.resolvedTargetType = beanType;

        // Allow post-processors to modify the merged bean definition.
        synchronized (mbd.postProcessingLock) {
            if (!mbd.postProcessed) {
                try {
                    applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
                }
                catch (Throwable ex) {
                    throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                            "Post-processing of merged bean definition failed", ex);
                }
                mbd.postProcessed = true;
            }
        }

        // 对于提前暴露的bean, 将它加入单例缓存中，从而使后续的依赖调用时，可以将此实例返回
        // Eagerly cache singletons to be able to resolve circular references
        // even when triggered by lifecycle interfaces like BeanFactoryAware.
        boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
                isSingletonCurrentlyInCreation(beanName));
        if (earlySingletonExposure) {
            if (logger.isDebugEnabled()) {
                logger.debug("Eagerly caching bean '" + beanName +
                        "' to allow for resolving potential circular references");
            }
            // 针对提前暴露的 singleton, 添加一个 获取实例 工厂，以备后续获取实例时使用
            addSingletonFactory(beanName, new ObjectFactory<Object>() {
                @Override
                public Object getObject() throws BeansException {
                    return getEarlyBeanReference(beanName, mbd, bean);
                }
            });
        }

        // 第二步，初始化实例，完成依赖注入等工作
        // Initialize the bean instance.
        Object exposedObject = bean;
        try {
            populateBean(beanName, mbd, instanceWrapper);
            if (exposedObject != null) {
                exposedObject = initializeBean(beanName, exposedObject, mbd);
            }
        }
        catch (Throwable ex) {
            if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
                throw (BeanCreationException) ex;
            }
            else {
                throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
            }
        }

        if (earlySingletonExposure) {
            Object earlySingletonReference = getSingleton(beanName, false);
            if (earlySingletonReference != null) {
                if (exposedObject == bean) {
                    exposedObject = earlySingletonReference;
                }
                else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
                    String[] dependentBeans = getDependentBeans(beanName);
                    Set<String> actualDependentBeans = new LinkedHashSet<String>(dependentBeans.length);
                    for (String dependentBean : dependentBeans) {
                        if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                            actualDependentBeans.add(dependentBean);
                        }
                    }
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

        // 第三步，对外暴露bean，如 singleton 入 容器中注册实例
        // Register bean as disposable.
        try {
            registerDisposableBeanIfNecessary(beanName, bean, mbd);
        }
        catch (BeanDefinitionValidationException ex) {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
        }

        return exposedObject;
    }
复制代码


而在获取 singleton 时，会先尝试从工厂中获取！

复制代码
// org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
Object singletonObject = this.singletonObjects.get(beanName);
// 针对正在创建中的 singleton, 将其放入 earlySingletonObjects
if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
synchronized (this.singletonObjects) {
singletonObject = this.earlySingletonObjects.get(beanName);
if (singletonObject == null && allowEarlyReference) {
ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
if (singletonFactory != null) {
// 将 singleton 实例从工厂中取出后，存入 提前暴露的缓存中，交将创建工厂删除，避免后续反复从中获取 singleton
singletonObject = singletonFactory.getObject();
this.earlySingletonObjects.put(beanName, singletonObject);
this.singletonFactories.remove(beanName);
}
}
}
}
return (singletonObject != NULL_OBJECT ? singletonObject : null);
}

    // org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.getEarlyBeanReference()
    protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
        Object exposedObject = bean;
        if (bean != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            for (BeanPostProcessor bp : getBeanPostProcessors()) {
                if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                    SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
                    exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
                    if (exposedObject == null) {
                        return null;
                    }
                }
            }
        }
        return exposedObject;
    }
复制代码


在获取bean实例时，会先尝试从缓存中直接获取，失败再进行真实的创建：

复制代码
// org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean()
// 会先尝试从单例缓存中获取bean，获取不到再进行创建
/**
* Return an instance, which may be shared or independent, of the specified bean.
* @param name the name of the bean to retrieve
* @param requiredType the required type of the bean to retrieve
* @param args arguments to use when creating a bean instance using explicit arguments
* (only applied when creating a new instance as opposed to retrieving an existing one)
* @param typeCheckOnly whether the instance is obtained for a type check,
* not for actual use
* @return an instance of the bean
* @throws BeansException if the bean could not be created
*/
@SuppressWarnings("unchecked")
protected <T> T doGetBean(
final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly)
throws BeansException {

        final String beanName = transformedBeanName(name);
        Object bean;

        // 先从缓存获取，失败则进行创建
        // Eagerly check singleton cache for manually registered singletons.
        Object sharedInstance = getSingleton(beanName);
        if (sharedInstance != null && args == null) {
            if (logger.isDebugEnabled()) {
                if (isSingletonCurrentlyInCreation(beanName)) {
                    logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
                            "' that is not fully initialized yet - a consequence of a circular reference");
                }
                else {
                    logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
                }
            }
            bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
        }

        else {
            // Fail if we're already creating this bean instance:
            // We're assumably within a circular reference.
            if (isPrototypeCurrentlyInCreation(beanName)) {
                throw new BeanCurrentlyInCreationException(beanName);
            }

            // Check if bean definition exists in this factory.
            BeanFactory parentBeanFactory = getParentBeanFactory();
            if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
                // Not found -> check parent.
                String nameToLookup = originalBeanName(name);
                if (args != null) {
                    // Delegation to parent with explicit args.
                    return (T) parentBeanFactory.getBean(nameToLookup, args);
                }
                else {
                    // No args -> delegate to standard getBean method.
                    return parentBeanFactory.getBean(nameToLookup, requiredType);
                }
            }

            if (!typeCheckOnly) {
                markBeanAsCreated(beanName);
            }

            try {
                final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                checkMergedBeanDefinition(mbd, beanName, args);

                // Guarantee initialization of beans that the current bean depends on.
                String[] dependsOn = mbd.getDependsOn();
                if (dependsOn != null) {
                    for (String dep : dependsOn) {
                        if (isDependent(beanName, dep)) {
                            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                    "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
                        }
                        registerDependentBean(dep, beanName);
                        getBean(dep);
                    }
                }

                // Create bean instance.
                if (mbd.isSingleton()) {
                    // 此时根据对象工厂，创建bean，创建时检测依赖情况
                    sharedInstance = getSingleton(beanName, new ObjectFactory<Object>() {
                        @Override
                        public Object getObject() throws BeansException {
                            try {
                                return createBean(beanName, mbd, args);
                            }
                            catch (BeansException ex) {
                                // Explicitly remove instance from singleton cache: It might have been put there
                                // eagerly by the creation process, to allow for circular reference resolution.
                                // Also remove any beans that received a temporary reference to the bean.
                                destroySingleton(beanName);
                                throw ex;
                            }
                        }
                    });
                    bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
                }

                else if (mbd.isPrototype()) {
                    // It's a prototype -> create a new instance.
                    Object prototypeInstance = null;
                    try {
                        beforePrototypeCreation(beanName);
                        prototypeInstance = createBean(beanName, mbd, args);
                    }
                    finally {
                        afterPrototypeCreation(beanName);
                    }
                    bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
                }

                else {
                    String scopeName = mbd.getScope();
                    final Scope scope = this.scopes.get(scopeName);
                    if (scope == null) {
                        throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
                    }
                    try {
                        Object scopedInstance = scope.get(beanName, new ObjectFactory<Object>() {
                            @Override
                            public Object getObject() throws BeansException {
                                beforePrototypeCreation(beanName);
                                try {
                                    return createBean(beanName, mbd, args);
                                }
                                finally {
                                    afterPrototypeCreation(beanName);
                                }
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

        // Check if required type matches the type of the actual bean instance.
        if (requiredType != null && bean != null && !requiredType.isAssignableFrom(bean.getClass())) {
            try {
                return getTypeConverter().convertIfNecessary(bean, requiredType);
            }
            catch (TypeMismatchException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to convert bean '" + name + "' to required type '" +
                            ClassUtils.getQualifiedName(requiredType) + "'", ex);
                }
                throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
            }
        }
        return (T) bean;
    }

    // org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton()
    // 进行 singleton 创建时，做安全检查，成功后放入缓存中
    /**
     * Return the (raw) singleton object registered under the given name,
     * creating and registering a new one if none registered yet.
     * @param beanName the name of the bean
     * @param singletonFactory the ObjectFactory to lazily create the singleton
     * with, if necessary
     * @return the registered singleton object
     */
    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "'beanName' must not be null");
        synchronized (this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                if (this.singletonsCurrentlyInDestruction) {
                    throw new BeanCreationNotAllowedException(beanName,
                            "Singleton bean creation not allowed while singletons of this factory are in destruction " +
                            "(Do not request a bean from a BeanFactory in a destroy method implementation!)");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
                }
                // 创建前检查，循环依赖失败在此时检测到，即单例只能被创建一次
                beforeSingletonCreation(beanName);
                boolean newSingleton = false;
                boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
                if (recordSuppressedExceptions) {
                    this.suppressedExceptions = new LinkedHashSet<Exception>();
                }
                try {
                    // createBean()
                    singletonObject = singletonFactory.getObject();
                    newSingleton = true;
                }
                catch (IllegalStateException ex) {
                    // Has the singleton object implicitly appeared in the meantime ->
                    // if yes, proceed with it since the exception indicates that state.
                    singletonObject = this.singletonObjects.get(beanName);
                    if (singletonObject == null) {
                        throw ex;
                    }
                }
                catch (BeanCreationException ex) {
                    if (recordSuppressedExceptions) {
                        for (Exception suppressedException : this.suppressedExceptions) {
                            ex.addRelatedCause(suppressedException);
                        }
                    }
                    throw ex;
                }
                finally {
                    if (recordSuppressedExceptions) {
                        this.suppressedExceptions = null;
                    }
                    // 最后，清除正在创建的标识
                    afterSingletonCreation(beanName);
                }
                // 单例创建成功，则将其加入到缓存中，并清理之前的标记
                if (newSingleton) {
                    addSingleton(beanName, singletonObject);
                }
            }
            return (singletonObject != NULL_OBJECT ? singletonObject : null);
        }
    }
    
    /**
     * Add the given singleton object to the singleton cache of this factory.
     * <p>To be called for eager registration of singletons.
     * @param beanName the name of the bean
     * @param singletonObject the singleton object
     */
    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, (singletonObject != null ? singletonObject : NULL_OBJECT));
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }
复制代码


如上代码，说明了bean的创建过程，我们主要看 singleton。

1. 尝试从缓存中获取bean，可能是提前暴露的，也可能是已经完全初始化好的；（提前暴露解决循环依赖）

2. 如果获取不到，则进行一次完整的初始化，通过 对象工厂的 getObject() 进行对象创建；

3. 创建前，先将正在创建中标识写入，从而防止后续又有进行创建动作；（抛出创建异常）

4. 创建好 singleton 后，将多余标识删除，并将 singleton 放入缓存，以待下次直接使用；



最后，我们用一个示意图描述下整个过程：





思考题：

      依赖注入使代码更简单和灵活，可以自动查找关联关系！当发现有依赖时，相当于递归生成里层bean实例！而每个bean的创建都经过n层方法的调用；

      那么请问，在创建bean时有没有可能导致栈溢出？？