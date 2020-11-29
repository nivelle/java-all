### SpringBoot 之 Tomcat启动过程

##### 子类实现: ServletWebServerApplicationContext
- private void createWebServer()

  - WebServer webServer = this.webServer;
  
  - ServletContext servletContext = getServletContext();

###### if (webServer == null && servletContext == null)

  - ServletWebServerFactory factory = getWebServerFactory();//获取webServer的工厂类
  
    - String[] beanNames = getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class);
    
    - return getBeanFactory().getBean(beanNames[0], ServletWebServerFactory.class);
  
  - this.webServer = factory.getWebServer(getSelfInitializer());//创建webServer,参数:ServletContextInitializer... initializers 实现SCI的组件
  
    ####### TomcatServletWebServerFactory 子类实现
    
    ```
    1. tomcat有多种配置和启动方式，最常见的方式是基于server.xml配置的启动器 org.apache.catalina.startup.Bootstrap, 而这里的Tomcat是一个内嵌tomcat的启动器
    
    2. 默认创建一个Tomcat Server,创建并关联到这个 Tomcat Server上一个 Tomcat Service: (1 Tomcat Service = 1 Tomcat Engine + N Tomcat Connector) 每个Service只能包含一个Servlet引擎（Engine）, 表示一个特定Service的请求处理流水线,做为一个Service可以有多个连接器，引擎从连接器接收和处理所有的请求，将响应返回给适合的连接器，通过连接器传输给用户。
    
    3. 在所创建的那一个 Tomcat Engine 上创建了一个 Tomcat Host;每个 Virtual Host 虚拟主机和某个网络域名Domain Name相匹配,每个虚拟主机下都可以部署(deploy)一个或者多个Web App，每个Web App对应于一个Context，有一个Context path。当Host获得一个请求时，将把该请求匹配到某个Context上。 一个 Tomcat Engine 上面可以有多个 Tomcat Host。
    
    ```
    - Tomcat tomcat = new Tomcat();
    
    - File baseDir = (this.baseDirectory != null) ? this.baseDirectory: createTempDir("tomcat");//如果没有设置基础文件目录则创建临时目录
    
    - tomcat.setBaseDir(baseDir.getAbsolutePath());
    
    - Connector connector = new Connector(this.protocol);// 创建tomcat的Connector,缺省协议为org.apache.coyote.http11.Http11NioProtocol,表示处理 http v1.1 协议

    - tomcat.getService().addConnector(connector);//该方法真正创建 Tomcat 的 Server 对象实例，
      
      - return getServer().findServices()[0]; //Get the service object. Can be used to add more connectors and few other global settings.
      
        - server = new StandardServer();
        
        - initBaseDir();
        
        - Service service = new StandardService();
        
        - service.setName("Tomcat");
        
        - server.addService(service);
        
    - customizeConnector(connector);//根据配置参数定制 connector ：端口，uri encoding字符集，是否启用SSL, 是否使用压缩等
    
    - tomcat.setConnector(connector);
    
    - tomcat.getHost().setAutoDeploy(false);//关闭应用的自动部署

    - configureEngine(tomcat.getEngine());
    
    - configureEngine(tomcat.getEngine());
    
    - tomcat.getService().addConnector(additionalConnector);
    
    - prepareContext(tomcat.getHost(), initializers);//纯程序方式创建并准备Tomcat StandardContext，它对应一个web应用，把它绑定到host上。
    
      - File documentRoot = getValidDocumentRoot();//准备Host的docBase,

      - TomcatEmbeddedContext context = new TomcatEmbeddedContext();//创建StandardContext，这是Tomcat的标准概念，用来对应表示一个web应用，这里使用实现类TomcatEmbeddedContext，由 spring boot 提供。可以认为是往tomcat servlet容器中部署和启动一个web应用的过程，只不过在传统方式下，一个web应用部署到tomcat使用war包的方式，而这里是完全程序化的方式
      
      - addDefaultServlet(context);//缺省情况下，会注册 Tomcat 的 DefaultServlet,DefaultServlet是Tomcat缺省的资源服务Servlet,用来服务HTML,图片等静态资源
      
      - addJspServlet(context);//Spring boot 提供了一个工具类 org.springframework.boot.context.embedded.JspServlet检测类 org.apache.jasper.servlet.JspServlet 是否存在于 classpath 中，如果存在，则认为应该注册JSP Servlet。
        
         **缺省情况下,不注册(换句话讲,SpringBoot web应用缺省不支持JSP)注意 !!! 这一点和使用Tomcat充当外部容器的情况是不一样的,使用Tomcat作为外部容器的时候，JSP Servlet 缺省是被注册的。**
         
      - ServletContextInitializer[] initializersToUse = mergeInitializers(initializers);
        
        ```
        合并参数提供的Spring SCI : EmbeddedWebApplicationContext$1,这是一个匿名内部类，封装的逻辑来自方法 selfInitialize()和当前servlet容器在bean创建时通过EmbeddedServletContainerCustomizer
        
        ServerProperties添加进来的两个Spring SCI :ServerProperties$SessionConfiguringInitializer 和 InitParameterConfiguringServletContextInitializer注意这里的SCI接口由spring定义，tomcat jar中也包含了一个servlet API规范定义的SCI接口,这是定义相同的两个接口而非同一个,最终实现了Spring SCI接口的类的逻辑必须通过某种方式封装成实现了servlet API规范定义的SCI的逻辑才能被
        执行
        
        ```
      - configureContext(context, initializersToUse);
      
        ```
         配置context:
      
         1.将Spring提供的SCI封装成Servlet API标准SCI配置到context中去，通过一个实现了Servlet API标准SCI接口的spring类 TomcatStarter
      
         2.将spring领域的MIME映射配置设置到context中去
      
         3.将spring领域的session配置设置到context中去，比如 sessionTimeout
      
        ```

    - getTomcatWebServer(tomcat);
    
      - initialize()
      
        - addInstanceIdToEngineName();//往引擎名字中增加instance Id信息，如果 instance id为0，则不修改引擎名字
        
        - removeServiceConnectors();//在 service 启动后 protocal binding 尚未发生之前执行删除 service 中 connector 的逻辑。
        
        - this.tomcat.start();//在这里启动了DispatcherServlet [Spring DispatcherServlet](SpringBoot源码解析之dispatcherServlet.md)
        
          ```
           1. 触发启动Tomcat容器中除了Connector之外的其他部分，Connector此处没被启动意味着该启动过程完成后，服务器还是不能接受来自网络的请求,因为Connector才是真正负责接受网络请求的入口。
           
          
           2. 这里Tomcat启动的主要是StandardServer[1实例,Tomcat Lifecycle] =>StandardService[1实例,Tomcat Lifecycle] =>StandardEngine[1实例,Tomcat Container] =异步startStopExecutor =>StandardHost[1实例,Tomcat Container] =异步startStopExecutor =>TomcatEmbeddedContext[1实例,Springboot实现的Tomcat Container] =>StandardWrapper[1实例,Tomcat Container]。这里StandardWrapper对应的Servlet是Spring MVC的DispatchServlet。上面Tomcat Container父容器启动子容器都是通过线程池异步方式启动的。
          
          ```
          
        - startDaemonAwaitThread();//tomcat 自身所有的线程都是daemon线程。这里spring创建了一个非daemon线程用来阻塞整个应用，避免刚启动就马上结束的情况。


