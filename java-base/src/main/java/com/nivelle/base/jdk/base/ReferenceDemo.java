package com.nivelle.base.jdk.base;

import java.lang.ref.*;
import java.util.Objects;

/**
 * 引用类型
 *
 * @author nivell
 * @date 2020/04/19
 */
public class ReferenceDemo {
    private static int K = 1024;

    private static int M = 1024 * K;

    /**
     * Referent: 被引用对象
     * <p>
     * RefernceQueue: 如果Reference在构造方法加入ReferenceQueue参数, Reference在它的Referent被GC的时,会将这个Reference加入ReferenceQueue
     * <p>
     * ReferenceQueue对象本身保存了一个Reference类型的head节点，Reference封装了next字段，这样就是可以组成一个单向链表。同时ReferenceQueue提供了两个静态字段NULL，ENQUEUED
     * <p>
     * NULL是当我们构造Reference实例时queue传入null时，会默认使用NULL，这样在enqueue时判断queue是否为NULL,如果为NULL直接返回，入队失败。ENQUEUED的作用是防止重复入队，reference后会把其queue字段赋值为ENQUEUED,当再次入队时会直接返回失败
     */
    public static void main(String[] args) {

        System.out.println("弱引用相较于软引用生命周期更弱, 当一个对象仅有一个弱引用引用它时，被该WeakReference引用Referent会在JVM GC执行以后被回收;同样, 弱引用也可以和一个ReferenceQueue共同使用");
        Object object = new Object();
        ReferenceQueue weakQueue = new ReferenceQueue();
        final WeakReference<Object> weakReference = new WeakReference(object, weakQueue);
        System.out.println("element equals weakReference.get():" + Objects.equals(object, weakReference.get()));
        System.out.println("weakReference is:" + weakReference);
        System.out.println("before gc object is:" + object);
        object = null;
        System.out.println("被引用对象直接置为null后:" + weakReference.get());
        System.gc();
        System.out.println("after gc weakReference.get() is null:" + Objects.isNull(weakReference.get()));
        System.out.println("after gc referenceQueue poll element:" + weakQueue.poll());

        System.out.println();
        System.out.println();

        System.out.println("SoftReference:软引用是Java中一个类, 它的Referent只有在内存不够的时候在抛出OutOfMemoryError前会被 JVM GC, 软引用一般用来实现内存敏感缓存(memory-sensitive caches)");
        byte[] b = new byte[2 * M];
        ReferenceQueue queue = new ReferenceQueue();
        SoftReference softReference = new SoftReference(b, queue);
        System.out.println("softReference is:" + softReference);
        System.out.println("softReference object is:" + softReference.get());
        b = null;
        System.out.println("被引用对象直接置为null后:" + softReference.get());
        try {
            byte[] c = new byte[50 * M];
        } catch (Error error) {
            System.out.println(error);
            System.out.println("after oom softReference get:" + softReference.get());
            System.out.println("after oom queue poll:" + queue.poll());
        }
        System.out.println();
        System.out.println();

        System.out.println("虚引用不会影响对象的生命周期, 它仅仅是一个对象生命周期的一个标记");
        /**
         * 虚引用不会影响对象的生命周期, 它仅仅是一个对象生命周期的一个标记, 他必须与ReferenceQueue一起使用, 构造方法必须传入ReferenceQueue, 因为它的作用就是在对象被JVM决定需要GC后,
         * 将自己enqueue到ReferenceQueue中.它通常用来在一个对象被GC前作为一个GC的标志,以此来做一些finalize操作,另外,PhantomReference.get()方法永远返回null;
         *
         *
         * PhantomReference 有两个好处:
         *
         * 1. 它可以让我们准确地知道对象何时被从内存中删除， 这个特性可以被用于一些特殊的需求中(例如 Distributed GC， XWork 和 google-guice 中也使用 PhantomReference 做了一些清理性工作).
         *
         * 2. 它可以避免 finalization 带来的一些根本性问题, 上文提到 PhantomReference 的唯一作用就是跟踪 referent 何时GC, 但是 WeakReference 也有对应的功能, 两者的区别到底在哪呢 ?
         *
         *    这就要说到 Object 的 finalize 方法, 此方法将在 gc 执行前被调用, 如果某个对象重载了 finalize 方法并故意在方法内创建本身的强引用, 这将导致这一轮的 GC 无法回收这个对象并有可能引起任意次 GC， 最后的结果就是明明 JVM 内有很多 Garbage 却 OutOfMemory， 使用 PhantomReference 就可以避免这个问题， 因为 PhantomReference 是在 finalize 方法执行后回收的，也就意味着此时已经不可能拿到原来的引用, 也就不会出现上述问题, 当然这是一个很极端的例子, 一般不会出现
         */
        Reference reference1 = new PhantomReference(object, null);

        /**
         * ReferenceQueue.enqueue
         *
         * @params r : reference
         *
         * boolean enqueue(Reference<? extends T> r) {
         *         synchronized (lock) {
         *             ReferenceQueue<?> queue = r.queue;
         *             //如果初始化的时候未指定队列或者为了防止重复入队
         *             if ((queue == NULL) || (queue == ENQUEUED)) {
         *                 return false;
         *             }
         *             ## reference 里的 referenceQueue是同一个对象
         *             assert queue == this;
         *             ## 设置队列为 ENQUEUED
         *             r.queue = ENQUEUED;
         *             ## 如果队列head为空，则r的下一个节点设置为r
         *             r.next = (head == null) ? r : head;
         *             ## r 赋值给head
         *             head = r;
         *             queueLength++;
         *             if (r instanceof FinalReference) {
         *                 sun.misc.VM.addFinalRefCount(1);
         *             }
         *             ## 通知等待获取锁的线程
         *             lock.notifyAll();
         *             return true;
         *         }
         * }
         *
         *
         *
         */

    }

}
