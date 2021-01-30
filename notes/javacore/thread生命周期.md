### 线程

java语言里的线程本质上就是操作系统的线程，她们是一一对应的


### 线程的生命周期

#### Thread.State

-  NEW
````
新建状态，线程还未开始
````

- RUNNABLE

````
可运行状态，正在运行或者在等待系统资源，比如CPU
````

- BLOCKED

````
1. 阻塞状态，在等待一个监视器锁（也就是我们常说的synchronized）

2. 在调用了Object.wait()方法且被notify()之后也会进入BLOCKED状态
````

- WAITING

````
等待状态，在调用了以下方法后进入此状态:
 1.Object.wait()无超时的方法后且未被notify()前，如果被notify()了会进入BLOCKED状态;
 2.Thread.join()无超时的方法后;
 3.LockSupport.park()无超时的方法后
````

- TIMED_WAITING

````
 超时等待状态，在调用了以下方法后会进入超时等待状态: 
 1. Thread.sleep(timeout)方法后; 
 2.Object.wait(timeout)方法后且未到超时时间前,如果达到超时了或被notify()了会进入BLOCKED状态;
 3.Thread.join(timeout)方法后;
 4.LockSupport.parkNanos(nanos)方法后;
 5.LockSupport.parkUntil(deadline)方法后
````

- TERMINATED 终止状态，线程已经执行完毕

#### 状态转换

- RUNNABLE 与 BLOCKED 的状态转换

``````
1. 线程等待synchronized的隐式锁

2. synchronized 修饰的方法、代码块同一时刻只允许一个线程执行，其他线程只能等待，这种情况下，等待的线程就会从 RUNNABLE 转换到 BLOCKED 状态。

3. 当等待的线程获得 synchronized 隐式锁时，就又会从 BLOCKED 转换到 RUNNABLE 状态

``````

- RUNNABLE 与 WAITING 的状态转换

````
1. 获得 synchronized 隐式锁的线程，调用无参数的 Object.wait() 方法

2. 调用无参数的 Thread.join() 方法。其中的 join() 是一种线程同步方法，例如有一个线程对象 thread A，当调用 A.join() 的时候，执行这条语句的线程会等待 thread A 执行完，而等待中的这个线程，其状态会从 RUNNABLE 转换到 WAITING。当线程 thread A 执行完，原来等待它的线程又会从 WAITING 状态转换到 RUNNABLE

3. 调用 LockSupport.park() 方法，当前线程会阻塞，线程的状态会从 RUNNABLE 转换到 WAITING。调用 LockSupport.unpark(Thread thread) 可唤醒目标线程，目标线程的状态又会从 WAITING 状态转换到 RUNNABLE。
````

- RUNNABLE 与 TIMED_WAITING 的状态转换

````

1. 调用带超时参数的 Thread.sleep(long millis) 方法；

2. 获得 synchronized 隐式锁的线程，调用带超时参数的 Object.wait(long timeout) 方法；

3. 调用带超时参数的 Thread.join(long millis) 方法；

4. 调用带超时参数的 LockSupport.parkNanos(Object blocker, long deadline) 方法；

5. 调用带超时参数的 LockSupport.parkUntil(long deadline) 方法

````

-  从 NEW 到 RUNNABLE 状态

````
NEW 状态的线程，不会被操作系统调度，因此不会执行。Java 线程要执行，就必须转换到 RUNNABLE 状态。从 NEW 状态转换到 RUNNABLE 状态 只要调用线程对象的 start() 方法就可以了

````

- 从 RUNNABLE 到 TERMINATED 状态

````
1. 线程执行完 run() 方法后，会自动转换到 TERMINATED 状态，当然如果执行 run() 方法的时候异常抛出，也会导致线程终止

````























##### 调用阻塞式API

- 在操作系统层面，线程是会转换到休眠状态的，但是在 JVM 层面，Java 线程的状态不会发生变化，也就是说 Java 线程的状态会依然保持 RUNNABLE 状态。JVM 层面并不关心操作系统调度相关的状态，因为在 JVM 看来，等待 CPU 使用权（操作系统层面此时处于可执行状态）与等待 I/O（操作系统层面此时处于休眠状态）没有区别，都是在等待某个资源，所以都归入了 RUNNABLE 状态

