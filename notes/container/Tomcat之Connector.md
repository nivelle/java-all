### Connector

#### connector 支持的 协议/模式

- AjpAprProtocol

- AjpNio2Protocol

- AjpNioProtocol

- Http11AprProtocol

- Http11Nio2Protocol

- Http11NioProtocol

#### Connector 构造函数

```
public Connector(String protocol) {
        boolean aprConnector = AprLifecycleListener.isAprAvailable() && AprLifecycleListener.getUseAprConnector();
        if ("HTTP/1.1".equals(protocol) || protocol == null) {
            if (aprConnector) {
                protocolHandlerClassName = "org.apache.coyote.http11.Http11AprProtocol";
            } else {
                protocolHandlerClassName = "org.apache.coyote.http11.Http11NioProtocol";
            }
        } else if ("AJP/1.3".equals(protocol)) {
            if (aprConnector) {
                protocolHandlerClassName = "org.apache.coyote.ajp.AjpAprProtocol";
            } else {
                protocolHandlerClassName = "org.apache.coyote.ajp.AjpNioProtocol";
            }
        } else {
            protocolHandlerClassName = protocol;
        }
        // Instantiate protocol handler
        ProtocolHandler p = null;
        try {
            Class<?> clazz = Class.forName(protocolHandlerClassName);
            p = (ProtocolHandler) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            log.error(sm.getString(
                    "coyoteConnector.protocolHandlerInstantiationFailed"), e);
        } finally {
            this.protocolHandler = p;
        }

        // Default for Connector depends on this system property
        setThrowOnFailure(Boolean.getBoolean("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE"));

        // Default for Connector depends on this (deprecated) system property
        if (Boolean.parseBoolean(System.getProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "false"))) {
            encodedSolidusHandling = EncodedSolidusHandling.DECODE;
        }
    }
```

#### createRequest

```
 /**
     * Create (or allocate) and return a Request object suitable for
     * specifying the contents of a Request to the responsible Container.
     *
     * @return a new Servlet request object
     */
    public Request createRequest() {
        return new Request(this);
    }

```

#### createResponse

```
    /**
     * Create (or allocate) and return a Response object suitable for
     * receiving the contents of a Response from the responsible Container.
     *
     * @return a new Servlet response object
     */
    public Response createResponse() {
        if (protocolHandler instanceof AbstractAjpProtocol<?>) {
            int packetSize = ((AbstractAjpProtocol<?>) protocolHandler).getPacketSize();
            return new Response(packetSize - org.apache.coyote.ajp.Constants.SEND_HEAD_LEN);
        } else {
            return new Response();
        }
    }
    
  ```
  
#### initInternal 初始化函数

```
protected void initInternal() throws LifecycleException {

        super.initInternal();

        if (protocolHandler == null) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerInstantiationFailed"));
        }
        // 第一步: 初始化 CoyoteAdapter,并且将其设置为ProtocolHandler的Adapter(CoyoteAdapter负责连接 connecoter和 Container)
        adapter = new CoyoteAdapter(this);
        // 将 CoyoteAdapter 设置到 ProtocolHandler
        protocolHandler.setAdapter(adapter);
        if (service != null) {
            protocolHandler.setUtilityExecutor(service.getServer().getUtilityExecutor());
        }
        // Make sure parseBodyMethodsSet has a default
        if (null == parseBodyMethodsSet) {
            setParseBodyMethods(getParseBodyMethods());
        }
        if (protocolHandler.isAprRequired() && !AprLifecycleListener.isInstanceCreated()) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerNoAprListener",getProtocolHandlerClassName()));
        }
        if (protocolHandler.isAprRequired() && !AprLifecycleListener.isAprAvailable()) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerNoAprLibrary",getProtocolHandlerClassName()));
        }
        if (AprLifecycleListener.isAprAvailable() && AprLifecycleListener.getUseOpenSSL() && protocolHandler instanceof AbstractHttp11JsseProtocol) {
            AbstractHttp11JsseProtocol<?> jsseProtocolHandler =(AbstractHttp11JsseProtocol<?>) protocolHandler;
            if (jsseProtocolHandler.isSSLEnabled() && jsseProtocolHandler.getSslImplementationName() == null) {
                // OpenSSL is compatible with the JSSE configuration, so use it if APR is available
                jsseProtocolHandler.setSslImplementationName(OpenSSLImplementation.class.getName());
            }
        }
        try {
            // 第二步: ProtocolHandler 调用其父类 AbstractProtocol 的init方法
            protocolHandler.init();
        } catch (Exception e) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerInitializationFailed"), e);
        }
    }

```
#### startInternal 开始化函数

