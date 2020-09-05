### @EnableScheduling

#### 概述

- 注解@EnableScheduling 导入SchedulingConfiguration;

- SchedulingConfiguration 定义基础设施bean: ScheduledAnnotationBeanPostProcessor scheduledAnnotationProcessor;

- ScheduledAnnotationBeanPostProcessor在容器启动时做如下事情:

  (1) 记所有使用@Scheduled注解的bean方法到一个 ScheduledTaskRegistrar，供调度任务执行器 TaskScheduler 执行
  
  (2) 为ScheduledTaskRegistrar指定任务执行器TaskScheduler,该任务执行器来自容器中的bean TaskScheduler/ScheduledExecutorService(如果不指定,ScheduledTaskRegistrar自己会本地创建一个ConcurrentTaskScheduler)
      
  (3) 告诉ScheduledTaskRegistrar将所注册的调度任务,也就是使用@Scheduled注解的bean方法,调度到任务执行器TaskScheduler执行

### 核心源码


#### @EnableScheduling

```
@Import(SchedulingConfiguration.class)
public @interface EnableScheduling {

}

```

#### SchedulingConfiguration

```
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SchedulingConfiguration {

    // Bean 名称使用 : 
    // org.springframework.context.annotation.internalScheduledAnnotationProcessor
    // org.springframework.context.annotation.internalScheduledAnnotationProcessor
	@Bean(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE) // 定义为基础设施bean
	public ScheduledAnnotationBeanPostProcessor scheduledAnnotationProcessor() {
		return new ScheduledAnnotationBeanPostProcessor();
	}

}

```

#### ScheduledAnnotationBeanPostProcessor#postProcessAfterInitialization检测处理每个@Scheduled 注解的方法ScheduledAnnotationBeanPostProcessor实现了DestructionAwareBeanPostProcessor,BeanPostProcessor等接口。
#### 作为一个BeanPostProcessor,ScheduledAnnotationBeanPostProcessor会针对每个bean的创建,在bean生命周期方法#postProcessAfterInitialization中，扫描该bean中使用了注解@Scheduled的方法.

```
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		if (bean instanceof AopInfrastructureBean || bean instanceof TaskScheduler ||
				bean instanceof ScheduledExecutorService) {
			// Ignore AOP infrastructure such as scoped proxies.
			## 忽略AOP,TaskSchedule基础类
			return bean;
		}
        ## 获取指定实例的类信息
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        ## this.nonAnnotatedClasses 是一个缓存，用于记录处理过程中所发现的不包含任何被@Scheduled注解的方法的类
		if (!this.nonAnnotatedClasses.contains(targetClass)) {
          ## 获取类  targetClass 上所有使用注解  @Scheduled 的方法
          ## 注意 : 某个方法上可能同时使用多个注解  @Scheduled ,所以以下 annotatedMethods 的每个 Entry 是 一个方法对应一个 @cheduled 集合
			Map<Method, Set<Scheduled>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,(MethodIntrospector.MetadataLookup<Set<Scheduled>>) method -> {
						Set<Scheduled> scheduledMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(method, Scheduled.class, Schedules.class);
						return (!scheduledMethods.isEmpty() ? scheduledMethods : null);
					});
			if (annotatedMethods.isEmpty()) {
                ## 如果当前类 targetClass 不包含任何使用注解  @Scheduled 的方法，将其添加到 this.nonAnnotatedClasses
				this.nonAnnotatedClasses.add(targetClass);
				if (logger.isTraceEnabled()) {
					logger.trace("No @Scheduled annotations found on bean class: " + targetClass);
				}
			}
			else {
				
              ## 当前类 targetClass 上找到了使用注解 @Scheduled 的方法，记录在  annotatedMethods 中，
              ## 现在将它们逐个处理，使用的处理为方法 processScheduled             
				annotatedMethods.forEach((method, scheduledMethods) ->
				        ## 挨个处理定时任务方法
						scheduledMethods.forEach(scheduled -> processScheduled(scheduled, method, bean)));
				if (logger.isTraceEnabled()) {
					logger.trace(annotatedMethods.size() + " @Scheduled methods processed on bean '" + beanName +"': " + annotatedMethods);
				}
			}
		}
		return bean;
	}

```

