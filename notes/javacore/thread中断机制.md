

### 定义

Java中断机制是一种协作机制，也就是说通过中断并不能直接终止另一个线程，而需要被中断的线程自己处理中断。

Java中断模型也是这么简单，每个线程对象里都有一个boolean类型的标识代表着是否有中断请求（该请求可以来自所有线程，包括被中断的线程本身）。例如，当线程t1想中断线程t2，只需要在线程t1中将线程t2对象的中断标识置为true，然后线程2可以选择在合适的时候处理该中断请求，甚至可以不理会该请求，就像这个线程没有被中断一样。

java.lang.Thread类提供了几个方法来操作这个中断状态，这些方法包括：



方法 | 说明
--- | ---
public static boolean interrupted | 测试当前线程是否已经中断。线程的中断状态 由该方法清除。换句话说，如果连续两次调用该方法，则第二次调用肯定返回false（在第一次调用已清除了其中断状态之后，且第二次调用检验完中断状态前，当前线程再次中断的情况除外）。
public boolean isInterrupted() | 测试线程是否已经中断。线程的中断状态不受该方法的影响。
public void interrupt() | 中断线程。

#### 处理时机

显然，作为一种协作机制，不会强求被中断线程一定要在某个点进行处理。实际上，被中断线程只需在合适的时候处理即可，如果没有合适的时间点，甚至可以不处理，这时候在任务处理层面，就跟没有调用中断方法一样。“合适的时候”与线程正在处理的业务逻辑紧密相关，例如，每次迭代的时候，进入一个可能阻塞且无法中断的方法之前等，但多半不会出现在某个临界区更新另一个对象状态的时候，因为这可能会导致对象处于不一致状态。


#### 处理方式

##### 中断状态的管理

一般说来，当可能阻塞的方法声明中有抛出InterruptedException则暗示该方法是可中断的，如BlockingQueue#put、BlockingQueue#take、Object#wait、Thread#sleep等，如果程序捕获到这些可中断的阻塞方法抛出的InterruptedException或检测到中断后，这些中断信息该如何处理？一般有以下两个通用原则：

- 如果遇到的是可中断的阻塞方法抛出InterruptedException，可以继续向方法调用栈的上层抛出该异常，如果是检测到中断，则可清除中断状态并抛出InterruptedException，使当前方法也成为一个可中断的方法。
- 若有时候不太方便在方法上抛出InterruptedException，比如要实现的某个接口中的方法签名上没有throws InterruptedException，这时就可以捕获可中断方法的InterruptedException并通过Thread.currentThread.interrupt()来重新设置中断状态。如果是检测并清除了中断状态，亦是如此。


##### 中断的响应

程序里发现中断后该怎么响应？这就得视实际情况而定了。有些程序可能一检测到中断就立马将线程终止，有些可能是退出当前执行的任务，继续执行下一个任务……作为一种协作机制，这要与中断方协商好，当调用interrupt会发生些什么都是事先知道的，如做一些事务回滚操作，一些清理工作，一些补偿操作等。若不确定调用某个线程的interrupt后该线程会做出什么样的响应，那就不应当中断该线程。

#### 中断的使用

通常,中断的使用场景有以下几个:

- 点击某个桌面应用中的取消按钮时；
- 某个操作超过了一定的执行时间限制需要中止时；
- 多个线程做相同的事情，只要一个线程成功其它线程都可以取消时；
- 一组线程中的一个或多个出现错误导致整组都无法继续时；
- 当一个应用或服务需要停止时。


### 中断的应用

当一个方法后面声明可能会抛出InterruptedException 异常时，说明该方法是可能会花一点时间，但是可以取消的方法。

抛出InterrcptedException的代表方法有:

- java.lang.Object 类的 wait 方法
- java.lang.Thread 类的 sleep 方法
- java.lang.Thread 类的 join 方法

##### 需要花点时间:

执行wait方法的线程，会进入等待区等待被notify/notify All。在等待期间，线程不会活动。
执行sleep方法的线程，会暂停执行参数内所设置的时间。
执行join方法的线程，会等待到指定的线程结束为止。

##### 可以取消的方法:

因为需要花时间的操作会降低程序的响应性，所以可能会取消/中途放弃执行这个方法。
这里主要是通过interrupt方法来取消。


1. sleep方法与interrupt方法

interrupt方法是Thread类的实例方法，在执行的时候并不需要获取Thread实例的锁定，任何线程在任何时刻，都可以通过线程实例来调用其他线程的interrupt方法。
当在sleep中的线程被调用interrupt方法时，就会放弃暂停的状态，并抛出InterruptedException异常，这样一来，线程的控制权就交给了捕捉这个异常的catch块了。

2. wait方法和interrupt方法

当线程调用wait方法后，线程在进入等待区时，会把锁定解除。当对wait中的线程调用interrupt方法时，会先重新获取锁定，再抛出InterruptedException异常，获取锁定之前，无法抛出InterruptedException异常。

3. join方法和interrupt方法

当线程以join方法等待其他线程结束时，一样可以使用interrupt方法取消。因为join方法不需要获取锁定，故而与sleep一样，会马上跳到catch程序块



interrupt方法其实只是改变了中断状态而已。
而sleep、wait和join这些方法的内部会不断的检查中断状态的值，从而自己抛出InterruptEdException。
所以，如果在线程进行其他处理时，调用了它的interrupt方法，线程也不会抛出InterruptedException的，只有当线程走到了sleep, wait, join这些方法的时候，才会抛出InterruptedException。若是没有调用sleep, wait, join这些方法，或者没有在线程里自己检查中断状态，自己抛出InterruptedException，那InterruptedException是不会抛出来的。



[转载至http://ifeve.com/java-interrupt-mechanism/](http://ifeve.com/java-interrupt-mechanism/)

[转载至http://blog.csdn.net/derekjiang/article/details/4845757](http://blog.csdn.net/derekjiang/article/details/4845757)