```

 protected void startInternal() throws LifecycleException {
        // Validate settings before starting
        if (getPortWithOffset() < 0) {
            throw new LifecycleException(sm.getString("coyoteConnector.invalidPort", Integer.valueOf(getPortWithOffset())));
        }
        setState(LifecycleState.STARTING);
        try {
            //启动端口监听,最终调用的是 endPoint的startInternal; startInternal方法,初始化了处理连接请求的线程池(默认最大线程数200个)开启Acceptor线程接收请求
            //子类实现: AbstractProtocol
            protocolHandler.start();
        } catch (Exception e) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerStartFailed"), e);
        }
    }
```


### 子类: public abstract class AbstractProtocol<S> implements ProtocolHandler,MBeanRegistration

- init()

```
public void init() throws Exception {
        if (getLog().isInfoEnabled()) {
            getLog().info(sm.getString("abstractProtocolHandler.init", getName()));
            logPortOffset();
        }
        if (oname == null) {
            // Component not pre-registered so register it
            oname = createObjectName();
            if (oname != null) {
                Registry.getRegistry(null, null).registerComponent(this, oname, null);
            }
        }
        if (this.domain != null) {
            rgOname = new ObjectName(domain + ":type=GlobalRequestProcessor,name=" + getName());
            Registry.getRegistry(null, null).registerComponent(getHandler().getGlobal(), rgOname, null);
        }
        String endpointName = getName();
        endpoint.setName(endpointName.substring(1, endpointName.length()-1));
        //对endpoint（Http11Protocol使用的是JIoEndPoint）进行了初始化
        endpoint.setDomain(domain);
        // 对需要监听的端口进行了绑定, 在AbstractEndpoint中，完成了对需要监听的端口的绑定
        endpoint.init();
    }

```

- start()

```
 public void start() throws Exception {
        if (getLog().isInfoEnabled()) {
            getLog().info(sm.getString("abstractProtocolHandler.start", getName()));
            logPortOffset();
        }
        // 子类实现: AbstractEndpoint
        endpoint.start();
        monitorFuture = getUtilityExecutor().scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!isPaused()) {
                            startAsyncTimeout();
                        }
                    }
                }, 0, 60, TimeUnit.SECONDS);
    }

```

### public abstract class AbstractEndpoint<S,U> 

- init()方法

```
public final void init() throws Exception {
        if (bindOnInit) {
            bindWithCleanup();
            bindState = BindState.BOUND_ON_INIT;
        }
        if (this.domain != null) {
            // Register endpoint (as ThreadPool - historical name)
            oname = new ObjectName(domain + ":type=ThreadPool,name=\"" + getName() + "\"");
            Registry.getRegistry(null, null).registerComponent(this, oname, null);

            ObjectName socketPropertiesOname = new ObjectName(domain +
                    ":type=ThreadPool,name=\"" + getName() + "\",subType=SocketProperties");
            socketProperties.setObjectName(socketPropertiesOname);
            Registry.getRegistry(null, null).registerComponent(socketProperties, socketPropertiesOname, null);

            for (SSLHostConfig sslHostConfig : findSslHostConfigs()) {
                registerJmx(sslHostConfig);
            }
        }
    }
```

- start()方法

### 子类实现

1) AprEndpoint

2) Nio2Endpoint

3) NioEndpoint


```
public final void start() throws Exception {
        if (bindState == BindState.UNBOUND) {
            bindWithCleanup();
            bindState = BindState.BOUND_ON_START;
        }
        startInternal();
    }
```