#### processScheduled()

rocessScheduled处理方法上的每个@Scheduled注解，生成一个ScheduledTask并登记到this.scheduledTasks。 

this.scheduledTasks 数据结构为Map: key:是一个对象,其类就是含有方法使用了注解@Scheduled的类; value:是一个ScheduledTask集合,方法上的每个注解@Scheduled对应一个ScheduledTask;

```
	/**
	 * Process the given {@code @Scheduled} method declaration on the given bean.
	 * @param scheduled the @Scheduled annotation
	 * @param method the method that the annotation has been declared on
	 * @param bean the target bean instance
	 * @see #createRunnable(Object, Method)
	 */
	protected void processScheduled(Scheduled scheduled, Method method, Object bean) {
		try {
            ## 将使用了@Scheduled注解的方法包装成一个 Runnable 对象,随后构建 ScheduledTask 对象时会用得到
			Runnable runnable = createRunnable(bean, method);
            ## 用于记录当前 @Scheduled 注解是否已经被处理，初始化为 false  
			boolean processedSchedule = false;
			String errorMessage ="Exactly one of the 'cron', 'fixedDelay(String)', or 'fixedRate(String)' attributes is required";
            ## 用于保存针对当前 @Scheduled  注解生成的 ScheduledTask,该方法完成时，该集合内元素数量通常为 1
			Set<ScheduledTask> tasks = new LinkedHashSet<>(4);
			// Determine initial delay
            
            ## 确定 initial delay 属性:基于注解属性 initialDelay 或者  initialDelayString 分析得到,二者只能使用其中之一
			long initialDelay = scheduled.initialDelay();
			String initialDelayString = scheduled.initialDelayString();
			
			if (StringUtils.hasText(initialDelayString)) {
				Assert.isTrue(initialDelay < 0, "Specify 'initialDelay' or 'initialDelayString', not both");
				if (this.embeddedValueResolver != null) {
					initialDelayString = this.embeddedValueResolver.resolveStringValue(initialDelayString);
				}
				if (StringUtils.hasLength(initialDelayString)) {
					try {
						initialDelay = parseDelayAsLong(initialDelayString);
					}
					catch (RuntimeException ex) {
					    throw new IllegalArgumentException("Invalid initialDelayString value \"" + initialDelayString + "\" - cannot parse into long");
					}
				}
			}
			// Check cron expression
            ## 检查这是否是一个 cron 表达式类型的注解  
			String cron = scheduled.cron();
			if (StringUtils.hasText(cron)) {
				String zone = scheduled.zone();
				if (this.embeddedValueResolver != null) {
					cron = this.embeddedValueResolver.resolveStringValue(cron);
					zone = this.embeddedValueResolver.resolveStringValue(zone);
				}
				if (StringUtils.hasLength(cron)) {
					Assert.isTrue(initialDelay == -1, "'initialDelay' not supported for cron triggers");
					processedSchedule = true;
					if (!Scheduled.CRON_DISABLED.equals(cron)) {
						TimeZone timeZone;
						if (StringUtils.hasText(zone)) {
							timeZone = StringUtils.parseTimeZoneString(zone);
						}
						else {
							timeZone = TimeZone.getDefault();
						}
	                    ## 包装成为一个 CronTask,并提交给 线程池
						tasks.add(this.registrar.scheduleCronTask(new CronTask(runnable, new CronTrigger(cron, timeZone))));
					}
				}
			}
			// At this point we don't need to differentiate between initial delay set or not anymore
			if (initialDelay < 0) {
				initialDelay = 0;
			}
			// Check fixed delay
           ## 检查这是否是一个固定延迟类型的注解    
			long fixedDelay = scheduled.fixedDelay();
			if (fixedDelay >= 0) {
				Assert.isTrue(!processedSchedule, errorMessage);
				processedSchedule = true;
				## 包装成为一个 FixedDelayTask 
				tasks.add(this.registrar.scheduleFixedDelayTask(new FixedDelayTask(runnable, fixedDelay, initialDelay)));
			}
			String fixedDelayString = scheduled.fixedDelayString();
			if (StringUtils.hasText(fixedDelayString)) {
				if (this.embeddedValueResolver != null) {
					fixedDelayString = this.embeddedValueResolver.resolveStringValue(fixedDelayString);
				}
				if (StringUtils.hasLength(fixedDelayString)) {
					Assert.isTrue(!processedSchedule, errorMessage);
					processedSchedule = true;
					try {
						fixedDelay = parseDelayAsLong(fixedDelayString);
					}
					catch (RuntimeException ex) {
						throw new IllegalArgumentException(
						"Invalid fixedDelayString value \"" + fixedDelayString + "\" - cannot parse into long");
					}
	                 ## 包装成为一个 FixedDelayTask    
					tasks.add(this.registrar.scheduleFixedDelayTask(
						new FixedDelayTask(runnable, fixedDelay, initialDelay)));
				}
			}

			// Check fixed rate            
           ## 检查这是否是一个固定周期执行类型的注解    
			long fixedRate = scheduled.fixedRate();
			if (fixedRate >= 0) {
				Assert.isTrue(!processedSchedule, errorMessage);
				processedSchedule = true;
				tasks.add(this.registrar.scheduleFixedRateTask(new FixedRateTask(runnable, fixedRate, initialDelay)));
			}
			String fixedRateString = scheduled.fixedRateString();
			if (StringUtils.hasText(fixedRateString)) {
				if (this.embeddedValueResolver != null) {
					fixedRateString = this.embeddedValueResolver.resolveStringValue(fixedRateString);
				}
				if (StringUtils.hasLength(fixedRateString)) {
					Assert.isTrue(!processedSchedule, errorMessage);
					processedSchedule = true;
					try {
						fixedRate = parseDelayAsLong(fixedRateString);
					}
					catch (RuntimeException ex) {
						throw new IllegalArgumentException("Invalid fixedRateString value \"" + fixedRateString + "\" - cannot parse into long");
					}
					## 包装成为一个 FixedRateTask       
					tasks.add(this.registrar.scheduleFixedRateTask(new FixedRateTask(runnable, fixedRate, initialDelay)));
				}
			}
			// Check whether we had any attribute set
			Assert.isTrue(processedSchedule, errorMessage);
			// Finally register the scheduled tasks
			synchronized (this.scheduledTasks) {
				Set<ScheduledTask> regTasks = this.scheduledTasks.computeIfAbsent(bean, key -> new LinkedHashSet<>(4));
				regTasks.addAll(tasks);
			}
		}catch (IllegalArgumentException ex) {
			throw new IllegalStateException("Encountered invalid @Scheduled method '" + method.getName() + "': " + ex.getMessage());
		}
	}


```

