### spring 中对 TaskExecutor的实现

TaskExecutor 接口与java.util.concurrent.Executor是等价的

````java
public interface TaskExecutor{
   void executoe(Runable task);
}
````

### @Async 注解的实现

````java
public Object invoke(final MethodInvocation invocation) throws Throwable {
        //被代理的目标对象
		Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
		//被代理的目标方法
		Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
		final Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
		//判断使用什么代理执行器执行代理方法
		AsyncTaskExecutor executor = determineAsyncExecutor(userDeclaredMethod);
		if (executor == null) {
			throw new IllegalStateException(
					"No executor specified and no default executor set on AsyncExecutionInterceptor either");
		}
        //使用Callable 包装要执行的方法
		Callable<Object> task = () -> {
			try {
				Object result = invocation.proceed();
				if (result instanceof Future) {
					return ((Future<?>) result).get();
				}
			}
			catch (ExecutionException ex) {
				handleError(ex.getCause(), userDeclaredMethod, invocation.getArguments());
			}
			catch (Throwable ex) {
				handleError(ex, userDeclaredMethod, invocation.getArguments());
			}
			return null;
		};
        //提交包装的Callable任务到执行器执行
		return doSubmit(task, executor, invocation.getMethod().getReturnType());
	}

````

#### determineAsyncExecutor 判断使用什么执行器

````java

protected AsyncTaskExecutor determineAsyncExecutor(Method method) {
        //获取方法对应的执行器
		AsyncTaskExecutor executor = this.executors.get(method);
		//不存在则按照规则寻找
		if (executor == null) {
			Executor targetExecutor;
			String qualifier = getExecutorQualifier(method);
			//如果@Async 指定了执行器名字
			if (StringUtils.hasLength(qualifier)) {
				targetExecutor = findQualifiedExecutor(this.beanFactory, qualifier);
			}
			else {
			    //获取默认的执行器
				targetExecutor = this.defaultExecutor.get();
			}
			if (targetExecutor == null) {
				return null;
			}
			//添加执行器到默认缓存
			executor = (targetExecutor instanceof AsyncListenableTaskExecutor ?
					(AsyncListenableTaskExecutor) targetExecutor : new TaskExecutorAdapter(targetExecutor));
			this.executors.put(method, executor);
		}
		//返回查找到的执行器
		return executor;
	}


````

#### 提交任务

````java
protected Object doSubmit(Callable<Object> task, AsyncTaskExecutor executor, Class<?> returnType) {
        //判断返回方法返回值是否是CompletableFuture类型或者其子类型
		if (CompletableFuture.class.isAssignableFrom(returnType)) {
			return CompletableFuture.supplyAsync(() -> {
				try {
					return task.call();
				}
				catch (Throwable ex) {
					throw new CompletionException(ex);
				}
			}, executor);
		}
		//判断返回方法返回值是否是ListenableFuture类型或者其子类型
		else if (ListenableFuture.class.isAssignableFrom(returnType)) {
			return ((AsyncListenableTaskExecutor) executor).submitListenable(task);
		}
	   //判断返回方法返回值是否是Future类型或者其子类型
		else if (Future.class.isAssignableFrom(returnType)) {
			return executor.submit(task);
		}
		else {
		    //其他情况没有返回值
			executor.submit(task);
			return null;
		}
	}

````