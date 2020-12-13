### Spring 的 initializeBean()

#### 属性注入(populateBean()方法)完成后，接下来就会执行init-method方法

- 激活 bean 实现的 Aware 类：BeanNameAware, BeanClassLoaderAware, BeanFactoryAware

- 应用 BeanPostProcessor 的 postProcessBeforeInitialization

- 激活用户自定义的 init-method 方法，以及常用的 afterPropertiesSet 方法

- 应用 BeanPostProcessor 的 postProcessAfterInitialization
````        
protected void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw) {
    // 获取bean实例的属性值集合
    PropertyValues pvs = mbd.getPropertyValues();
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
    // 给InstantiationAwareBeanPostProcessors最后一次机会在注入属性前改变bean实例
    if (!mbd.isSynthetic() && this.hasInstantiationAwareBeanPostProcessors()) {
        for (BeanPostProcessor bp : this.getBeanPostProcessors()) {
            if (bp instanceof InstantiationAwareBeanPostProcessor) {
                InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                // 是否继续填充bean
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
 
    boolean hasInstAwareBpps = this.hasInstantiationAwareBeanPostProcessors();
    boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);
    if (hasInstAwareBpps || needsDepCheck) {
        PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
        // 在属性注入前应用实例化后置处理器
        if (hasInstAwareBpps) {
            for (BeanPostProcessor bp : this.getBeanPostProcessors()) {
                if (bp instanceof InstantiationAwareBeanPostProcessor) {
                    InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                    // 调用后置处理器的postProcessPropertyValues方法
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
 
    // 执行属性注入
    this.applyPropertyValues(beanName, mbd, bw, pvs);
}

````

### 