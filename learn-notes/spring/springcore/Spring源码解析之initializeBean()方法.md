### Spring 的 initializeBean()

#### 属性注入(populateBean()方法)完成后，接下来就会执行init-method方法

- 激活 bean 实现的 Aware 类: BeanNameAware, BeanClassLoaderAware, BeanFactoryAware

- 应用 BeanPostProcessor 的 postProcessBeforeInitialization

- 激活用户自定义的 init-method 方法，以及常用的 afterPropertiesSet 方法

- 应用 BeanPostProcessor 的 postProcessAfterInitialization

````        
protected Object initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd) {
    // 1. 激活bean实现的Aware类：BeanNameAware, BeanClassLoaderAware, BeanFactoryAware
    if (System.getSecurityManager() != null) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                //(此处回调了AnnotationAwareAspectJAutoProxyCreator的setBeanFactory方法)
                invokeAwareMethods(beanName, bean);
                return null;
            }
        }, getAccessControlContext());
    } else {
        this.invokeAwareMethods(beanName, bean);
    }
 
    // 2. 应用 BeanPostProcessor 的 postProcessBeforeInitialization
    Object wrappedBean = bean;
    if (mbd == null || !mbd.isSynthetic()) {
        wrappedBean = this.applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
    }
 
    // 3. 激活用户自定义的 init-method 方法，以及常用的 afterPropertiesSet 方法
    try {
        this.invokeInitMethods(beanName, wrappedBean, mbd);
    } catch (Throwable ex) {
        throw new BeanCreationException((mbd != null ? mbd.getResourceDescription() : null), beanName, "Invocation of init method failed", ex);
    }
 
    // 4. 应用 BeanPostProcessor 的 postProcessAfterInitialization
    if (mbd == null || !mbd.isSynthetic()) {
        wrappedBean = this.applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
    }
    return wrappedBean;
}
````

### DisposableBean

在销毁对应的 bean 时能够回调实现的 destroy 方法，从而为销毁前的处理工作提供了入口，容器会利用一个 Map 集合来记录所有实现了 DisposableBean 接口的 bean

````
protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition mbd) {
    AccessControlContext acc = (System.getSecurityManager() != null ? getAccessControlContext() : null);
    if (!mbd.isPrototype() && this.requiresDestruction(bean, mbd)) {
        // 非原型,且需要执行销毁前的处理工作
        if (mbd.isSingleton()) {
            // 单例，注册bean到disposableBeans集合
            this.registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, mbd, this.getBeanPostProcessors(), acc));
        } else {
            // 自定义的scope
            Scope scope = this.scopes.get(mbd.getScope());
            if (scope == null) {
                throw new IllegalStateException("No Scope registered for scope name '" + mbd.getScope() + "'");
            }
            scope.registerDestructionCallback(beanName, new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
        }
    }
}

````