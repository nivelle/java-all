### thread 

### Thread特性分析

#### 守护线程Daemon

- 定性：支持性线程，主要用于程序中后台调度以及支持性工作。

- 当JVM中不存在Daemon线程时，JVM将会退出。

- 将一个线程设定为Daemon的方法： 
  
  1. 调用Thread.setDaemon(true)。
  2. Daemon属性的设定只能在启动线程前设置，启动线程后不能设置。
    
- JVM退出时Daemon线程中的finally块中的代码不一定会执行。因此不能依靠finally块中的内容来确保执行关闭或清理资源的逻辑。

- 当JVM启动时,通常会有唯一的一个非守护线程(这一线程用于调用指定类的main()方法).

####2种方式创建一个可执行线程

- 定义一个继承Thread类的子类.子类可覆写父类的run()方法.
- 使类实现Runnable接口
#### 线程名字
- 每一个线程都有一个用于目的标识的名字
- 多个线程可以有相同的名字
- 线程名类型为volatile类型，可更改且线程可见
#### 线程ID
- 一个long类型的正数,在线程被创建时就有
- 在其生命周期内都不会更改且独一无二
- 当线程终止时,则此线程的ID会被重复使用
#### 线程优先级
- 默认优先级：5
- 最高优先级：10
- 最低优先级：1
#### 线程状态6种
- NEW：线程还未开始,只是进行了一些线程创建的初始化操作,但未调用start()方法.
- RUNNABLE：线程在JVM里面处于运行状态(这里就绪和运行同属于运行).
- BLOCKED：线程正在等待一个监视器锁,处于阻塞状态.
- WAITING：一个线程在等待另一个线程的特定操作(通知or中断),这种等待是无限期的.
- TIMED_WAITING：一个线程在等待另一个线程的特定操作,这种等待是有时间限制的.一旦超时则线程自行返回.
- TERMINATED：线程已退出.表示线程已经执行完毕.
#### 浅拷贝
- 不支持
- 替代操作： 构造一个新的线程。

### 类、方法、字段分析
   
#### 实现接口和继承类
  
- 只实现了一个接口Runnable 
  
#### threadLocals变量
   
- 类型为：ThreadLocal.ThreadLocalMap
- 功能：此线程的本地变量值.此map由ThreadLocal类进行维护,因为这个类在ThreadLocal中是包级私有的.
- ThreadLocalMap：一个用于维护线程本地变量的hashmap,此hashmap的key引用类型为弱引用,这是为了支持大且长期存活的使用方法.
#### inheritableThreadLocals变量
   
- 类型：ThreadLocal.ThreadLocalMap
- 功能：和此线程相关的由继承得到的本地变量值
   
#### public static native void yield()方法
- 功能： 提示线程调度器当前线程愿意放弃当前CPU的使用。当然调度器可以忽略这个提示
- 设计目的： 让出CPU是一种启发式的尝试，以改善线程之间的相对进展，否则将过度利用CPU。
- 使用场景： 此方法很少有适用的场景.它可以用用于debug或者test,通过跟踪条件可以重现bug
   
#### public final synchronized void setName(String name)方法
- 功能
- 设定线程名 注意这是一个同步方法

#### 3个join方法
- 功能：等待直到线程终止
   
##### public final synchronized void join(long millis)
- 最多等待参数millis(ms)时长当前线程就会死亡.参数为0时则要持续等待 ；此方法为同步方法
##### public final synchronized void join(long millis, int nanos)
- 等待时间单位为纳秒,其它解释都和上面方法一样 ；此方法为同步方法
##### public final void join() throws InterruptedException

##### toString()格式
- 线程名+优先级+所属组别

##### @CallerSensitive注解
- 功能： 这个注解是为了堵住漏洞用的
- 原理： 曾经有黑客通过构造双重反射来提升权限,原理是当时反射只检查固定深度的调用者的类，看它有没有特权.使用CallerSensitive后，getCallerClass不再用固定深度去寻找actual caller（“我”），而是把所有跟反射相关的接口方法都标注上CallerSensitive，搜索时凡看到该注解都直接跳过，这样就有效解决了这类的黑客问题.


### 核心源码
````
package sourcecode.analysis;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.LockSupport;
import sun.nio.ch.Interruptible;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.security.util.SecurityConstants;


