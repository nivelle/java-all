### 并发编程

Java SDK 并发包通过 Lock 和 Condition 两个接口来实现管程，其中 Lock 用于解决互斥问题，Condition 用于解决同步问题。

### Lock

#### 破坏不可抢占条件:

synchronized 没有办法解决。原因是 synchronized 申请资源的时候，如果申请不到，线程直接进入阻塞状态了，而线程进入阻塞状态，释放不了线程已经占有的资源

````

// 支持中断的API:能够响应中断
void lockInterruptibly() 
  throws InterruptedException;
// 支持超时的API:如果线程在一段时间之内没有获取到锁，不是进入阻塞状态，而是返回一个错误，那这个线程也有机会释放曾经持有的锁
boolean tryLock(long time, TimeUnit unit) 
  throws InterruptedException;
// 支持非阻塞获取锁的API:非阻塞地获取锁。如果尝试获取锁失败，并不进入阻塞状态，而是直接返回，那这个线程也有机会释放曾经持有的锁
boolean tryLock();

````

### Condition

Condition实现了管程模型里面的条件变量

#### 异步编程的支持

- 调用方创建一个子线程，在子线程中执行方法调用，这种调用我们称为异步调用

- 方法实现的时候，创建一个新的线程执行主要逻辑，主线程直接return,这种方法我们称为异步方法
