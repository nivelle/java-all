### SpringBoot 之自动配置

#### 1. springBoot隐含的自动依赖

```
  //   org.springframework.boot:spring-boot-starter
	    //   |--org.springframework.boot:spring-boot
	    //   |--org.springframework.boot:spring-boot-autoconfigure
	    //   |--org.springframework.boot:spring-boot-starter-logging
	    //   |--org.springframework:spring-core
	    //   |--org.yaml:snakeyaml

```

#### 2. 自动配置注解

 - @SpringBootApplication
 
   - @EnableAutoConfiguration

     - @Import(AutoConfigurationImportSelector.class)

     - public String[] selectImports(AnnotationMetadata annotationMetadata)
     
       - AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
     
         - loadMetadata(classLoader, PATH);// PATH = "META-INF/" + "spring-autoconfigure-metadata.properties";加载MetaData数据
     
       - AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(autoConfigurationMetadata,annotationMetadata);
     
         - AnnotationAttributes attributes = getAttributes(annotationMetadata);
       
         - List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
             //自动配置加载默认的
           - List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),getBeanClassLoader());
               //加载META-INF/spring.factories下的自动配置
             - loadSpringFactories(classLoader).getOrDefault(factoryTypeName, Collections.emptyList());
           //使用 LinkedHashSet实现移除冲突的配置
         - configurations = removeDuplicates(configurations);
           //获取配置除外的配置
         - Set<String> exclusions = getExclusions(annotationMetadata, attributes);
       
         - checkExcludedClasses(configurations, exclusions);//检查要排除的配置是否是自动配置，否则跑出异常
       
         - configurations.removeAll(exclusions);//移除需要被除外的配置

         - configurations = filter(configurations, autoConfigurationMetadata);
           
           ```
           1. 应用过滤器AutoConfigurationImportFilter，对于 spring boot autoconfigure，定义了一个需要被应用的过滤器:org.springframework.boot.autoconfigure.condition.OnClassCondition,
              
              此过滤器检查候选配置类上的注解@ConditionalOnClass，如果要求的类在classpath中不存在，则这个候选配置类会被排除掉
              
           2. 使用内部工具 SpringFactoriesLoader，查找classpath上所有jar包中的META-INF\spring.factories，找出其中key为org.springframework.boot.autoconfigure.AutoConfigurationImportFilter 
              
              的属性定义的过滤器类并实例化。AutoConfigurationImportFilter过滤器可以被注册到 spring.factories用于对自动配置类做一些限制，在这些自动配置类的字节码被读取之前做快速排除处理。
              
              spring boot autoconfigure 缺省注册了一个 AutoConfigurationImportFilter:org.springframework.boot.autoconfigure.condition.OnClassCondition.也就是检查当前Class是否存在不存在的话满足过滤条件
           
           ```
           
         - fireAutoConfigurationImportEvents(configurations, exclusions);//触发事件

         - return new AutoConfigurationEntry(configurations, exclusions);

      - return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());

#### 3. 应用启动时 ConfigurationClassPostProcessor 的注册 [SpringBoot run()方法prepareContext创建BeanDefinitionReader时构造ConfigurationClassPostProcessor注册](./SpringBoot源码解析之run()方法.md)


1. **ConfigurationClassPostProcessor 被设计用来发现所有的配置类和相关的Bean定义并注册到容器，它在所有BeanFactoryPostProcessor中具备最高执行优先级，因为其他BeanFactoryPostProcessor需要基于注册了Bean定义工作。**

2. **ConfigurationClassPostProcessor.postProcessBeanFactory()会将识别这些配置类中定义的bean并将它们注册到容器。**

3. PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors()方法会先应用所有的BeanDefinitionRegistryPostProcessor的方法postProcessBeanDefinitionRegistry()，直到参数指定的或者容器中所有的这些BeanDefinitionRegistryPostProcessor的该方法都被执行完，然后执行所有的BeanFactoryPostProcessor的方法postProcessBeanFactory()直到参数指定的或者容器中所有的这些BeanFactoryPostProcessor的该方法都被执行完。


