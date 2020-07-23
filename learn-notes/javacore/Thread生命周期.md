## 线程的生命周期

### Thread.State
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

### 不管是synchronized锁还是基于AQS的锁，内部都是分成两个队列，一个是**同步队列（AQS的队列**，一个是**等待队列（Condition的队列）**；

### 对于内部调用了object.wait()/wait(timeout)或者condition.await()/await(timeout)方法，线程都是先进入等待队列，被notify()/signal()或者超时后，才会进入同步队列；

## synchronized

### 明确声明，BLOCKED状态只有线程处于synchronized的同步队列的时候才会有这个状态，其它任何情况都跟这个状态无关 ;==

（1）对于synchronized，线程执行synchronized的时候，如果立即获得了锁（没有进入同步队列），线程处于RUNNABLE状态；

（2）对于synchronized，线程执行synchronized的时候，如果无法获得锁（直接进入同步队列），线程处于BLOCKED状态；

（3）对于synchronized内部，调用了object.wait()之后线程处于WAITING状态（进入等待队列);

（4）对于synchronized内部，调用了object.wait(timeout)之后线程处于TIMED_WAITING状态（进入等待队列);

（5）对于synchronized内部，调用了object.wait()之后且被notify()了，如果线程立即获得了锁（也就是没有进入同步队列），线程处于RUNNABLE状态；

（6）对于synchronized内部，调用了object.wait(timeout)之后且被notify()了或者超时了，如果线程立即获得了锁（也就是没有进入同步队列），线程处于RUNNABLE状态；

（7）对于synchronized内部，调用了object.wait()之后且被 notify()了，如果线程无法获得锁（也就是进入了同步队列），线程处于BLOCKED状态；

（8）对于synchronized内部，调用了object.wait(timeout)之后且被notify()了或者超时了，如果线程无法获得锁（也就是进入了同步队列），线程处于BLOCKED状态；

## ReentrantLock

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