###### else if (servletContext != null)
  
  - getSelfInitializer().onStartup(servletContext);
  
    - private void selfInitialize(ServletContext servletContext) throws ServletException
    
      - prepareWebApplicationContext(servletContext);//1. 将当前spring application context作为属性设置到servletContext WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE 随后应用启动完在提供Web服务的时候,Spring MVC dispatchServlet的应用上下文的双亲就会使用这个根Web应用上下文 2. 将servletContext记录到当前web application context，也就是当前EmbeddedWebApplicationContext对象的属性servletContext中
      
      - registerApplicationScope(servletContext);//	注册标准webapp作用域				
   
      - WebApplicationContextUtils.registerEnvironmentBeans(getBeanFactory(),servletContext);//注册webapp相关环境参数bean : contextParameters,contextAttributes WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME，WebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME
      
      - for (ServletContextInitializer beans : getServletContextInitializerBeans());//到目前为止，尚未初始化webapp中的Servlet，Filter 和 EventListener从Bean容器中找到所有的SCI bean,并调用其 onStartup()回调方法getServletContextInitializerBeans()会从bean容器中找到所有的SCI bean将bean容器中所有Servlet, Filter 和EventListener bean转换成SCI 然后返回给该for循环然后逐一调用其 onStartup() 回调
        
         ```
         缺省spring boot web应用中ServletContextInitializerBeans从beanFactory所获得的SCI有 :
         
         dispatcherServlet(类型:ServletRegistrationBean),characterEncodingFilter,hiddenHttpMethodFilter,httpPutFormContentFilter,requestContextFilter(类型:FilterRegistrationBean)

         
         ```
      - beans.onStartup(servletContext);//这里是真正的Servlet, Filter 和EventListener注入到ServletContext的触发点
  
  - initPropertySources();

###### 除了Tomcat Connector之外的Tomcat组件启动完毕,接下来继续启动Connector

- WebServer webServer = startWebServer(); // [父类 finishRefresh()](../springcore/Spring源码解析之refresh()方法.md)

  - webServer.start();
     
     ```
     Connector启动的核心动作是协议处理器的启动对于缺省配置的springboot web应用，它会在8080端口提供 HTTP 服务，所以这里是一个处理http协议请求的 Http11NioProtocol 实例，使用 nio 方式处理 http 协议，Connector 对HTTP请求的接收和处理并不是亲自完成的，而是交给该 Http11NioProtocol
     
     protocolHandler 完成，而 protocolHandler 又进一步将请求处理工作交给 NioEndpoint 完成
     
     ```
  
    - addPreviouslyRemovedConnectors();//将内置容器创建和初始化阶段删除的Connector再添加到容器，将Connector添加回容器(实际上是添加到容器的Service)，因为相应的Service已经处于启动状态，所以Connector在添加回来之后马上会被启动. see number 104
    
    - Connector connector = this.tomcat.getConnector();//获得tomcat的Connector，如果不为空并且设置为自动启动，则启动之。缺省配置下，这里 autoStart 为 true 连接器 Connector 主要是接收用户的请求，然后封装请求传递给容器处理，tomcat中默认的连接器是Coyote。连接器表示Tomcat将会在哪个端口使用哪种协议提供服务。
    
	- performDeferredLoadOnStartup();//启动Connector
    
    - checkThatConnectorsHaveStarted();// 检查确保Connector已经启动，如果没启动，抛出异常