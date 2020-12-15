### SpringApplication

#### 属性
````

//springboot中，allowBeanDefinitionOverriding 默认为false；spring默认为true。需要在application.properties中新增spring.main.allow-bean-definition-overriding=true
//Sets if bean definition overriding, by registering a definition with the same name as an existing definition, should be allowed. Defaults to {@code false} :是否允许注册已经存在的类定义,springboot默认是不允许的
private boolean allowBeanDefinitionOverriding;

````
#### 构造函数

````
public SpringApplication(Class<?>... primarySources) {
		//调用下面构造方法
        this((ResourceLoader)null, primarySources);
    }

    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
    	//设置一些默认参数
        this.sources = new LinkedHashSet();
        this.bannerMode = Mode.CONSOLE;
        this.logStartupInfo = true;
        this.addCommandLineProperties = true;
        this.addConversionService = true;
        this.headless = true;
        this.registerShutdownHook = true;
        this.additionalProfiles = new HashSet();
        this.isCustomEnvironment = false;
        // Sets if beans should be initialized lazily. Defaults to {@code false}.
        this.lazyInitialization = false;
        this.resourceLoader = resourceLoader;
        Assert.notNull(primarySources, "PrimarySources must not be null");
        
        //保存主配置类到一个Set集合primarySources中,也就是XXXBootStrapApplication类，加了@SpringBootApplication 注解的类
        this.primarySources = new LinkedHashSet(Arrays.asList(primarySources));
        
        //获取当前的应用类型，判断是不是web应用，从ClassPath推断是否是Web应用
        this.webApplicationType = WebApplicationType.deduceFromClasspath();
       
        //从类路径下找到META‐INF/spring.factories 配置的所有 ApplicationContextInitializer；然后保存起来
        this.setInitializers(this.getSpringFactoriesInstances(ApplicationContextInitializer.class));
       
        //从类路径下找到META‐INF/spring.ApplicationListener；然后保存起来,原理同上
        this.setListeners(this.getSpringFactoriesInstances(ApplicationListener.class));
       
        //从多个配置类中找到有main方法的主配置类，(在调run方法的时候是可以传递多个配置类的)
        this.mainApplicationClass = this.deduceMainApplicationClass();
        //执行完毕，SpringApplication对象创建完毕
    }

````

#### deduceFromClasspath() 判断是不是Web应用

````
static WebApplicationType deduceFromClasspath() {
        if (ClassUtils.isPresent("org.springframework.web.reactive.DispatcherHandler", (ClassLoader)null) && 
            !ClassUtils.isPresent("org.springframework.web.servlet.DispatcherServlet", (ClassLoader)null) && 
            !ClassUtils.isPresent("org.glassfish.jersey.servlet.ServletContainer", (ClassLoader)null)) {
            return REACTIVE;
        } else {
            String[] var0 = SERVLET_INDICATOR_CLASSES;
            int var1 = var0.length;

            for(int var2 = 0; var2 < var1; ++var2) {
                String className = var0[var2];
                if (!ClassUtils.isPresent(className, (ClassLoader)null)) {
                    return NONE;
                }
            }

            return SERVLET;
        }
    }
````

#### getSpringFactoriesInstances

````
private <T> Collection<T> getSpringFactoriesInstances(Class<T> type) {
		return getSpringFactoriesInstances(type, new Class<?>[] {});
	}

	private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
		ClassLoader classLoader = getClassLoader();
		// Use names and ensure unique to protect against duplicates
        // 获取key为ApplicationContextInitializer全类名的所有值
		Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
        //根据拿到的类名集合，使用反射创建对象放到集合中返回
		List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
		AnnotationAwareOrderComparator.sort(instances);
		return instances;
	}

````

#### loadFactoryNames //把类路径下所有META‐INF/spring.factories中的配置都存储起来

````
public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
             String factoryTypeName = factoryType.getName();
             return (List)loadSpringFactories(classLoader).getOrDefault(factoryTypeName, Collections.emptyList());
         }

````

````
private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
        MultiValueMap<String, String> result = (MultiValueMap)cache.get(classLoader);
        if (result != null) {
            return result;
        } else {
            try {
                Enumeration<URL> urls = classLoader != null ? classLoader.getResources("META-INF/spring.factories") : ClassLoader.getSystemResources("META-INF/spring.factories");
                LinkedMultiValueMap result = new LinkedMultiValueMap();

                while(urls.hasMoreElements()) {
                    URL url = (URL)urls.nextElement();
                    UrlResource resource = new UrlResource(url);
                    Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                    Iterator var6 = properties.entrySet().iterator();

                    while(var6.hasNext()) {
                        Entry<?, ?> entry = (Entry)var6.next();
                        String factoryTypeName = ((String)entry.getKey()).trim();
                        String[] var9 = StringUtils.commaDelimitedListToStringArray((String)entry.getValue());
                        int var10 = var9.length;

                        for(int var11 = 0; var11 < var10; ++var11) {
                            String factoryImplementationName = var9[var11];
                            result.add(factoryTypeName, factoryImplementationName.trim());
                        }
                    }
                }

                cache.put(classLoader, result);
                return result;
            } catch (IOException var13) {
                throw new IllegalArgumentException("Unable to load factories from location [META-INF/spring.factories]", var13);
            }
        }
    }
````

#### createSpringFactoriesInstances 通过反射创建实例

````
private <T> List<T> createSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes,
			ClassLoader classLoader, Object[] args, Set<String> names) {
		List<T> instances = new ArrayList<>(names.size());
		for (String name : names) {
			try {
				Class<?> instanceClass = ClassUtils.forName(name, classLoader);
				Assert.isAssignable(type, instanceClass);
				Constructor<?> constructor = instanceClass.getDeclaredConstructor(parameterTypes);
				T instance = (T) BeanUtils.instantiateClass(constructor, args);
				instances.add(instance);
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("Cannot instantiate " + type + " : " + name, ex);
			}
		}
		return instances;
	}

````

#### 环境配置，命令行参数的解析

````

protected void configurePropertySources(ConfigurableEnvironment environment, String[] args) {
        //从上面创建的ConfigurableEnvironment实例中获取MutablePropertySources实例
		MutablePropertySources sources = environment.getPropertySources();
        //如果有defaultProperties属性的话，则把默认属性添加为最后一个元素
		if (this.defaultProperties != null && !this.defaultProperties.isEmpty()) {
			sources.addLast(new MapPropertySource("defaultProperties", this.defaultProperties));
		}
        // 这里addCommandLineProperties默认为true 如果有命令行参数的数
		if (this.addCommandLineProperties && args.length > 0) {
            //name为：commandLineArgs
			String name = CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME;
            //如果之前的 MutablePropertySources 中有name为 commandLineArgs的PropertySource的话，则把当前命令行参数转换为 CompositePropertySource 类型，和原来的PropertySource进行合并，替换原来的PropertySource
			if (sources.contains(name)) {
				PropertySource<?> source = sources.get(name);
				CompositePropertySource composite = new CompositePropertySource(name);
				composite.addPropertySource(new SimpleCommandLinePropertySource("springApplicationCommandLineArgs", args));
				composite.addPropertySource(source);
				sources.replace(name, composite);
			}
			else {
                //如果之前没有name为commandLineArgs的PropertySource的话，则将其添加为MutablePropertySources中的第一个元素，注意了这里讲命令行参数添加为ConfigurableEnvironment中MutablePropertySources实例的第一个元素，且永远是第一个元素
				sources.addFirst(new SimpleCommandLinePropertySource(args));
			}
		}
	}

````