/**
 * 线程就是程序中一个线程的执行.JVM允许一个应用中多个线程并发执行.
 *
 * 每个线程都有优先级.高优先级线程优先于低优先级线程执行.
 * 每个线程都可以(不可以)被标记为守护线程.
 * 当线程中的run()方法代码里面又创建了一个新的线程对象时,新创建的线程优先级和父线程优先级一样.
 * 当且仅当父线程为守护线程时,新创建的线程才会是守护线程.
 *
 * 当JVM启动时,通常会有唯一的一个非守护线程(这一线程用于调用指定类的main()方法)
 * JVM会持续执行线程直到下面情况某一个发生为止:
 * 1.类运行时exit()方法被调用 且 安全机制允许此exit()方法的调用.
 * 2.所有非守护类型的线程均已经终止,or run()方法调用返回  or 在run()方法外部抛出了一些可传播性的异常.
 *
 *
 * 有2种方式可以创建一个可执行线程.
 * 1.定义一个继承Thread类的子类.子类可覆写父类的run()方法.子类实例分配内存后可运行(非立即,取决于CPU调用)
 * 比如:计算大于指定值的素数的线程可以写成如下
 * class PrimeThread extends Thread {
 *         long minPrime;
 *         PrimeThread(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // compute primes larger than minPrime
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 * }
 * 下面的代码将创建一个线程并启动它.
 * PrimeThread p = new PrimeThread(143);
 *     p.start();
 *
 * 2.另一个实现线程的方式就是使类实现Runnable接口.
 * 此类自己会实现run()方法.然后此线程会被分配内存,当线程被创建时,会传入一个参数,然后开始执行.
 * 此种方式的样例代码如下:
 *
 * class PrimeRun implements Runnable {
 *         long minPrime;
 *         PrimeRun(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // compute primes larger than minPrime
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 * }
 * 下面的代码能够创建一个线程并开始执行:
 *  PrimeRun p = new PrimeRun(143);
 *     new Thread(p).start();
 *
 * 每一个线程都有一个用于目的标识的名字.多个线程可以有相同的名字.
 * 线程被创建时如果名字没有被指定,则系统为其自动生成一个新的名字.
 *
 * 除非特别说明,否则在创建线程时传入一个null参数到构造器或者方法会抛出空指针异常NullPointerException
 *
 * @author  unascribed
 * @see     Runnable
 * @see     Runtime#exit(int)
 * @see     #run()
 * @see     #stop()
 * @since   JDK1.0
 */
public class Thread implements Runnable {

    //确保本地注册(类构造器方法<clinit>方法用于类初始化)是创建一个线程首要做的事情.
    //注册的都是一些本地方法
    private static native void registerNatives();
    static {
        registerNatives();
    }

    private volatile String name;//线程名:可更改且线程可见
    private int            priority;//线程优先级用一个int数字表示
    private Thread         threadQ;//
    private long           eetop;


   //是否单步执行此线程
    private boolean     single_step;

    //此线程是否为守护线程
    private boolean     daemon = false;

    //JVM状态
    private boolean     stillborn = false;

    //run方法执行的目标代码
    private Runnable target;

    //此线程所属的组别
    private ThreadGroup group;

    //此类型的类加载器
    private ClassLoader contextClassLoader;

    //此线程继承的访问控制上下文
    private AccessControlContext inheritedAccessControlContext;

    //用于自动编号的匿名线程
    private static int threadInitNumber;
    private static synchronized int nextThreadNum() {
        return threadInitNumber++;
    }

    //此线程的本地变量值.此map由ThreadLocal类进行维护,因为这个类在ThreadLocal中是包级私有的.
    //ThreadLocalMap是一个用于维护线程本地变量的hashmap,此hashmap的key引用类型为弱引用,这是为了支持大且长期存活的使用方法.
    ThreadLocal.ThreadLocalMap threadLocals = null;

    //和此线程相关的由继承得到的本地变量值.
    //此hashmap由InheritableThreadLocal类进行维护.
    ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;

    /*
     * 此线程请求栈的深度,如果线程创建者未指定栈深度则其值为0.
     * 此数字如何被使用完全取决于虚拟机自己;也有一些虚拟机会忽略此变量值.
     */
    private long stackSize;

    //此变量表示:在本地线程终止后,JVM私有的一个状态值
    private long nativeParkEventPointer;

    //线程id
    private long tid;

    //用于生成线程ID
    private static long threadSeqNumber;

    //为工具提供的线程状态值,初始化值表示当前线程还未运行
    private volatile int threadStatus = 0;


    //私有同步方法,获取下一个线程id
    private static synchronized long nextThreadID() {
        return ++threadSeqNumber;
    }

    /**
     * 此变量为用于调用java.util.concurrent.locks.LockSupport.park方法的参数.
     * 其值由方法(private) java.util.concurrent.locks.LockSupport.setBlocker进行设定.
     * 其值访问由方法java.util.concurrent.locks.LockSupport.getBlocker进行获取.
     */
    volatile Object parkBlocker;

    /*
     * 在可中断I/O操作中,本线程中的此对象会被阻塞.
     * 如果此线程的中断状态位被设置,则应该调用此阻塞对象的中断方法.
     */
    private volatile Interruptible blocker;
    private final Object blockerLock = new Object();

    //设定block变量的值;通过java.nio代码中的 sun.misc.SharedSecrets进行调用.
    void blockedOn(Interruptible b) {
        synchronized (blockerLock) {
            blocker = b;
        }
    }

    //一个线程可以拥有的最低优先级
    public final static int MIN_PRIORITY = 1;

    //线程的默认优先级
    public final static int NORM_PRIORITY = 5;

    //一个线程可以拥有的最高优先级.
    public final static int MAX_PRIORITY = 10;

    //返回当前正在执行线程对象的引用,注意这是一个本地方法
    public static native Thread currentThread();

    /**
     * 提示线程调度器当前线程愿意放弃当前CPU的使用。当然调度器可以忽略这个提示。
     *
     * 让出CPU是一种启发式的尝试，以改善线程之间的相对进展，否则将过度利用CPU。
     * 它的使用应该与详细的分析和基准测试相结合以确保它实际上具有预期的效果。
     *
     * 此方法很少有适用的场景.它可以用用于debug或者test,通过跟踪条件可以重现bug.
     * 当设计并发控制结构(如java.util.concurrent.locks包中的并发结构)时,它可能比较有用.
     */
    public static native void yield();