- 平时所谓的 Java 在调用阻塞式 API 时，线程会阻塞，指的是操作系统线程的状态，并不是 Java 线程的状态


#### 在操作系统层面，Java 线程中的 BLOCKED、WAITING、TIMED_WAITING 是一种状态，也即休眠，只要java线程处于这三种状态之一，那么这个线程就永远没有CPU使用权 

### 双队列
 
- 不管是synchronized锁还是基于AQS的锁，内部都是分成两个队列，一个是**同步队列（AQS的队列**，一个是**等待队列（Condition的队列）**；

- 对于内部调用了object.wait()/wait(timeout)或者condition.await()/await(timeout)方法，线程都是先进入等待队列，被notify()/signal()或者超时后，才会进入同步队列；

### synchronized

#### 明确声明，BLOCKED状态只有线程处于synchronized的同步队列的时候才会有这个状态，其它任何情况都跟这个状态无关 ;==

（1）对于synchronized，线程执行synchronized的时候，如果立即获得了锁（没有进入同步队列），线程处于RUNNABLE状态；

（2）对于synchronized，线程执行synchronized的时候，如果无法获得锁（直接进入同步队列），线程处于BLOCKED状态；

（3）对于synchronized内部，调用了object.wait()之后线程处于WAITING状态（进入等待队列);

（4）对于synchronized内部，调用了object.wait(timeout)之后线程处于TIMED_WAITING状态（进入等待队列);

（5）对于synchronized内部，调用了object.wait()之后且被notify()了，如果线程立即获得了锁（也就是没有进入同步队列），线程处于RUNNABLE状态；

（6）对于synchronized内部，调用了object.wait(timeout)之后且被notify()了或者超时了，如果线程立即获得了锁（也就是没有进入同步队列），线程处于RUNNABLE状态；

（7）对于synchronized内部，调用了object.wait()之后且被 notify()了，如果线程无法获得锁（也就是进入了同步队列），线程处于BLOCKED状态；

（8）对于synchronized内部，调用了object.wait(timeout)之后且被notify()了或者超时了，如果线程无法获得锁（也就是进入了同步队列），线程处于BLOCKED状态；

#### ReentrantLock

（1）对于重入锁，线程执行lock.lock()的时候，如果立即获得了锁（没有进入同步队列），线程处于RUNNABLE状态；

（2）对于重入锁，线程执行lock.lock()的时候，如果无法获得锁（直接进入同步队列），线程处于WAITING状态;

（3）对于重入锁内部，调用了condition.await()之后线程处于WAITING状态（进入等待队列);

（4）对于重入锁内部，调用了condition.await(timeout)之后线程处于TIMED_WAITING状态（进入等待队列);

（5）对于重入锁内部，调用了condition.await()之后且被signal()了，如果线程立即获得了锁（也就是没有进入同步队列），线程处于RUNNABLE状态；

（6）对于重入锁内部，调用了condition.await(timeout)之后且被signal()或则超市了，如果线程立即获得了锁（也就是没有进入同步队列），线程处于RUNNABLE状态；

（7）对于重入锁内部，调用了condition.await()之后且被signal()了，如果线程无法获得锁（也就是进入了同步队列），线程处于WAITING状态；

（8）对于重入锁内部，调用了condition.await(timeout)之后且被signal()了或者超时了，如果线程无法获得锁（也就是进入了同步队列），线程处于WAITING状态；

（9）对于重入锁，如果内部调用了condition.await()之后且被signal()之后依然无法获取锁的，其实经历了两次WAITING状态的切换，一次是在等待队列，一次是在同步队列；

（10）对于重入锁，如果内部调用了condition.await(timeout)之后且被signal()或超时了的，状态会有一个从TIMED_WAITING切换到WAITING的过程，也就是从等待队列进入到同步队列；