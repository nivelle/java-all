

## Executor

```
public interface Executor {
    ## 执行无返回值任务
    void execute(Runnable command);
}
```
## ExecutorService 是Executor的次级接口,扩展了一些功能; AbstractExecutorService 实现了 ExecutorService 接口,通过模版方法的方式提供了一些基础实现
```
public interface ExecutorService extends Executor {
    ## 关闭线程池，不再接受新任务，但已经提交的任务会执行完成
    void shutdown();
    ## 立即关闭线程池，尝试停止正在运行的任务，未执行的任务将不再执行;被迫停止及未执行的任务将以列表的形式返回
    List<Runnable> shutdownNow();
    ## 检查线程池是否已关闭
    boolean isShutdown();
    ## 检查线程池是否已终止，只有在shutdown()或shutdownNow()之后调用才有可能为true
    boolean isTerminated();
    ## 在指定时间内线程池达到终止状态了才会返回true
    boolean awaitTermination(long timeout, TimeUnit unit)throws InterruptedException;
    ## 执行有返回值的任务，任务的返回值为task.call()的结果
    <T> Future<T> submit(Callable<T> task);
    ##  执行有返回值的任务,任务的返回值为这里传入的result;当然只有当任务执行完成了调用get()时才会返回
    <T> Future<T> submit(Runnable task, T result);
    ## 执行有返回值的任务，任务的返回值为null;当然只有当任务执行完成了调用get()时才会返回
    Future<?> submit(Runnable task);
    ## 批量执行任务，只有当这些任务都完成了这个方法才会返回
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException;
    ## 在指定时间内批量执行任务，未执行完成的任务将被取消;这里的timeout是所有任务的总时间，不是单个任务的时间
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,long timeout, TimeUnit unit) throws InterruptedException;
    ## 返回任意一个已完成任务的执行结果，未执行完成的任务将被取消
    <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException;
    ## 在指定时间内如果有任务已完成，则返回任意一个已完成任务的执行结果，未执行完成的任务将被取消
    <T> T invokeAny(Collection<? extends Callable<T>> tasks,long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
}
```

## ScheduledExecutorService 对 ExecutorService接口的拓展,增加了一些 定时任务相关的方法

```
public interface ScheduledExecutorService extends ExecutorService {

    ## 在指定延时后执行一次,无返回值
    public ScheduledFuture<?> schedule(Runnable command,long delay, TimeUnit unit);

    ## 在指定延时后执行一次,有返回值
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,long delay, TimeUnit unit);

    ## 在指定延时后开始执行，并在之后以指定时间间隔重复执行（间隔不包含任务执行的时间),相当于之后的延时以任务开始计算
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,long initialDelay,long period,TimeUnit unit);

    ## 在指定延时后开始执行，并在之后以指定延时重复执行（间隔包含任务执行的时间),相当于之后的延时以任务结束计算
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,long initialDelay,long delay,TimeUnit unit);

}

```

## AbstractExecutorService

```



```



## 线程生命周期

```
    public enum State {
       
        NEW,//新建状态，线程还未开始
      
        RUNNABLE,//可运行状态，正在运行或者在等待系统资源，比如CPU

        BLOCKED,//阻塞状态，在等待一个监视器锁（也就是我们常说的synchronized）或者在调用了Object.wait()方法且被notify()之后也会进入BLOCKED状态

        WAITING,//等待状态，在调用了以下方法后进入此状态:1.Object.wait()无超时的方法后且未被notify()前，如果被notify()了会进入BLOCKED状态; 2.Thread.join()无超时的方法后;3.LockSupport.park()无超时的方法后

        TIMED_WAITING,// 超时等待状态，在调用了以下方法后会进入超时等待状态: 1. Thread.sleep()方法后;2.Object.wait(timeout)方法后且未到超时时间前,如果达到超时了或被notify()了会进入BLOCKED状态;3.Thread.join(timeout)方法后;4.LockSupport.parkNanos(nanos)方法后;5.LockSupport.parkUntil(deadline)方法后

        TERMINATED;//终止状态，线程已经执行完毕
    }

```


