    /**
     * 此方法会引起当前执行线程sleep(临时停止执行)指定毫秒数.
     * 此方法的调用不会引起当前线程放弃任何监听器(monitor)的所有权(ownership).
     */
    public static native void sleep(long millis) throws InterruptedException;

    //和上面的sleep介绍一样
    public static void sleep(long millis, int nanos)
            throws InterruptedException {
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                    "nanosecond timeout value out of range");
        }

        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
            millis++;
        }

        sleep(millis);
    }

    //利用当前访问控制上下文(AccessControlContext)来初始化一个线程.
    private void init(ThreadGroup g, Runnable target, String name,
                      long stackSize) {
        init(g, target, name, stackSize, null, true);
    }

    /**
     * @param stackSize 值为0表示此参数被忽略
     * @param acc 用于继承的访问控制上下文
     * @param inheritThreadLocals 如果值为true,从构造线程继承可继承线程局部变量的初始值
     */
    //初始化一个线程
    private void init(ThreadGroup g, Runnable target, String name,
                      long stackSize, AccessControlContext acc,
                      boolean inheritThreadLocals) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        this.name = name;

        Thread parent = currentThread();
        SecurityManager security = System.getSecurityManager();
        //如果所属线程组为null
        if (g == null) {
            //检测其是否为一个应用

            //如果有安全管理,查询安全管理需要做的工作
            if (security != null) {
                g = security.getThreadGroup();
            }

            //如果安全管理在线程所属父线程组的问题上没有什么强制的要求
            if (g == null) {
                g = parent.getThreadGroup();
            }
        }

        //无论所属线程组是否显示传入,都要进行检查访问.
        g.checkAccess();

        //检查是否有required权限
        if (security != null) {
            if (isCCLOverridden(getClass())) {
                security.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
            }
        }

        g.addUnstarted();

        this.group = g;
        this.daemon = parent.isDaemon();//如果父线程为守护线程,则此线程也被 设置为守护线程.
        this.priority = parent.getPriority();
        if (security == null || isCCLOverridden(parent.getClass()))
            this.contextClassLoader = parent.getContextClassLoader();
        else
            this.contextClassLoader = parent.contextClassLoader;
        this.inheritedAccessControlContext =
                acc != null ? acc : AccessController.getContext();
        this.target = target;
        setPriority(priority);
        if (inheritThreadLocals && parent.inheritableThreadLocals != null)
            this.inheritableThreadLocals =
                    ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
        /* Stash the specified stack size in case the VM cares */
        this.stackSize = stackSize;

        /* Set thread ID */
        tid = nextThreadID();
    }

    //线程不支持前拷贝.取而代之的是构造一个新的线程.
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    //分配一个新的线程对象.此构造器和Thread(ThreadGroup,Runnable,String) 构造器的效果一样.
    public Thread() {
        init(null, null, "Thread-" + nextThreadNum(), 0);
    }

    public Thread(Runnable target) {
        init(null, target, "Thread-" + nextThreadNum(), 0);
    }

    //此构造器生成的线程继承控制访问上下文.
    //此构造器为非公有方法.
    Thread(Runnable target, AccessControlContext acc) {
        init(null, target, "Thread-" + nextThreadNum(), 0, acc, false);
    }

    public Thread(ThreadGroup group, Runnable target) {
        init(group, target, "Thread-" + nextThreadNum(), 0);
    }

    public Thread(String name) {
        init(null, null, name, 0);
    }

    public Thread(ThreadGroup group, String name) {
        init(group, null, name, 0);
    }

    public Thread(Runnable target, String name) {
        init(null, target, name, 0);
    }

    public Thread(ThreadGroup group, Runnable target, String name) {
        init(group, target, name, 0);
    }

    /**
     * @param  stackSize 此值具有平台依赖性,有的虚拟机中此值可能很大以尽量避免栈溢出;有的虚拟机中则很小;
     *                   还有的虚拟机直接忽略此值的设置.
     * @since 1.4
     */
    public Thread(ThreadGroup group, Runnable target, String name,
                  long stackSize) {
        init(group, target, name, stackSize);
    }

    /**
     * 此方法的调用会引起当前线程的执行;JVM会调用此线程的run()方法.
     * 结果就是两个线程可以并发执行:当前线程(从调用的start方法返回)和另一个线程(它在执行run方法).
     * 一个线程可以被调用多次.
     * 尤其注意:一个线程执行完成后可能并不会再被重新执行.
     */
    public synchronized void start() {
        /**
         * 此方法并不会被主要方法线程or由虚拟机创建的系统组线程所调用.
         * 任何向此方法添加的新功能方法在未来都会被添加到虚拟机中.
         * 0状态值代表了NEW的状态.
         */
        if (threadStatus != 0)
            throw new IllegalThreadStateException();

        /* Notify the group that this thread is about to be started
         * so that it can be added to the group's list of threads
         * and the group's unstarted count can be decremented. */
        group.add(this);

        boolean started = false;
        try {
            start0();
            started = true;
        } finally {
            try {
                if (!started) {
                    group.threadStartFailed(this);
                }
            } catch (Throwable ignore) {
                /* do nothing. If start0 threw a Throwable then
                  it will be passed up the call stack */
            }
        }
    }

    private native void start0();

    //如果此线程有runable对象,则执行,否则什么也不执行.
    @Override
    public void run() {
        if (target != null) {
            target.run();
        }
    }

    //此方法由系统调用,用于在一个线程退出前做一些扫尾工作.
    private void exit() {
        if (group != null) {
            group.threadTerminated(this);
            group = null;
        }
        /* Aggressively null out all reference fields: see bug 4006245 */
        target = null;
        /* Speed the release of some of these resources */
        threadLocals = null;
        inheritableThreadLocals = null;
        inheritedAccessControlContext = null;
        blocker = null;
        uncaughtExceptionHandler = null;
    }

    /**
     * 强制线程退出.此时会创建一个新的对象ThreadDeath作为异常.
     * 允许对一个还没有start的线程执行此方法.如果当前线程已经start了,则此方法的调用会使其立即停止.
     *
     * 客户端不应该经常去捕获ThreadDeath异常,除非有一些额外的清除工作要做(注意:在线程死亡前,ThreadDeath的异常在抛出
     * 时会引发try对应的finally代码块的执行).如果catch捕获了ThreadDeath对象,必须重新抛出此异常以保证线程可以真正的死亡.
     *
     * 最顶级的错误处理器会对其它未被捕获类型的异常进行处理,但是如果未处理异常是线程死亡的实例，则不会打印消息或通知应用程序。
     */
    @Deprecated
    public final void stop() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            checkAccess();
            if (this != Thread.currentThread()) {
                security.checkPermission(SecurityConstants.STOP_THREAD_PERMISSION);
            }
        }
        // A zero status value corresponds to "NEW", it can't change to
        // not-NEW because we hold the lock.
        if (threadStatus != 0) {
            resume(); // Wake up thread if it was suspended; no-op otherwise
        }

        //虚拟机能处理所有的线程
        stop0(new ThreadDeath());
    }


    @Deprecated
    public final synchronized void stop(Throwable obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * 此方法功能:中断当前线程.
     *
     * 除非当前线程在中断自己(这么做是允许的),此线程的checkAccess()方法被调用且抛出异常SecurityException
     *
     * 1.如果当前线程由于wait类型方法,join类型方法或者sleep类型的方法的调用被阻塞,则它的中断状态将被清除且会收到一个
     * 中断异常InterruptedException
     *
     * 2.如果此线程由于java.nio.channels.InterruptibleChannel类中的InterruptibleChannel的I/O操作而被阻塞,
     * 则此方法会导致通道被关闭,且线程的中断状态会被重置,同时线程会收到一个异常ClosedByInterruptException.
     *
     * 3.如果此线程由于java.nio.channels.Selector而阻塞,则线程的中断状态会被重置,且它将立即从阻塞的selection操作返回,
     * 且返回值通常是一个非零值,这就和java.nio.channels.Selector#wakeup的wakeup()方法被调用一样.
     *
     * 4.如果前面的条件都不成立，那么该线程的中断状态将被重置.。
     *
     * 中断一个处于非活着状态的线程并不需要产生任何其它影响.
     *
     * @revised 6.0
     * @spec JSR-51
     */
    public void interrupt() {
        if (this != Thread.currentThread())
            checkAccess();

        //对阻塞锁使用同步机制
        synchronized (blockerLock) {
            Interruptible b = blocker;
            if (b != null) {
                interrupt0(); //只是为了设定中断标识位
                b.interrupt(this);//中断当前线程
                return;
            }
        }
        interrupt0();//只是为了设置中断标识位
    }

    /**
     * 测试当前线程是否被中断.
     * 线程的中断状态会被此方法清除.
     * 换句话说,如果此方法两次调用都能成功,则第二次调用的返回结果为false(除非在第一次调用完后和第二次调用前,当前线程被再次中断)
     *
     * 因为在中断方法被调用时线程并未处于alive状态而忽略线程中断的情况会由于此方法的调用而受到影响.
     *
     * @see #isInterrupted()
     * @revised 6.0
     */
    public static boolean interrupted() {
        return currentThread().isInterrupted(true);
    }

    /**
     * 查看当前线程是否被中断.
     * 此方法的调用不会影响当前线程的中断状态.
     *
     * 因为在中断方法被调用时线程并未处于alive状态而忽略线程中断的情况会由于此方法的调用而受到影响.
     *
     * @see     #interrupted()
     * @revised 6.0
     */
    public boolean isInterrupted() {
        return isInterrupted(false);
    }

    /**
     * 测试一些线程是否被中断.
     * 中断状态会被重置or并不依赖于之前的中断清除的值.
     */
    private native boolean isInterrupted(boolean ClearInterrupted);

    /**
     * Throws {@link NoSuchMethodError}.
     * 此方法在最开始被设计的目的是:不带任何清除操作的销毁一个线程.
     * 此方法被调用后线程持有的监听器依旧处于锁状态.
     * 然而,此方法从未被实现过.如果被实现,则由于悬挂的问题会带来死锁.
     * 当目标线程被销毁时,如果它持有了一个用于保护临界资源的锁,那么会导致此临界资源再也无法被其它线程使用.
     * 如果其它线程尝试对此资源进行加锁,就会导致死锁.这种死锁通常表现为"冻结"状态.
     * @throws NoSuchMethodError always
     */
    @Deprecated
    public void destroy() {
        throw new NoSuchMethodError();
    }

    //测试当前线程是否处于存活状态.如果一个线程在死亡状态前都是存活状态.
    public final native boolean isAlive();

    //将一个线程挂起
    @Deprecated
    public final void suspend() {
        checkAccess();
        suspend0();
    }

    //恢复一个悬挂状态的线程
    @Deprecated
    public final void resume() {
        checkAccess();
        resume0();
    }

    //当前线程的最小优先级被设定为参数newPriority,其最高优先级为线程所属线程组的优先级
    public final void setPriority(int newPriority) {
        ThreadGroup g;
        checkAccess();
        if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
            throw new IllegalArgumentException();
        }
        //要求此线程必须有所属线程组
        if((g = getThreadGroup()) != null) {
            if (newPriority > g.getMaxPriority()) {
                newPriority = g.getMaxPriority();
            }
            setPriority0(priority = newPriority);
        }
    }

    //获取线程状态优先级
    public final int getPriority() {
        return priority;
    }

    //这是一个同步方法,用于设定线程名
    public final synchronized void setName(String name) {
        checkAccess();
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        this.name = name;
        if (threadStatus != 0) {
            setNativeName(name);
        }
    }

    //返回此线程的名字
    public final String getName() {
        return name;
    }

    //返回线程所属的线程组
    public final ThreadGroup getThreadGroup() {
        return group;
    }

    //返回线程所属组的线程数,这是一个估计值.
    public static int activeCount() {
        return currentThread().getThreadGroup().activeCount();
    }

    /**
     * 将线程所属组和其子组中所有活着的线程拷贝到参数数组中.
     * 此方法只调用了一个方法java.lang.ThreadGroup.enumerate().
     *
     * 一个应用如果想获得这个线程数组,则它必须调用此方法,然而如果此数组太小而无法存放所有的线程,则放不下的线程
     * 就自动被忽略了.其实从线程组里面获取存活线程的方法是受到争议的,此方法调用者应该证明方法返回值应该严格小于
     * 参数数组的长度.
     *
     * 因为此方法在被调用时存在竞争,因此建议此方法只用于debug和监听目的.
     */
    public static int enumerate(Thread tarray[]) {
        return currentThread().getThreadGroup().enumerate(tarray);
    }

    //查看此线程使用了多少栈框架,此线程必须被挂起.
    @Deprecated
    public native int countStackFrames();

    /**
     * 最多等待参数millis(ms)时长当前线程就会死亡.参数为0时则要持续等待.
     *
     * 此方法在实现上:循环调用以this.isAlive()方法为条件的wait()方法.
     * 当线程终止时notifyAll()方法会被调用.
     * 建议应用程序不要在线程实例上使用wait,notify,notifyAll方法.
     */
    public final synchronized void join(long millis)
            throws InterruptedException {
        long base = System.currentTimeMillis();
        long now = 0;

        //如果等待时间<0,则抛出异常
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        //如果等待时间为0
        if (millis == 0) {
            while (isAlive()) {
                wait(0);
            }
        } else {
            while (isAlive()) {
                long delay = millis - now;
                if (delay <= 0) {
                    break;
                }
                wait(delay);
                now = System.currentTimeMillis() - base;
            }
        }
    }

    //等待时间单位为纳秒,其它解释都和上面方法一样
    public final synchronized void join(long millis, int nanos)
            throws InterruptedException {

        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                    "nanosecond timeout value out of range");
        }

        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
            millis++;
        }

        join(millis);
    }

    //方法功能:等待一直到线程死亡.
    public final void join() throws InterruptedException {
        join(0);
    }

    //此方法只用于debug
    public static void dumpStack() {
        new Exception("Stack trace").printStackTrace();
    }

    /**
     * 将当前线程设定为守护线程or用户线程.
     * 此方法在start前被调用.
     * @param  on 如果值为true,则此线程被设定为守护线程
     */
    public final void setDaemon(boolean on) {
        checkAccess();
        if (isAlive()) {
            throw new IllegalThreadStateException();
        }
        daemon = on;
    }

    //查看当前线程是否为守护线程
    public final boolean isDaemon() {
        return daemon;
    }

    /**
     * 确定当前运行的线程是否有权利更改此线程.
     * 如果有安全管理器,则会将当前线程作为参数传入checkAccess()方法.
     * 这可能会导致SecurityException异常的跑出.
     */
    public final void checkAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkAccess(this);
        }
    }

    //格式包括 :线程名+优先级+所属组别
    public String toString() {
        ThreadGroup group = getThreadGroup();
        if (group != null) {
            return "Thread[" + getName() + "," + getPriority() + "," +
                    group.getName() + "]";
        } else {
            return "Thread[" + getName() + "," + getPriority() + "," +
                    "" + "]";
        }
    }

    /**
     * 此方法返回此线程的上下文类加载器.上下文类加载器由当加载类和资源时使用代码创建线程的创造者提供.
     * 如果通过方法setContextClassLoader进行上下文类加载器的设定,则默认的上下文类加载器为父线程.
     * 原始线程的类加载器通常被设定为:加载应用的类加载器.
     *
     * 如果安全管理器存在,且调用者的类加载器不为null,且它们不相同,且也不是父子关系,则此方法的调用会导致安全管理的方法
     * checkPermission的调用,用于确定对上下文类加载器的检索是否被允许.
     *
     * 关于注解@CallerSensitive:这个注解是为了堵住漏洞用的。曾经有黑客通过构造双重反射来提升权限,原理是当时反射只检查固定深度的
     * 调用者的类，看它有没有特权.使用CallerSensitive后，getCallerClass不再用固定深度去寻找actual caller（“我”），而是把所
     * 有跟反射相关的接口方法都标注上CallerSensitive，搜索时凡看到该注解都直接跳过，这样就有效解决了这类的黑客问题.
     * @since 1.2
     */
    @CallerSensitive
    public ClassLoader getContextClassLoader() {
        if (contextClassLoader == null)
            return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader.checkClassLoaderPermission(contextClassLoader,
                    Reflection.getCallerClass());
        }
        return contextClassLoader;
    }

    /**
     * 设定一个线程的上下文类加载器.上下文类加载器可以在线程被创建时被设定,且允许线程创建者提供合适的类加载器.
     *
     * 如果存在安全管理器,则它的checkPermission()方法会被调用,用于查看设定上下文类加载器的行为是否被允许.
     * @since 1.2
     */
    public void setContextClassLoader(ClassLoader cl) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        contextClassLoader = cl;
    }

    /**
     * 当且仅当当前线程持有指定对象的监听器锁时,返回返回true.
     * 这一方法被设计的目的是:用于程序自身去声明它已经有某个对象的锁啦.
     * @since 1.4
     */
    public static native boolean holdsLock(Object obj);

    private static final StackTraceElement[] EMPTY_STACK_TRACE
            = new StackTraceElement[0];

    /**
     * 返回表示该线程堆栈转储的堆栈跟踪元素数组。
     * 如果线程还没有start,or虽然start了,但是并没有被CPU调用过,or线程以及终止了,则返回数组长度为0.
     * 如果返回数组长度非0,则数组中第一个元素(索引为0)代表了栈的顶部,就是所有调用方法中距离现在时间最近的那个.
     * 数组的最后一个元素代表了栈的底部,这是所有调用方法中距离现在时间最远的那个.
     *
     * 如果存在安全管理器,且这一线程又不是当前线程,则安全管理器的checkPermission()方法会被调用以查看
     *
     * 一些虚拟机在某些情况下,可能会在栈跟踪时遗漏至少一个以上的栈.在极端情况下,虚拟机没有任何栈跟踪信息所以返回数组长度为0.
     * @since 1.5
     */
    public StackTraceElement[] getStackTrace() {
        //如果此线程并不是当前线程
        if (this != Thread.currentThread()) {
            //检查getStackTrace许可情况.
            SecurityManager security = System.getSecurityManager();
            //如果安全管理器不为空,则进行检查
            if (security != null) {
                security.checkPermission(
                        SecurityConstants.GET_STACK_TRACE_PERMISSION);
            }
            //if方法是一种优化,以便我们不会在此处调用虚拟机里面还没有开始或者已经死亡的线程.
            if (!isAlive()) {
                return EMPTY_STACK_TRACE;
            }
            StackTraceElement[][] stackTraceArray = dumpThreads(new Thread[] {this});
            StackTraceElement[] stackTrace = stackTraceArray[0];
            //在前面的isAlive检查中一个活着的线程可能在此时已经终结了,所以此时不会再有栈跟踪.
            if (stackTrace == null) {
                stackTrace = EMPTY_STACK_TRACE;
            }
            return stackTrace;
        } else {
            //此处并不需要JVM的帮忙.
            return (new Exception()).getStackTrace();
        }
    }

    /**
     * 返回一个用于所有存活线程的栈跟踪信息的map.
     * map的key是每个线程;value是对应线程的栈跟踪元素的一个数组.
     *
     * 当此方法被调用时,可能有些线程正在执行.每一个线程的栈跟踪信息都代表了线程在某一时刻状态的快照且每一个栈跟踪信息
     * 会在不同的时间得到.如果虚拟机中某一个线程没有栈跟踪信息则其数组长度为0.
     *
     * 如果有安全管理器,则安全管理器的checkPermission方法会被调用以检查是否允许获取所有线程的栈跟踪信息.
     *
     * @since 1.5
     */
    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        // check for getStackTrace permission
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(
                    SecurityConstants.GET_STACK_TRACE_PERMISSION);
            security.checkPermission(
                    SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        }

        // Get a snapshot of the list of all threads
        Thread`[`] threads = getThreads();
        StackTraceElement[][] traces = dumpThreads(threads);
        Map<Thread, StackTraceElement[]> m = new HashMap<>(threads.length);
        for (int i = 0; i < threads.length; i++) {
            StackTraceElement[] stackTrace = traces[i];
            if (stackTrace != null) {
                m.put(threads[i], stackTrace);
            }
            // else terminated so we don't put it in the map
        }
        return m;
    }


    //常量:运行时许可
    private static final RuntimePermission SUBCLASS_IMPLEMENTATION_PERMISSION =
            new RuntimePermission("enableContextClassLoaderOverride");

    //子类安全审核结果的缓存
    //在将来如果它出现的话,可以替代ConcurrentReferenceHashMap
    private static class Caches {
        //子类安全审核结果的缓存值
        static final ConcurrentMap<WeakClassKey,Boolean> subclassAudits =
                new ConcurrentHashMap<>();

        //审核子类的弱引用队列
        static final ReferenceQueue<Class<?>> subclassAuditsQueue =
                new ReferenceQueue<>();
    }

    /**
     * 证明创建当前子类实例能够忽略安全限制:子类不能覆盖安全敏感,非final类型的方法,否则
     * enableContextClassLoaderOverride这个运行时许可会被坚持.
     */
    private static boolean isCCLOverridden(Class<?> cl) {
        if (cl == Thread.class)
            return false;

        processQueue(Caches.subclassAuditsQueue, Caches.subclassAudits);
        WeakClassKey key = new WeakClassKey(cl, Caches.subclassAuditsQueue);
        Boolean result = Caches.subclassAudits.get(key);
        if (result == null) {
            result = Boolean.valueOf(auditSubclass(cl));
            Caches.subclassAudits.putIfAbsent(key, result);
        }
        return result.booleanValue();
    }

    /**
     * 在给定子类上的检查操作以证明它并未覆写安全敏感,非final类型的方法.
     * 如果子类覆写了任何一个此类型的方法,则返回true;否则false.
     */
    private static boolean auditSubclass(final Class<?> subcl) {
        Boolean result = AccessController.doPrivileged(
                new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        for (Class<?> cl = subcl;
                             cl != Thread.class;
                             cl = cl.getSuperclass())
                        {
                            try {
                                cl.getDeclaredMethod("getContextClassLoader", new Class<?>[0]);
                                return Boolean.TRUE;
                            } catch (NoSuchMethodException ex) {
                            }
                            try {
                                Class<?>[] params = {ClassLoader.class};
                                cl.getDeclaredMethod("setContextClassLoader", params);
                                return Boolean.TRUE;
                            } catch (NoSuchMethodException ex) {
                            }
                        }
                        return Boolean.FALSE;
                    }
                }
        );
        return result.booleanValue();
    }

    private native static StackTraceElement[][] dumpThreads(Thread[] threads);
    private native static Thread[] getThreads();

    /**
     * 返回当前线程的ID.
     * 线程ID是一个long类型的正数,在线程被创建时就有的.
     * 线程ID在其生命周期内都不会更改且独一无二.
     * 当线程终止时,则此线程的ID会被重复使用.
     * @since 1.5
     */
    public long getId() {
        return tid;
    }

    /**
     * 此枚举表示线程状态.线程状态有如下几种:
     * 1.NEW表示:线程还未开始,只是进行了一些线程创建的初始化操作,但未调用start()方法.
     * 2.RUNNABLE表示:线程在JVM里面处于运行状态(这里就绪和运行同属于运行).
     * 3.BLOCKED表示:线程正在等待一个监视器锁,处于阻塞状态.
     * 4.WAITING表示:一个线程在等待另一个线程的特定操作(通知or中断),这种等待是无限期的.
     * 5.TIMED_WAITING表示:一个线程在等待另一个线程的特定操作,这种等待是有时间限制的.一旦超时则线程自行返回.
     * 6.TERMINATED表示:线程已退出.表示线程已经执行完毕.
     *
     * 线程在某一时刻,只能处于上述6个状态的某一个.这些状态值是虚拟机状态值,因而并不会反映操作系统的线程状态.
     *
     * @since   1.5
     * @see #getState
     */
    public enum State {

        NEW,

        //线程可以正在运行,也可以处于就绪状态等待获得CPU.
        RUNNABLE,

        //在调用完wait()方法后,为了进入同步方法(锁)或者重进入同步方法(锁).
        BLOCKED,

        /**
         * 一个线程处于wating状态,是因为调用了下面方法中的某一个:
         * 1.Object.wait
         * 2.Thread.join
         * 3.LockSupport.park
         *
         * 其它线程的特定操作包括 :notify(),notifyAll(),join()等.
         */
        WAITING,

        /**
         * 线程等待指定时间.
         * 这种状态的出现是因为调用了下面方法中的某一个:
         * 1.Thread.sleep()
         * 2.Object.wait()
         * 3.Thread.join()
         * 4.LockSupport.parkNanos()
         * 5.LockSupport.parkUntil()
         */
        TIMED_WAITING,

        //线程完成了执行
        TERMINATED;
    }

    /**
     * 返回线程状态.
     * 这一方法的设计目的:用于系统状态的监听,而非同步控制.
     * @since 1.5
     */
    public State getState() {
        // get current thread state
        return sun.misc.VM.toThreadState(threadStatus);
    }

    // Added in JSR-166

    /**
     * 由于未捕获异常而导致线程终止的函数接口处理器.
     * 当一个线程由于未捕获异常而终止时,JVM将会使用getUncaughtExceptionHandler来查询此线程的UncaughtExceptionHandler,
     * 且会调用处理器handler的uncaughtException()方法,将此线程和其异常作为参数.
     *
     * 如果一个线程没有它特定的UncaughtExceptionHandler,则它所属的线程组对象充当其UncaughtExceptionHandler.
     * 如果线程组对象没有处理异常的指定请求,它可以向前调用getDefaultUncaughtExceptionHandler的默认处理异常的方法.
     *
     * @see #setDefaultUncaughtExceptionHandler
     * @see #setUncaughtExceptionHandler
     * @see ThreadGroup#uncaughtException
     * @since 1.5
     */
    @FunctionalInterface
    public interface UncaughtExceptionHandler {
        /**
         * 由于未捕获异常而导致线程终止的方法调用.
         * 此方法抛出的任何异常都会被JVM忽略.
         * @param t the thread
         * @param e the exception
         */
        void uncaughtException(Thread t, Throwable e);
    }

    // null unless explicitly set
    private volatile UncaughtExceptionHandler uncaughtExceptionHandler;

    // null unless explicitly set
    private static volatile UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    /**
     * 设定默认处理器用于处理:由于未捕获异常而导致的线程终止,且此线程还未定义任何其它的处理器.
     *
     * 未捕获异常首先由本线程进行处理,然后由线程所属的线程组对象处理,最后由默认未捕获异常处理器进行处理.
     * 如果线程未设定明确的未捕获异常处理器,且线程的线程组(包括父线程组)也未指定,则此时默认处理器的uncaughtException
     * 方法会被执行.由于设定了默认的未捕获异常处理器,则应用能够更改未捕获异常的处理方法.
     *
     * 注意:默认的未捕获异常处理器不应该经常使用线程的线程组对象,因为这会引起无限递归.
     *
     * @since 1.5
     */
    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(
                    new RuntimePermission("setDefaultUncaughtExceptionHandler")
            );
        }
        defaultUncaughtExceptionHandler = eh;
    }

    //当一个线程因未捕获异常而突然终止时,返回的默认处理器.
    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler(){
        return defaultUncaughtExceptionHandler;
    }

    //返回由于未捕获异常而导致线程中断的处理器.如果此线程无未捕获异常处理器,则返回此线程的线程组对象
    //如果此线程已经终止,则返回null.
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler != null ?
                uncaughtExceptionHandler : group;
    }

    /**
     * 设定一个由于未捕获异常而导致线程中断的处理器.
     * 通过设定未捕获异常处理器,一个线程可以完全控制如何处理未捕获异常.
     * 如果没有设定未捕获异常,则线程组对象默认为其未捕获异常处理器.
     * @since 1.5
     */
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        checkAccess();
        uncaughtExceptionHandler = eh;
    }

    //将未捕获异常分发给处理器.这一方法通常被JVM调用.
    private void dispatchUncaughtException(Throwable e) {
        getUncaughtExceptionHandler().uncaughtException(this, e);
    }

    //删除指定map中那些在特定引用队列中已经排队的所有key
    static void processQueue(ReferenceQueue<Class<?>> queue,
                             ConcurrentMap<? extends
                                     WeakReference<Class<?>>, ?> map)
    {
        Reference<? extends Class<?>> ref;
        while((ref = queue.poll()) != null) {
            map.remove(ref);
        }
    }

    /**
     *  Weak key for Class objects.
     *  WeakReference类说明:弱引用对象(JVM四种引用中的弱引用),这并不妨碍它们的引用对象被最终化、定型，然后回收.弱引用最常用于实现规范化映射。
     **/
    static class WeakClassKey extends WeakReference<Class<?>> {

        //用于保存引用的参照hash值,一旦参照明确后用于保持一个持续的hash值.
        private final int hash;

        //根据给定的对象创建一个weakClassKey,使用给定队列进行注册.
        WeakClassKey(Class<?> cl, ReferenceQueue<Class<?>> refQueue) {
            super(cl, refQueue);
            hash = System.identityHashCode(cl);
        }

        //返回原始引用的参照hash值
        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (obj instanceof WeakClassKey) {
                Object referent = get();
                return (referent != null) &&
                        (referent == ((WeakClassKey) obj).get());
            } else {
                return false;
            }
        }
    }

    /**
     * 以下三个初始未初化的变量专门由java.util.concurrent.ThreadLocalRandom管理.
     * 这些变量用在并发代码中构建高性能的PRNGs,由于存在共享失败的情况所以我们不能冒险共享.
     * 因此,这些变量和注解@Contended是隔离的.
     */
    //用于ThreadLocalRandom的当前种子
    @sun.misc.Contended("tlr")
    long threadLocalRandomSeed;

    //探测hash值;如果threadLocalRandomSeed已经初始化了,则其值非0
    @sun.misc.Contended("tlr")
    int threadLocalRandomProbe;

    //隔离ThreadLocalRandom序列的第二个种子
    @sun.misc.Contended("tlr")
    int threadLocalRandomSecondarySeed;

    //一些私有本地辅助方法
    private native void setPriority0(int newPriority);
    private native void stop0(Object o);
    private native void suspend0();
    private native void resume0();
    private native void interrupt0();
    private native void setNativeName(String name);
}


````