-  AbstractApplicationContext.refresh()
   
   - invokeBeanFactoryPostProcessors()
 
   - PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors(); [refresh()第五步](Spring源码解析之refresh()方法.md)
   
     - public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry);//使用工具ConfigurationClassParser尝试发现所有的配置(@Configuration)类，使用工具ConfigurationClassBeanDefinitionReader注册所发现的配置类中所有的bean定义。结束执行的条件是所有配置类都被发现和处理,相应的bean定义注册到容器
                                                                                        
     - int registryId = System.identityHashCode(registry); =>this.registriesPostProcessed.add(registryId);
     
     - processConfigBeanDefinitions(registry);//处理配置类的bean定义信息,Build and validate a configuration model based on the registry of {@link Configuration} classes
     
       - List<BeanDefinitionHolder> configCandidates = new ArrayList<BeanDefinitionHolder>();
       
       - String[] candidateNames = registry.getBeanDefinitionNames();
       
         ```
         //  0 = "org.springframework.context.annotation.internalConfigurationAnnotationProcessor"
         //  1 = "org.springframework.context.annotation.internalAutowiredAnnotationProcessor"
         //  2 = "org.springframework.context.annotation.internalCommonAnnotationProcessor"
         //  3 = "org.springframework.context.annotation.internalPersistenceAnnotationProcessor"
         //  4 = "org.springframework.context.event.internalEventListenerProcessor"
         //  5 = "org.springframework.context.event.internalEventListenerFactory"
         //  6 = "application" ⇐ 这是开发人员使用了注解 @SpringBootApplication 的程序入口类
         //  7 = "org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory" 
         
         ```
     #### for (String beanName : candidateNames)
       
        - BeanDefinition beanDef = registry.getBeanDefinition(beanName);
        
        - if (ConfigurationClassUtils.isFullConfigurationClass(beanDef) || ConfigurationClassUtils.isLiteConfigurationClass(beanDef))
        
        - else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef,this.metadataReaderFactory)) 
        
          - configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));// 如果这个Bean定义有注解@Configuration，将其记录为候选配置类
          
        - if (configCandidates.isEmpty()) return; //一个候选配置类都没有找到，直接返回

        - Collections.sort(configCandidates, new Comparator<BeanDefinitionHolder>());//Sort by previously determined @Order value, if applicable
        
        - ConfigurationClassParser parser = new ConfigurationClassParser(this.metadataReaderFactory, this.problemReporter, this.environment,this.resourceLoader, this.componentScanBeanNameGenerator, registry); // Parse each @Configuration class，现在准备要分析配置类了
        
        - Set<BeanDefinitionHolder> candidates = new LinkedHashSet<BeanDefinitionHolder>(configCandidates);//表示将要被处理的候选配置类因为不清楚候选是否确实是配置类，所以使用BeanDefinitionHolder类型记录这里初始化为方法开始时容器中注解了@Configuration的Bean定义的集合

        - Set<ConfigurationClass> alreadyParsed = new HashSet<ConfigurationClass>(configCandidates.size());//表示已经处理的配置类，已经被处理的配置类已经明确了其类型，所以用 ConfigurationClass 类型记录，初始化为空
        
        - do{} while(!candidates.isEmpty());//一直循环到没有新的候选配置类被发现
        
          - parser.parse(candidates);//1. 如果遇到注解了@Component类，直接作为Bean定义注册到容器 2. 如果注解或者注解的注解中有@Import, 处理所有这些@import，识别配置类,添加到分析器的属性configurationClasses中去
          
          - parser.validate();
          
          - Set<ConfigurationClass> configClasses = new LinkedHashSet<ConfigurationClass>(parser.getConfigurationClasses());//从分析器parser中获取分析得到的配置类configurationClasses
          
          - configClasses.removeAll(alreadyParsed);
          
          - if (this.reader == null) =》 this.reader = new ConfigurationClassBeanDefinitionReader(registry, this.sourceExtractor, this.resourceLoader, this.environment,this.importBeanNameGenerator, parser.getImportRegistry());
                                       
          - this.reader.loadBeanDefinitions(configClasses);//使用 ConfigurationClassBeanDefinitionReader reader 从 configClasses 中加载Bean定义并注册到容器
          
          - alreadyParsed.addAll(configClasses);// 刚刚处理完的配置类记录到已处理配置类alreadyParsed
          
          - if (registry.getBeanDefinitionCount() > candidateNames.length) 
          
            - String[] newCandidateNames = registry.getBeanDefinitionNames();//经过一轮do循环,现在容器中Bean定义数量超过了该次循环开始时的容器内Bean定义数量，说明在该次循环中发现并注册了更多的Bean定义到容器中去，这些新注册的Bean定义也有可能是候选配置类，它们也要被处理用来发现和注册Bean定义
            
            - Set<String> oldCandidateNames = new HashSet<String>(Arrays.asList(candidateNames));

            - Set<String> alreadyParsedClasses = new HashSet<String>();
            
            - for (ConfigurationClass configurationClass : alreadyParsed) {alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());」
            
            - for (String candidateName : newCandidateNames) =》if (!oldCandidateNames.contains(candidateName)) =》candidates.add(new BeanDefinitionHolder(bd, candidateName));// 在新注册的Bean定义中找到一个候选配置类
            
            - sbr.registerSingleton(IMPORT_REGISTRY_BEAN_NAME, parser.getImportRegistry());//Register the ImportRegistry as a bean in order to support ImportAware @Configuration classes

     - public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory);//1. 对容器中的每个配置类做增强 2. 往容器中增加一个BeanPostProcessor:ImportAwareBeanPostProcessor(如果所增加的BeanPostProcessor已经存在会先将其删除然后重新添加)
       
       - int factoryId = System.identityHashCode(beanFactory);
       
       - this.factoriesPostProcessed.add(factoryId);
       
       - enhanceConfigurationClasses(beanFactory);//对容器中的每个配置类做增强,该增强是通过为相应配置类创建一个CGLIB子类来完成的。
        
         ```
         Spring中存在这样一个工具类ConfigurationClassEnhancer,它会对应用中每个配置类，也就是一般通过@Configuration注解定义的类进行一个增强。通过增强以后，配置类中使用@Bean注解的bean定义方法就不再是普通的方法了，它们具有了如下跟bean作用域有关的能力，以单例bean为例
         
         1.它们首次被调用时，相应方法逻辑会被执行用于创建bean实例；
         
         2.再次被调用时，不会再执行创建bean实例，而是根据bean名称返回首次该方法被执行时创建的bean实例；

         3.增强器可以理解为是对被增强类的对象进行了增强，比如在方法调用前后做拦截等等，回调过滤器设置就是这个意思，CALLBACK_FILTER(new Callback[] {new BeanMethodInterceptor(),new BeanFactoryAwareMethodInterceptor(),NoOp.INSTANCE};)就相当于为被增强类增加的功能

         ```
       - beanFactory.addBeanPostProcessor(new ImportAwareBeanPostProcessor(beanFactory));//添加ImportAwareBeanPostProcessor到容器