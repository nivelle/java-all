package com.nivelle.core.javabase;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ThreadLocal 内存溢出
 *
 * @author fuxinzhong
 * @date 2020/11/17
 */
public class ThreadLocalMemoryLeakMock {
    public static final ExecutorService threadPool = Executors.newFixedThreadPool(5);

    public static final ThreadLocal<LocalVariable> threadLocal = new ThreadLocal<>();
    static Set<Thread> threads = new HashSet<>();

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            threadPool.execute(() -> {
                // 使用大对象
                threadLocal.set(new LocalVariable());
                System.out.println("use local variable");

                // 保存线程池中核心线程的引用,用于debug查看ThreadLocalMap中是否保存了对象
                threads.add(Thread.currentThread());

                // 使用完后删除，不执行会造成内存泄漏
                //threadLocal.remove();
            });
            Thread.sleep(500);
        }
        System.out.println("gc之前。。。。。");
        //主动触发gc,将导致ThreadLocal被回收
        System.gc();
        System.out.println(threadLocal);
        // 由于没有调用线程池的shutdown方法，线程池中的核心线程并不会退出,进而JVM也不会退出
        // debug可以看到，threads集合中每个线程的 ThreadLocalMap 中都有key(referent)==null，value为LocalVariable大对象没被回收
        // 此时jvm进程并不会退出，因为5个线程还存在，jconsole 可以监控堆内存的使用量。
        System.out.println("pool executor over");
    }


    static class LocalVariable {
        // long 是64位8B，数组占用内存则为8MB
        private long[] a = new long[1024 * 1024];
    }
}