#### 经过ScheduledAnnotationBeanPostProcessor以上这些处理，每个bean中所包含的@Scheduled注解都被发现了，这样的每条信息最终对应生成一个ScheduledTask,该ScheduledTask会被 ScheduledTaskRegistrar registrar 登记调度。
#### 这意味着该ScheduledTask从此刻起在程序运行期间就会按照@Scheduled注解所设定的时间点被执行。

### 备注1: 从上面的代码可以看出,如果多个定时任务定义的是同一个时间,那么也是顺序执行的，会根据程序加载Scheduled方法的先后来执行。

### 备注2: 但是如果某个定时任务执行未完成此任务一直无法执行完成，无法设置下次任务执行时间，之后会导致此任务后面的所有定时任务无法继续执行，也就会出现所有的定时任务“失效”现象
        

### 定时任务处理

- command: 任务

- initialDelay:初始化多久后开始执行

- period:周期
       
- unit: 时间单位
    
    
```
     
      public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,long initialDelay,long period,TimeUnit unit) {
              if (command == null || unit == null){
                  throw new NullPointerException();
              }
              if (period <= 0){
                  throw new IllegalArgumentException();
              }
              //将普通任务装饰成: ScheduledFutureTask 是 ScheduledThreadPoolExecutor的一个私有内部类
              ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(command,null,triggerTime(initialDelay, unit),unit.toNanos(period));
              //钩子方法,给子类用来替换装饰 task,这里认为t==sft
              RunnableScheduledFuture<Void> t = decorateTask(command, sft);
              sft.outerTask = t;
              //延时执行
              delayedExecute(t);
              return t;
          }
```     
      
