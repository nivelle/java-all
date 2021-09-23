### 容器

- BeanFactory Ioc容器:是Spring里面最低层的接口，提供了最简单的容器的功能，只提供了实例化对象和拿对象的功能；

- ApplicationContext 应用上下文或者spring容器，spring技术内幕里说是高级Ioc容器

- WebApplicationContext专门为web应用准备，扩展了ApplicationContext。

### 初始化：

- 通过xmlBeanFactory初始化BeanFactory

- 通过new ClassPathXmlApplicationContext("aaa/bean.xml")或new FileSystemXmlApplicationContext"aaa/bean.xml”) 初始化ApplicationContext。例如我们在main函数里启动spring。 见spring 3.x企业应用 3.4.2节。

- WebApplicationContext需要servlet环境，也就是说必须拥有web服务器(如tomcat)的前提下才能启动。他的初始化在web.xml中配置，我们做的项目也大多是webApplicationContext的初始化。为了启动webApplicationContext，spring提供了两种方式：ContextLoaderServlet和ContextLoaderListener(必须是servlet2.3或以上的版本的web容器才支持web监听器，些即使支持Servlet 2.3的Web服务器,但也不能在Servlet初始化之前启动Web监听器,如Weblogic 8.1、WebSphere 5.x、Oracle OC4J 9.0。)

两者的内部都实现了启动WebApplicationContext的内部逻辑，我们只需要在web.xml中配置就行了。意思即：只要在web.xml配置了两者之一(假如是ContextLoaderListener)，我们启动tomcat服务器时，ContextLoaderListener就会监听到tomcat启动完成后，根据web.xml中的配置去启动WebApplicationContext。

- servletConsextListener监听servletContext(tomcat里的servletContext)启动销毁，web.xml里配置的ContextLoaderListener，实现了servletConsextListener，初始化时，ContextLoaderListener里contextInitialized进行一系列的动作，这是web容器和ioc容器启动时关系

### web.xml属于tomcat ？可以看tomcat书了解更多

- web.xml是一个web工程的部署描述文件。https://segmentfault.com/q/1010000000338283/a-1020000000338619

它不是tomcat规定的，tomcat只是一个web容器，尽管tomcat在其示例中给出相关的web.xml，并且在其文档中也给出了简略的示例：
http://tomcat.apache.org/tomcat-8.0-doc/appdev/web.xml.txt
上述地址的tomcat版本号8.0，可换成不同的版本号，例如7.0,5.5等。 猜想你想知道的是web.xml文件支持哪些配置节点、属性，这个是Java Web工程部署文件规范，具体是有谁制定，我不知道，不过可以从web.xml的schema声明http://java.sun.com/dtd/web-app_2_3.dtd中看出其支持的配置节点：
