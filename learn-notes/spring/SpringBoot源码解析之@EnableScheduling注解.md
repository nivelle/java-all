## @EnableScheduling

### 概述

- 注解@EnableScheduling导入SchedulingConfiguration;

- SchedulingConfiguration定义基础设施bean ScheduledAnnotationBeanPostProcessor scheduledAnnotationProcessor;

- ScheduledAnnotationBeanPostProcessor在容器启动时做如下事情:

  (1) 记所有使用@Scheduled注解的bean方法到一个ScheduledTaskRegistrar，供调度任务执行器TaskScheduler执行
  
  (2) 为ScheduledTaskRegistrar指定任务执行器TaskScheduler,该任务执行器来自容器中的bean TaskScheduler/ScheduledExecutorService(如果不指定,ScheduledTaskRegistrar自己会本地创建一个ConcurrentTaskScheduler)
      
  (3) 告诉ScheduledTaskRegistrar将所注册的调度任务,也就是使用@Scheduled注解的bean方法,调度到任务执行器TaskScheduler执行

## 核心源码


### @EnableSchedule

```
@Import(SchedulingConfiguration.class)
public @interface EnableScheduling {

}

```

### SchedulingConfiguration

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

### ScheduledAnnotationBeanPostProcessor#postProcessAfterInitialization检测处理每个@Scheduled注解的方法ScheduledAnnotationBeanPostProcessor实现了DestructionAwareBeanPostProcessor,BeanPostProcessor等接口。
### 作为一个BeanPostProcessor,ScheduledAnnotationBeanPostProcessor会针对每个bean的创建，在bean生命周期方法#postProcessAfterInitialization中，扫描该bean中使用了注解@Scheduled的方法.

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
          ## 注意 : 某个方法上可能同时使用多个注解  @Scheduled ，所以以下 annotatedMethods 的每个 Entry 是 一个方法对应一个 @cheduled 集合
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

### processScheduled()

rocessScheduled处理方法上的每个@Scheduled注解，生成一个ScheduledTask并登记到this.scheduledTasks。 

this.scheduledTasks 数据结构为Map; key:是一个对象，其类就是含有方法使用了注解@Scheduled的类 value:是一个ScheduledTask集合,方法上的每个注解@Scheduled对应一个ScheduledTask;

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
            ##用于记录当前 @Scheduled 注解是否已经被处理，初始化为 false  
			boolean processedSchedule = false;
			String errorMessage ="Exactly one of the 'cron', 'fixedDelay(String)', or 'fixedRate(String)' attributes is required";
           ##用于保存针对当前 @Scheduled  注解生成的 ScheduledTask,该方法完成时，该集合内元素数量通常为 1
			Set<ScheduledTask> tasks = new LinkedHashSet<>(4);
			// Determine initial delay
           ## 确定 initial delay 属性 ： 基于注解属性 initialDelay 或者  initialDelayString 分析得到,二者只能使用其中之一
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
						throw new IllegalArgumentException(
					"Invalid initialDelayString value \"" + initialDelayString + "\" - cannot parse into long");
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
	                    ## 包装成为一个 CronTask 
						tasks.add(this.registrar.scheduleCronTask(
							new CronTask(runnable, new CronTrigger(cron, timeZone))));
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
				tasks.add(this.registrar.scheduleFixedDelayTask(
					new FixedDelayTask(runnable, fixedDelay, initialDelay)));
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
				tasks.add(this.registrar.scheduleFixedRateTask(
					new FixedRateTask(runnable, fixedRate, initialDelay)));
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
						throw new IllegalArgumentException(
							"Invalid fixedRateString value \"" + fixedRateString + "\" - cannot parse into long");
					}
					## 包装成为一个 FixedRateTask       
					tasks.add(this.registrar.scheduleFixedRateTask(
						new FixedRateTask(runnable, fixedRate, initialDelay)));
				}
			}

			// Check whether we had any attribute set
			Assert.isTrue(processedSchedule, errorMessage);

			// Finally register the scheduled tasks
			synchronized (this.scheduledTasks) {
				Set<ScheduledTask> regTasks = 
					this.scheduledTasks.computeIfAbsent(bean, key -> new LinkedHashSet<>(4));
				regTasks.addAll(tasks);
			}
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalStateException(
					"Encountered invalid @Scheduled method '" + method.getName() + "': " + ex.getMessage());
		}
	}


```

### 经过ScheduledAnnotationBeanPostProcessor以上这些处理，每个bean中所包含的@Scheduled注解都被发现了，这样的每条信息最终对应生成一个ScheduledTask,
### 该ScheduledTask会被 ScheduledTaskRegistrar registrar登记调度。这意味着该ScheduledTask从此刻起在程序运行期间就会按照@Scheduled注解所设定的时间点被执行。

### 备注1:从上面的代码可以看出，如果多个定时任务定义的是同一个时间，那么也是顺序执行的，会根据程序加载Scheduled方法的先后来执行。

### 备注2:但是如果某个定时任务执行未完成此任务一直无法执行完成，无法设置下次任务执行时间，之后会导致此任务后面的所有定时任务无法继续执行，也就会出现所有的定时任务“失效”现象
        