##### triggerTime:触发时间计算

```
      long triggerTime(long delay) {
           return now() + ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
       }
     
```

##### delayedExecute(t);延时执行

```
ScheduledThreadPoolExecutor.delayedExecute(RunnableScheduledFuture task)
      private void delayedExecute(RunnableScheduledFuture<?> task) {
               //如果线程池关闭了,执行拒绝策略
              if (isShutdown()){
                  reject(task);
              }
              else {
                  //先把任务放到阻塞队列中去(addWorker()方法)
                  super.getQueue().add(task);
                  // 再次检查线程池状态
                  if (isShutdown() && !canRunInCurrentRunState(task.isPeriodic()) && remove(task)){
                      task.cancel(false);
                  }else{
                      //保证有足够有线程执行任务
                      ensurePrestart();
                  }
              }
        }
        
```
        
##### ensurePrestart() 保证有足够的线程执行任务

```
       void ensurePrestart() {
              int wc = workerCountOf(ctl.get());
               //创建工作线程,这里没有传入firstTask参数,因为上面先把任务扔到队列中去了,另外没用上maxPoolSize参数,所以最大线程数量在定时线程池中实际是没有用的
              if (wc < corePoolSize){
                  addWorker(null, true);
              } else if (wc == 0){
                  addWorker(null, false);
              }
          }
```     
##### ScheduledThreadPoolExecutor.ScheduledFutureTask

```
      public void run() {
                  // 是否重复执行
                  boolean periodic = isPeriodic();
                  // 线程池状态判断
                  if (!canRunInCurrentRunState(periodic)){
                      cancel(false);
                  }
                  //一次性任务，直接调用父类的run()方法，这个父类实际上是FutureTask
                  else if (!periodic){
                      ScheduledFutureTask.super.run();
                      //重复性任务，先调用父类的runAndReset()方法，这个父类也是FutureTask                                        
                  } else if (ScheduledFutureTask.super.runAndReset()) {
                      //设置下次执行的时间
                      setNextRunTime();
                      reExecutePeriodic(outerTask);
                  }
              }
 ```    
##### ScheduledFutureTask.reExecutePeriodic

```
       void reExecutePeriodic(RunnableScheduledFuture<?> task) {
               //线程池状态检查
              if (canRunInCurrentRunState(true)) {
                  //再次把任务放到任务队列中
                  super.getQueue().add(task);
                  //再次检查线程池状态
                  if (!canRunInCurrentRunState(true) && remove(task)){
                      task.cancel(false);
                  }else{
                      //保证工作线程足够
                      ensurePrestart();
                 }
              }
          }
```     
#### DelayedWorkQueue 延迟队列 内部类

```
       public RunnableScheduledFuture<?> take() throws InterruptedException {
                  final ReentrantLock lock = this.lock;
                   //加锁
                  lock.lockInterruptibly();
                  try {
                      for (;;) {
                          //堆顶任务
                          RunnableScheduledFuture<?> first = queue[0];
                          //如果队列为空,则等待
                          if (first == null){
                              available.await();
                          } else {
                              //还有多久到时间
                              long delay = first.getDelay(NANOSECONDS);
                               //如果小于等于0，说明这个任务到时间了，可以从队列中出队了
                              if (delay <= 0){
                                  //出队，然后堆化
                                  return finishPoll(first);
                              }
                               //还没有到时间
                              first = null; // don't retain ref while waiting
                              //如果前面有线程在等待,直接进入等待;任务是按顺序执行的。
                              if (leader != null){
                                  available.await();
                              }else {
                                  //当前线程作为leader
                                  Thread thisThread = Thread.currentThread();
                                  leader = thisThread;
                                  try {
                                       //等待上面计算的延时时间，再自动唤醒
                                      available.awaitNanos(delay);
                                  } finally {
                                       //唤醒后再次获得锁后把leader再置空
                                      if (leader == thisThread){
                                          leader = null;
                                      }
                                  }
                              }
                          }
                      }
                  } finally {
                      if (leader == null && queue[0] != null){
                          //相当于唤醒下一个等待的任务
                          available.signal();
                      }
                      //解锁
                      lock.unlock();
                  }
              }
     
```