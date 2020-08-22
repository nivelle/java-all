### Tomcat生命周期

![生命周期结构图](https://s1.ax1x.com/2020/07/16/UrDI1g.jpg)


- Lifecycle: 定义了容器生命周期、容器状态转换及容器状态迁移事件的监听器注册和移除等主要接口

- LifecycleBase: 作为Lifecycle接口的抽象实现类，运用抽象模板模式将所有容器的生命周期及状态转换衔接起来，此外还提供了生成LifecycleEvent事件的接口;

- LifecycleSupport: 提供有关LifecycleEvent事件的监听器注册、移除，并且使用经典的监听器模式，实现事件生成后触打监听器的实现;

- MBeanRegistration:Java jmx框架提供的注册MBean的接口，引入此接口是为了便于使用JMX提供的管理功能;

- LifecycleMBeanBase: Tomcat提供的对MBeanRegistration的抽象实现类，运用抽象模板模式将所有容器统一注册到JMX;

ContainerBase、StandardServer、StandardService、WebappLoader、Connector、StandardContext、StandardEngine、StandardHost、StandardWrapper等容器都继承了LifecycleMBeanBase，因此这些容器都具有了同样的生命周期并可以通过JMX进行管理


![容器初始化](https://s1.ax1x.com/2020/07/18/UgcQHO.jpg)


![Tomcat结构图](https://s1.ax1x.com/2020/07/18/Ug0DpV.jpg)


### 容器初始化

#### standardServer -> initInternal
```
public final synchronized void init() throws LifecycleException {
        if (!state.equals(LifecycleState.NEW)) {
            invalidTransition(Lifecycle.BEFORE_INIT_EVENT);
        }

        try {
            setStateInternal(LifecycleState.INITIALIZING, null, false);
            // 1. 容器本身真正的初始化:具体容器的initInternal方法调用父类LifecycleMBeanBase的initInternal方法实现，此initInternal方法用于将容器托管到JMX，便于运维管理
            // 2. 容器如果有子容器，会调用子容器的init方法
            // 3. standardServer->standardService[initInternal]
            initInternal();
            setStateInternal(LifecycleState.INITIALIZED, null, false);
        } catch (Throwable t) {
            handleSubClassException(t, "lifecycleBase.initFail", toString());
        }
    }

```

#### standardService

```
protected void initInternal() throws LifecycleException {

        super.initInternal();

        if (engine != null) {
            // 初始化引擎:standardEngine
            engine.init();
        }

        // 初始化线程池:executor
        for (Executor executor : findExecutors()) {
            if (executor instanceof JmxEnabled) {
                ((JmxEnabled) executor).setDomain(getDomain());
            }
            executor.init();
        }

        // StandardEngin 子容器容器 ->registerHost(host)->registerContext((Context) container)
        mapperListener.init();

        // 连接器初始化: Connector 
        synchronized (connectorsLock) {
            for (Connector connector : connectors) {
                //连接初始化
                connector.init();
            }
        }
    }


```

#### 注册项目：registerContext 

```
private void registerContext(Context context) {
        //the context path for this web application 获取应用路径
        String contextPath = context.getPath();
        if ("/".equals(contextPath)) {
            contextPath = "";
        //获取父容器虚拟机: host
        Host host = (Host)context.getParent();
        //获取资源根路径
        WebResourceRoot resources = context.getResources();
        //获取欢迎文件
        String[] welcomeFiles = context.findWelcomeFiles();
        List<WrapperMappingInfo> wrappers = new ArrayList<>();
        // 获取子容器也及各个项目（standardWrapper）
        for (Container container : context.findChildren()) {
            //wrappers list添加 容器的映射
            prepareWrapperMappingInfo(context, (Wrapper) container, wrappers);
            if(log.isDebugEnabled()) {
                log.debug(sm.getString("mapperListener.registerWrapper",
                        container.getName(), contextPath, service));
            }
        }

        mapper.addContextVersion(host.getName(), host, contextPath,
                context.getWebappVersion(), context, welcomeFiles, resources,
                wrappers);

        if(log.isDebugEnabled()) {
            log.debug(sm.getString("mapperListener.registerContext",
                    contextPath, service));
        }
    }


```

##### 项目映射：prepareWrapperMappingInfo(Context context ,Wrapper wrapper, List<WrapperMappingInfo> wrappers)

```
private void prepareWrapperMappingInfo(Context context, Wrapper wrapper,
            List<WrapperMappingInfo> wrappers) {
        String wrapperName = wrapper.getName();
        boolean resourceOnly = context.isResourceOnlyServlet(wrapperName);
        String[] mappings = wrapper.findMappings();
        for (String mapping : mappings) {
            boolean jspWildCard = (wrapperName.equals("jsp")
                                   && mapping.endsWith("/*"));
            wrappers.add(new WrapperMappingInfo(mapping, wrapper, jspWildCard,
                    resourceOnly));
        }
    }

```