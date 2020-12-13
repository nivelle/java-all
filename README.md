## java 编程每天学

### java-notes 多数文章来自网上优秀文章摘要整合,仅供记录学习。

### 依赖环境: 

- **JDK版本:1.8;**
- **Mysql版本:8.0.16;**
- **Kafka版本:2.2.1;** 
- **RabbitMQ版本:3.7.15;** 
- **Redis版本:5.0.4;** 
- **Maven版本:3.6.1** 
- **ZooKeeper版本:3.4.13;**
- **Nginx版本:1.17.1**
- **Dubbo版本：0.2.0**
- **Elasticsearch版本：6.8.6**

#### docker整合

- 【下载:docker pull nivelle/nivelle:1.0.2】

- 【运行:sudo docker run -p 28080:8088 -it --rm nivelle/nivelle:1.0.2 bash 

--------------------

✅ 【spring refresh()方法学习路径】

✅ 【生命周期学习】

✅ 【CommandLineRunner】

✅ 【shutdown 实现优雅停机】

✅ 【InitializingBean】

✅ 【BeanFactoryAware】

✅ 【FactoryBean】

✅ 【ApplicationContextAware】

✅ 【EmbeddedValueResolverAware】

✅ 【SpringAOP】

✅ 【BeanDefinitionRegistryPostProcessor】

✅ 【springApplicationRunListeners:springBoot自带事件】

✅ 【ContextRefreshedEvent:spring自带监听器】

✅ 【ServletContextListener:servlet容器监听器】

✅ 【ApplicationEvent:自定义事件】

✅ 【javaConfig:配置类】

✅ 【validate:注解校验】
 
✅ 【exception:全局异常处理】 

✅ 【Filter:过滤器】

✅ 【interceptor:拦截器】

✅ 【starter:自定义starter】

✅ 【xml:对xml格式参数的支持】

✅ 【消息转化:HttpMessageConverters】

✅ 【Shiro:权限管理】

✅ 【定时任务】

✅ 【swagger:接口文档自动生成】

✅ 【JdbcTemplate && Mybatis 多数据源配置整合】

✅ 【CorsFilter 解决跨域问题】

✅ 【xml配置文件扫描】

✅ 【@Async&@EnableAsync】

✅ 【@Lazy&@Scope&@ComponentScan】

✅ 【自定义扫描过滤器】

✅ 【@Profile】

✅ 【AbstractGenericHttpMessageConverter】

✅ 【HandlerMethodArgumentResolver:请求参数消息转换器】

✅ 【HandlerMethodReturnValueHandler:返回消息转换器】

✅ 【RequestBodyAdvice】

✅ 【ResponseBodyAdvice】

✅ 【ApplicationRunner&&CommandLineRunner】

✅ 【WebMvcConfigurer && @EnableWebMvc】

✅ 【@Scheduled】&& 【@EnableScheduling】

✅ 【TestNG】

✅ 【ignoreDependencyInterface】

✅ 【registerResolvableDependency】


--------------------

### middleware项目

✅ 【mybatis 整合到springBoot】

✅ 【mybatis 使用redis做缓存组件】


##### JVM实践

✅ 【自定义类加载器】

✅ 【类实例化过程实例】

#### zookeeper实践

⌛ 自己实现RPC 

✅ 【zookeeper选主】

✅ 【zookeeper分布式锁】

⌛ 分布式事物


##### Redis实践

✅ 【redis实践】

#### RabbitMQ

✅ 【rabbitMQ】

#### Kafka

✅【Produce】

✅【Consumer】

✅【Filter:过滤器】

✅【ErrorHandler】

✅【Partitioner:自定义分区】

#### MySQL实践

✅ 【mysql常用语句】

✅️ 乐观锁，悲观锁，事物，传播特性

✅ 【mysql explain】


#### HBase实践

✅【put】

✅【scan】

✅【get】

✅【delete】

✅【delete所有版本数据】

#### Netty实践 

✅【客户端&服务端】

#### Mongo实践

✅ 【mongo实践】

#### ElasticSearch实践

✅ 【ElasticSearch实践】

#### Kibana

✅ 【Kibana实践】

--------------------------


### container-all项目

#### Tomcat实践

✅ 【SPI&SCI机制】

✅ 【AbstractAnnotationConfigDispatcherServletInitializer】

✅ 【ServletContainerInitializer】

✅ 【ServletContextInitializer】

✅ 【SpringBootServletInitializer】

✅ 【SpringServletContainerInitializer】

✅ 【WebApplicationInitializer】

-------------------------

### rpc-dubbo

✅ 【ConfigurableServletWebServerFactory:自定义Tomcat】

✅ 【dubbo service】

✅ 【AsyncService】

### java-base项目

#### 算法实践

✅ 【有序二维数组查】

✅ 【空格字符窜替换】

✅ 【从尾到头打印数组】

✅ 【构建二叉树】

✅ 【两个栈实现队列】

✅ 【最小旋转数组】

✅ 【斐波那契数列】

✅ 【二进制1的个数】

✅ 【数值的整数次方】

✅ 【数组奇数偶数相对排序】

#### Java2e实践

✅【自定义注解】

✅【动态代理】

✅【多线程示例】

✅【jdk8 Lambda表达式,Stream的使用】

✅【java 关键字(instanceof,final,continue)】

✅【范型:范型类,范性方法,范型接口】

✅【java序列化】

#### JDK源码

✅【Integer】

✅【String】

✅【Float】

✅【Byte】

✅【Long】

--- 
✅【BigDecimal】

✅【Enum】

✅【Math】

✅【System】 

✅【Unsafe】

✅【Reflection】

✅【Object】

✅【Thread】

✅【striped64】 

✅【Contended】 

--------------------

✅【ArrayList】

✅【LinkedList】

✅【HashMap】

✅【LinkedHashMap】

✅【TreeMap】

✅【TreeSet】

✅【WeakHashMap】

✅【HashSet】

✅【LinkedHashSet】

✅【Stack】

✅【DelayQueue】

--------------------

✅【ThreadPool】

✅【ScheduledThreadPoolExecutor】

✅【ThreadLocal】

✅【CountDownLatch】

✅【ConcurrentHashMap】

✅【LongAccumulator】

✅【LongAdder】

✅【AtomicStampedReference】

✅【AtomicInteger】

✅【ArrayDeque】

✅【PriorityQueue】

✅【ArrayBlockingQueue】

✅【LinkedBlockingQueue】

✅【PriorityBlockingQueue】

✅【SynchronizedQueue】

✅【ConcurrentSkipListMap】

✅【CopyOnWriteArrayList】

✅【CopyOnWriteArraySet】

✅【ConcurrentLinkedQueue】

✅【LinkedTransferQueue】

✅【CyclicBarrier】

✅【Phaser】

✅【Condition】

✅【ReentrantReadWriteLock】

✅【Semaphore】

✅【StampedLock】

✅【LockSupport】

✅【FutureTask】

✅【ForkJoin】

✅【AtomicIntegerArray】

✅【ThreadLocalRandom】


#### 设计模式实践

✅ 【迭代器模式】

✅ 【适配器模式】

✅ 【模板方法模式】

✅ 【工厂方法模式】

✅ 【代理模式】

✅ 【责任链模式】

✅ 【桥梁模式】

✅ 【策略模式】
 
✅ 【构建模式】

✅ 【抽象工厂模式】

✅ 【观察者模式】

-------------------------

#### 基础学习

✅【git 指令学习】

✅【linux 基础指令】

✅【shell 编程】

✅【docker 学习】

✅【nginx 基础】

✅【maven 基础】

----------------------------
