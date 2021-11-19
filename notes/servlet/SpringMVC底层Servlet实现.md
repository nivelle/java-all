### springMVC 基于 DeferredResult 的异步处理

````java
public class SpringMVCServlet {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue(5));

    @PostMapping("mvcServlet")
    public DeferredResult<String> listPostDeferredResult() {
        DeferredResult<String> deferredResult = new DeferredResult<>();
        executor.execute(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("异步任务执行。。。完毕");
                deferredResult.setResult("ok");
            } catch (Exception e) {
                deferredResult.setErrorResult(e.getMessage());
            }
        });
        return deferredResult;
    }
}
````

1. tomcat 容器接收路径为 requestMapping的请求后，会分配一个容器线程来执行DispatcherServlet进行请求分配，请求被分配到匹配的的Controller
,然后执行该controller方法,该方法内创建了一个 DeferredResult 对象，然后把处理任务提交到了线程池进行处理，最后返回DeferredResult对象
   
2. springMVC 内部在 方法返回后会保存DeferredResult对象到内存队列或者列表，然后会调用request.startAsync()开启异步处理，并且调用DeferredResult对象的setResultHandler方法
，设置当异步结果产生后对结果进行重新路由的回调函数（逻辑在WebAsyncManager的startDeferredResultProcessing方法），**接着释放分配给当前请求的容器线程**，于此同时当前请求的 DispatcherServlet 和所有filters也执行完毕了，但是response流还是保持打开
   
3. 最终在业务线程池中执行的异步任务会产生一个结果，该结果会被设置到DeferredResult对象，然后设置的回调函数会被调用，接着Spring MVC 会分配请求结果回到servlet 容器继续完成处理，DispatcherServlet 被再次调用，使用返回的异步结果继续进行处理，最终把响应结果写会请求方。

### springMVC 基于 Callable 的异步处理

````java
public class SpringMVCServlet2 {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue(5));

    @PostMapping("mvcServlet2")
    public Callable<String> listPostDeferredResult() {
       return new Callable<String>() {
           @Override
           public String call() throws Exception {
               System.out.println("mvcServlet2 is done");
               return "ok mvcServlet2";
           }
       };
    }
}
````
1. tomcat 容器接收路径为 requestMapping的请求后，会分配一个容器线程来执行DispatcherServlet进行请求分配，请求被分配到匹配的的Controller
   ,然后执行该controller方法,返回一个Callable对象;

2. springMVC 内部在 方法返回后，调用request.startAsync()开启异步处理，然后提交Callable任务到内部线程池TaskExecutor(非容器线程)中进行异步执行(WebAsyncManager的startCallableProcessing方法内)，接着释放分配给当前请求的容器线程，于此同时当前请求的DispatcherServlet和所有filters也执行完毕了，但是 resposne 还是保持打开（因为Callable任务执行结果还没写会）

3. 最终在业务线程池中执行的异步任务会产生一个结果，然后spring MVC会分派请求结果到Servlet容器继续完成处理，DispatcherServlet被再次调用，使用返回的异步结果继续进行处理，最终响应结果写回请求方。

4. 这种方式下异步执行默认使用内部的SimpleAsyncTaskExecutor，对每个请求都会开启一个线程，并没有很好地服用线程，可以使用自定义的线程池来执行异步处理。

````java
  @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(8);
        configurer.setTaskExecutor(threadPoolTaskExecutor);
    }
```

----

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