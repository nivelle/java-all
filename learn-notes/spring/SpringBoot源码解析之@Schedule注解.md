## @Schedule

第一步：spring在初始化bean后，通过“postProcessAfterInitialization”拦截到所有的用到“@Scheduled”注解的方法，并解析相应的的注解参数，放入“定时任务列表”等待后续处理；

```
public class ScheduledAnnotationBeanPostProcessor
		implements ScheduledTaskHolder, MergedBeanDefinitionPostProcessor, DestructionAwareBeanPostProcessor,
		Ordered, EmbeddedValueResolverAware, BeanNameAware, BeanFactoryAware, ApplicationContextAware,
		SmartInitializingSingleton, ApplicationListener<ContextRefreshedEvent>, DisposableBean 
		
		
	
	
## 在这个方法里解析注解，放入定时任务列表	
@Override
public Object postProcessAfterInitialization(Object bean, String beanName) {
		if (bean instanceof AopInfrastructureBean || bean instanceof TaskScheduler ||
				bean instanceof ScheduledExecutorService) {
			// Ignore AOP infrastructure such as scoped proxies.
			return bean;
		}

		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
		if (!this.nonAnnotatedClasses.contains(targetClass)) {
			Map<Method, Set<Scheduled>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
					(MethodIntrospector.MetadataLookup<Set<Scheduled>>) method -> {
						Set<Scheduled> scheduledMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(method, Scheduled.class, Schedules.class);
						return (!scheduledMethods.isEmpty() ? scheduledMethods : null);
					});
			if (annotatedMethods.isEmpty()) {
				this.nonAnnotatedClasses.add(targetClass);
				if (logger.isTraceEnabled()) {
					logger.trace("No @Scheduled annotations found on bean class: " + targetClass);
				}
			}
			else {
				// Non-empty set of methods
				annotatedMethods.forEach((method, scheduledMethods) ->
						scheduledMethods.forEach(scheduled -> processScheduled(scheduled, method, bean)));
				if (logger.isTraceEnabled()) {
					logger.trace(annotatedMethods.size() + " @Scheduled methods processed on bean '" + beanName +
							"': " + annotatedMethods);
				}
			}
		}
		return bean;
}

```
第二步: "定时任务列表"中统一执行相应的定时任务（任务为顺序执行，先执行cron，之后再执行fixedRate)

```
## ScheduledAnnotationBeanPostProcessor.processScheduled
protected void processScheduled(Scheduled scheduled, Method method, Object bean) {
		try {
			Runnable runnable = createRunnable(bean, method);
			boolean processedSchedule = false;
			String errorMessage =
					"Exactly one of the 'cron', 'fixedDelay(String)', or 'fixedRate(String)' attributes is required";

			Set<ScheduledTask> tasks = new LinkedHashSet<>(4);

			// Determine initial delay（初始化延期时间）
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

			// Check cron expression（获取Cron表达式）
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
						## 放入cron任务列表中（不执行）
						tasks.add(this.registrar.scheduleCronTask(new CronTask(runnable, new CronTrigger(cron, timeZone))));
					}
				}
			}

			// At this point we don't need to differentiate between initial delay set or not anymore
			if (initialDelay < 0) {
				initialDelay = 0;
			}

			// Check fixed delay 执行频率类型（long类型）
			long fixedDelay = scheduled.fixedDelay();
			if (fixedDelay >= 0) {
				Assert.isTrue(!processedSchedule, errorMessage);
				processedSchedule = true;
				## 放入FixedRate任务列表中（不执行）（registrar为ScheduledTaskRegistrar）
				tasks.add(this.registrar.scheduleFixedDelayTask(new FixedDelayTask(runnable, fixedDelay, initialDelay)));
			}
			## 执行频率类型（字符串类型，不接收参数计算如：600*20）
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
					## 放入FixedRate任务列表中（不执行）
					tasks.add(this.registrar.scheduleFixedDelayTask(new FixedDelayTask(runnable, fixedDelay, initialDelay)));
				}
			}

			// Check fixed rate
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
						throw new IllegalArgumentException(
								"Invalid fixedRateString value \"" + fixedRateString + "\" - cannot parse into long");
					}
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
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalStateException(
					"Encountered invalid @Scheduled method '" + method.getName() + "': " + ex.getMessage());
		}
	}

```

第三步:执行相应的定时任务。说明：定时任务先执行corn，判断定时任务的执行时间，计算出相应的下次执行时间，放入线程中，到相应的时间后进行执行。之后执行按“频率”（fixedRate）执行的定时任务，直到所有任务执行结束。
    
```
## ScheduledTaskRegistrar.scheduleTasks
protected void scheduleTasks() {
		if (this.taskScheduler == null) {
			this.localExecutor = Executors.newSingleThreadScheduledExecutor();
			this.taskScheduler = new ConcurrentTaskScheduler(this.localExecutor);
		}
	    ## 顺序执行相应的Trigger
		if (this.triggerTasks != null) {
			for (TriggerTask task : this.triggerTasks) {
				addScheduledTask(scheduleTriggerTask(task));
			}
		}
		## 顺序执行相应的Cron
		if (this.cronTasks != null) {
			for (CronTask task : this.cronTasks) {
				addScheduledTask(scheduleCronTask(task));
			}
		}
		## 顺序执行所有的“fixedRate”定时任务（无延迟，也就是说initialDelay参数为空），因为无延迟，所以定时任务会直接执行一次，执行任务完成后，会将下次执行任务的时间放入delayedExecute中等待下次执行
		if (this.fixedRateTasks != null) {
			for (IntervalTask task : this.fixedRateTasks) {
				addScheduledTask(scheduleFixedRateTask(task));
			}
		}
		## 顺序执行所有的“fixedRate”定时任务（有延迟，也就是说initialDelay参数不为空）
		if (this.fixedDelayTasks != null) {
			for (IntervalTask task : this.fixedDelayTasks) {
				addScheduledTask(scheduleFixedDelayTask(task));
			}
		}
	}
```

## 备注1:从上面的代码可以看出，如果多个定时任务定义的是同一个时间，那么也是顺序执行的，会根据程序加载Scheduled方法的先后来执行。

## 备注2:但是如果某个定时任务执行未完成此任务一直无法执行完成，无法设置下次任务执行时间，之后会导致此任务后面的所有定时任务无法继续执行，也就会出现所有的定时任务“失效”现象
        
