### Reference处理流程

![YIGs91.png](https://s1.ax1x.com/2020/05/19/YIGs91.png)

#### Reference 生命周期状态

- Active:每个引用的创建之初都是活动状态,直到下次 GC 的时候引用的强弱关系发生变化,同时不同的引用根据不同的策略改变状态;
 
 ````
  queue = ReferenceQueue with which instance is registered, or ReferenceQueue.NULL if it was not registered with a queue; next =null
 ````
- Pending:正准备加入引用链表;

````
  queue = ReferenceQueue with which instance is registered; next = this
````
- Enqueued:已经加入引用链表,相当于已经注册成功等待处理;
````
  queue = ReferenceQueue.ENQUEUED; next = Following instance in queue, or this if at end of list.
````
- Inactive:所有的引用对象的终点,可回收状态;

````
  queue = ReferenceQueue.NULL; next = this
````
#### 工作原理

1. Reference里有个静态字段pending，同时还通过静态代码块启动了Reference-handler thread。

2. 当一个Reference的referent被回收时，垃圾回收器会把reference添加到pending这个链表里，

3. 然后Reference-handler thread不断的读取pending中的reference，把它加入到对应的ReferenceQueue中

##### JVM不仅仅只会考虑当前内存情况，还会考虑软引用所指向的referent最近使用情况和创建时间来综合决定是否回收该referent。

Hotspot在gc时会根据两个标准来回收：

- 根据SoftReference引用实例的timestamp(每次调用softReference.get()会自动更新该字段，把最近一次垃圾回收时间赋值给timestamp,见源码)

- 当前JVM heap的内存剩余(free_heap)情况

计算的规则是：

1. free_heap 表示当前堆剩余的内存，单位是MB

2. interval 表示最近一次GC’s clock 和 当前我们要判断的softReference的timestamp 差值

3. ms_per_mb:通过-XX:SoftRefLRUPolicyMSPerMB来设定

那么判断依据就是: interval <= freeheap * ms_per_mb,如果为true,则保留，false则进行对象清除


softReferences will always be kept for at least one GC after their last access。_ 因为 只要调用一次，那么clock和timestamp的值就会一样，clock-timestamp则为0，一定小于等于free_heap * ms_per_mb













#### 核心属性

1. private T referent;

````
1. 引用指向的对象，即需要Reference包装的对象;
````
  
2. volatile ReferenceQueue<? super T> queue;

````
1. 虽然ReferenceQueue的名字里面有队列，但是它的内部却没有包含任何队列和链表的结构;

2. 他的内部封装了单向链表的添加,删除和遍历等操作，实际作用相当于事件监听器；

````

3. volatile Reference next;//引用单向链表

````
1. 单向链表，由 JVM 维护;在 GC 标记的时候,当引用强弱关系达到一定条件时,由 JVM 添加;需要注意的是这个字段是 transient 修饰的，
  
2. 但是 Reference 类声明的时候却没有实现 Serializable 接口，这是因为 Reference 子类的子类可能实现 Serializable 接口，另外一般情况下也不建议实现 Serializable 接口；
   
````
4. transient private Reference<T> discovered; 

5. private static Reference<Object> pending = null;//表示正在排队等待入队的引用


#### 核心方法

##### 初始化Reference在被加载的时候就会触发static里的代码执行, 就会创建Reference Handler线程并启动.

```
 static {
        //当前线程的线程组
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        //循环找到当前线程组的最高父线程组，也即System线程组
        for (ThreadGroup tgn = tg;tgn != null;tg = tgn, tgn = tg.getParent());
        //在层级最高的线程组System 线程组添加名字为"Reference Handler线程组"
        Thread handler = new ReferenceHandler(tg, "Reference Handler");
        /* If there were a special system-only priority greater than
         * MAX_PRIORITY, it would be used here
         */
        //最高优先级
        handler.setPriority(Thread.MAX_PRIORITY);
        //设置为守护线程
        handler.setDaemon(true);
        //启动该线程
        handler.start();

        // 通过JVM推测获取堆栈信息
        SharedSecrets.setJavaLangRefAccess(new JavaLangRefAccess() {
            @Override
            public boolean tryHandlePendingReference() {
                //在死循环中调用了Reference的tryHandlePending来清理无效的Reference
                return tryHandlePending(false);
            }
        });
    }

```
#### SharedSecrets 相当于holder保存了一些对象引用,并提供了set/get方法

#### 启动后的Reference Handler线程的任务

```
private static class ReferenceHandler extends Thread {

        //采用反射的方式触发类的加载
        private static void ensureClassInitialized(Class<?> clazz) {
            try {
                Class.forName(clazz.getName(), true, clazz.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw (Error) new NoClassDefFoundError(e.getMessage()).initCause(e);
            }
        }

        static {
            // pre-load and initialize InterruptedException and Cleaner classes
            // so that we don't get into trouble later in the run loop if there's
            // memory shortage while loading/initializing them lazily.
            //加载类InterruptedException 和 Cleaner
            ensureClassInitialized(InterruptedException.class);
            ensureClassInitialized(Cleaner.class);
        }

        ReferenceHandler(ThreadGroup g, String name) {
            super(g, name);
        }

        public void run() {
            while (true) {
                tryHandlePending(true);
            }
        }
    }
```
#### tryHandlePending,清理无效的reference

1. Reference里有个静态字段 pending,同时还通过静态代码块启动了Reference-handler thread。

2. 当一个Reference的referent被回收时,垃圾回收器会把 reference 添加到pending这个链表里,

3. 然后Reference-handler thread不断的读取pending中的reference，把它加入到对应的 ReferenceQueue 中

```
static boolean tryHandlePending(boolean waitForNotify) {
        Reference<Object> r;
        Cleaner c;
        try {
            synchronized (lock) {
                // pending list不为null,说明有需要处理的引用
                if (pending != null) {
                    r = pending;
                    // 'instanceof' might throw OutOfMemoryError sometimes
                    // so do this before un-linking 'r' from the 'pending' chain...
                    c = r instanceof Cleaner ? (Cleaner) r : null;
                    // unlink 'r' from 'pending' chain
                    // 从 pending list中删除r
                    pending = r.discovered;
                    r.discovered = null;
                } else {
                    // The waiting on the lock may cause an OutOfMemoryError
                    // because it may try to allocate exception objects.
                    // 没有需要处理的引用, 就wait, 这个应该会被JVM给notify. 这个地方可能找出OOM，因为申请异常对象
                    if (waitForNotify) {
                        lock.wait();
                    }
                    // retry if waited
                    return waitForNotify;
                }
            }
        } catch (OutOfMemoryError x) {
            // Give other threads CPU time so they hopefully drop some live references
            // and GC reclaims【回收】 some space.
            // Also prevent【防止】 CPU intensive【密集】 spinning【旋转】 in case 'r instanceof Cleaner' above
            // persistently throws OOME for some time...
            Thread.yield();
            // retry
            return true;
        } catch (InterruptedException x) {
            // retry
            return true;
        }
        // Fast path for cleaners
        if (c != null) {
            c.clean();
            return true;
        }
        ReferenceQueue<? super Object> q = r.queue;
        if (q != ReferenceQueue.NULL) q.enqueue(r);
        return true;
    }

```

#### ReferenceQueue  ReferenceQueue用一个链表来维护队列里的Reference

##### enqueue 入队操作

```
boolean enqueue(Reference<? extends T> r) {
        synchronized (lock) {
            // Check that since getting the lock this reference hasn't already been
            // enqueued (and even then removed)
            ReferenceQueue<?> queue = r.queue;
            //校验下r带的队列是自己, 并且已经enqueue的不会重复enqueue
            if ((queue == NULL) || (queue == ENQUEUED)) {
                return false;
            }
            assert queue == this;
            //修改Reference的queue为ENQUEUED, 代表已经入队了
            r.queue = ENQUEUED;
            //链表的头插法,将当前的引用入队,并更新head的值和队列的长度
            r.next = (head == null) ? r : head;
            head = r;
            queueLength++;
            if (r instanceof FinalReference) {
                sun.misc.VM.addFinalRefCount(1);
            }
            lock.notifyAll();
            return true;
        }
    }
```

##### remove 移除操作

```
public Reference<? extends T> remove(long timeout)throws IllegalArgumentException, InterruptedException
        {
        if (timeout < 0) {
            throw new IllegalArgumentException("Negative timeout value");
        }
        synchronized (lock) {
            // 从队列中取出一个元素
            Reference<? extends T> r = reallyPoll();
            //如果不为空则直接返回
            if (r != null) return r;
            long start = (timeout == 0) ? 0 : System.nanoTime();
            for (;;) {
                //否则等待，由enqueue时notify唤醒 
                lock.wait(timeout);
                //出队操作
                r = reallyPoll();
                if (r != null) return r;
                if (timeout != 0) {
                    long end = System.nanoTime();
                    timeout -= (end - start) / 1000_000;
                    if (timeout <= 0) return null;
                    start = end;
                }
            }
        }
    }

```
##### reallyPoll 出队相关操作

```
private Reference<? extends T> reallyPoll() {
        //获取头元素 
        Reference<? extends T> r = head;
        if (r != null) {
            @SuppressWarnings("unchecked")
            Reference<? extends T> rn = r.next;
            //因为这个队列的尾节点的next总是指向自己, 这里判断如果是尾节点出队列, head置为null
            //新的头节点是null或者r的下一个节点
            head = (rn == r) ? null : rn;
            r.queue = NULL;
            // 对于出队列的引用, 他的next也是指向自己的
            r.next = r;
            queueLength--;
            if (r instanceof FinalReference) {
                sun.misc.VM.addFinalRefCount(-1);
            }
            return r;
        }
        return null;
    }
``` 