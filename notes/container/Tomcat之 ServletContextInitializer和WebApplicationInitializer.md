### ServletContainerInitializer(SCI)

1. ServletContainerInitializer是servlet3.0规范中引入的接口，能够让web应用程序在servlet容器启动后做一些自定义的操作

2. ServletContainerInitializer 基于服务提供者接口（SPI）概念，因此你需要在你的jar包目录下添加META-INF/services/javax.servlet.ServletContainerInitializer文件，内容就是ServletContainerInitializer实现类的全限定名。

3. ServletContainerInitializer#onStartup方法由Servlet容器调用(必须至少支持Servlet 3.0版本)。我们在这个方法中通过编程的方式去注册Servlet Filter Listener等组件，代替web.xml

4. 可以配合 @HandleTypes 注解，通过指定Class，容器会把所有的指定类的子类作为方法onStartup 的参数Set<Class<?>> c传递进来

### SpringServletContainerInitializer 它是Spring提供的ServletContainerInitializer的实现类

```
public void onStartup(@Nullable Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)throws ServletException {

		List<WebApplicationInitializer> initializers = new LinkedList<>();

		if (webAppInitializerClasses != null) {
			for (Class<?> waiClass : webAppInitializerClasses) {
				// Be defensive: Some servlet containers provide us with invalid classes,
				// no matter what @HandlesTypes says...
				if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers()) &&
						WebApplicationInitializer.class.isAssignableFrom(waiClass)) {
					try {
						initializers.add((WebApplicationInitializer)
								ReflectionUtils.accessibleConstructor(waiClass).newInstance());
					}
					catch (Throwable ex) {
						throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
					}
				}
			}
		}

		if (initializers.isEmpty()) {
			servletContext.log("No Spring WebApplicationInitializer types detected on classpath");
			return;
		}

		servletContext.log(initializers.size() + " Spring WebApplicationInitializers detected on classpath");
		AnnotationAwareOrderComparator.sort(initializers);
		for (WebApplicationInitializer initializer : initializers) {
			initializer.onStartup(servletContext);
		}
	}



```

### WebApplicationInitializer

1. WebApplicationInitializer是Spring提供的接口，和ServletContainerInitializer没有直接关系，但是和它有间接关系

2. WebApplicationInitializer在SpringServletContainerInitializer中实例化后被调用。

3. SpringServletContainerInitializer实现了servlet容器提供的接口带了个头，接下来的事可以交由 spring自己定义的WebApplicationInitializer
 

####  ServletContextInitializerBeans 实例表示一个从ListableBeanFactory bean容器中获得的ServletContextInitializer实例的集合。这个集合中的每个元素来自容器中定义的每个如下类型的bean

- ServletContextInitializer bean:具体可能以ServletRegistrationBean/FilterRegistrationBean/EventListenerRegistrationBean的形式存在

- Servlet/Filter/EventListener bean:这些 bean直接以Servlet/Filter/EventListener bean的形式存在

**所有这些bean最终都会以 ServletContextInitializer 形式在随后Servlet容器启动阶段ServletContext创建后应用于初始化ServletContext**

```
//selfInitialize()方法,在具体的内置Servlet容器启动过程中Servlet上下文创建之后,Spring EmbeddedWebApplicationContext 使用 bean 容器中定义的ServletContextInitializer对Servlet上下文进行初始化

public class ServletWebServerApplicationContext extends GenericWebApplicationContext implements ConfigurableWebServerApplicationContext
  		
private void selfInitialize(ServletContext servletContext) throws ServletException {
		prepareWebApplicationContext(servletContext);
		registerApplicationScope(servletContext);
		WebApplicationContextUtils.registerEnvironmentBeans(getBeanFactory(), servletContext);
		// 返回需要在内置Servlet Context上应用的ServletContextInitializer bean,缺省情况下,该方法先找直接定义为ServletContextInitializer的bean,然后是定义为Servlet,
		// Filter,EventListener的bean，将它们封装成 ServletContextInitializer bean
		// 排序然后一并返回,排序算法使用 AnnotationAwareOrderComparator。
		for (ServletContextInitializer beans : getServletContextInitializerBeans()) {
			beans.onStartup(servletContext);
		}
	}
```
#### SpringBoot 调用链

- 在主线程中,SpringBoot启动过程交给内置Tomcat Servlet 容器一个ServletContextInitializer
```
SpringApplication.run()
 => refreshContext()
  => EmbeddedWebApplicationContext.refresh()
  => onRefresh()
  => createEmbeddedServletContainer()


```

- 在内置Tomcat Servlet容器启动线程中，ServletContext创建之后调用该ServletContextInitializer 

```
StandartContext.startInternal()
 => TomcatStarter.onStartup()
   => EmbeddedWebApplicationContext.selfInitialize()
   // containerFactory.getEmbeddedServletContainer(getSelfInitializer());


```

---
  
一组ServletContextInitializer会被设置到ServletContainerInitializer TomcatStarter上,
而TomcatStarter在Servlet容器启动过程中调用自己的方法(onStartup)会逐一调用这些ServletContextInitializer的方法onStartUp的方法 onStartUp初始化ServletContext

#### class TomcatStarter implements ServletContainerInitializer
```
public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
		try {
		   //Servlet 容器启动时回会用该方法，该方法会逐一调用每个 ServletContextInitializer 的方法
		   //#onStartup 会指定 ServletContext 进行初始化。这些 ServletContextInitializer 的目的通常会是 注册 Servlet, Filter 或者 EventListener 。
			for (ServletContextInitializer initializer : this.initializers) {
				initializer.onStartup(servletContext);
			}
		}
		catch (Exception ex) {
			this.startUpException = ex;
			if (logger.isErrorEnabled()) {
				logger.error("Error starting Tomcat context. Exception: " + ex.getClass().getName() + ". Message: "+ ex.getMessage());
			}
		}
	}

```

#### ServletContextInitializer: Spring Boot提供的在Servlet 3.0+环境中用于程序化配置ServletContext的接口

- 该接口ServletContextInitializer主要被RegistrationBean实现用于往ServletContext容器中注册Servlet,Filter或者EventListener;

- 这些ServletContextInitializer的设计目的主要是用于这些实例被Spring IoC容器管理。

- 这些ServletContextInitializer实例不会被SpringServletContainerInitializer检测，因此不会被Servlet容器自动启动


#### WebApplicationInitializer:

- Spring Web中,WebApplicationInitializer也是针对Servlet 3.0+环境，设计用于程序化配置ServletContext

- 跟传统的web.xml相对或者配合使用

- WebApplicationInitializer实现类会被 SpringServletContainerInitializer 自动检测和启动