### Spring 的 initializeBean()

#### 属性注入(populateBean()方法)完成后，接下来就会执行init-method方法

- 激活 bean 实现的 Aware 类：BeanNameAware, BeanClassLoaderAware, BeanFactoryAware

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
                //调用Aware方法
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

### 