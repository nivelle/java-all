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