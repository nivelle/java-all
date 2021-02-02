package com.nivelle.base.jdk.lang.ref;

import java.lang.ref.*;
import java.util.Objects;

/**
 * 引用类型
 *
 * @author nivelle
 * @date 2020/04/19
 */
public class ReferenceMock {
    private static int K = 1024;

    private static int M = 1024 * K;

    /**
     * 1. Referent: 被引用对象
     *
     * 2. RefernceQueue: 如果Reference在构造方法加入ReferenceQueue参数, Reference 在它的Referent被GC的时,会将这个Reference加入ReferenceQueue
     * - ReferenceQueue对象本身保存了一个Reference类型的head节点，Reference封装了next字段，这样就是可以组成一个单向链表。同时ReferenceQueue提供了两个静态字段NULL，ENQUEUED
     * - NULL是当我们构造Reference实例时queue传入null时，会默认使用NULL，这样在enqueue时判断queue是否为NULL,如果为NULL直接返回，入队失败。
     * - ENQUEUED的作用是防止重复入队，reference后会把其queue字段赋值为ENQUEUED,当再次入队时会直接返回失败
     * - 除了幻象引用（因为 get 永远返回 null），如果对象还没有被销毁，都可以通过 get 方法获取原有对象。这意味着，利用软引用和弱引用，我们可以将访问到的对象，重新指向强引用，也就是人为的改变了对象的可达性状态
     */
    public static void main(String[] args) {


        System.out.println("弱引用相较于软引用生命周期更弱, 当一个对象仅有一个弱引用引用它时，被该WeakReference引用Referent会在JVM GC执行以后被回收;同样, 弱引用也可以和一个ReferenceQueue共同使用");
        Object object = new Object();
        ReferenceQueue weakQueue = new ReferenceQueue();
        final WeakReference<Object> weakReference = new WeakReference(object, weakQueue);
        //reference get 返回的是它关联的对象
        System.out.println("element equals weakReference.get():" + Objects.equals(object, weakReference.get()));
        System.out.println("reference is:" + weakReference.hashCode());
        System.out.println("before gc object is:" + object.hashCode());
        object = null;
        System.out.println("before gc 被引用对象直接置为null后，但是并没有gc,reference:" + weakReference.hashCode());
        System.out.println("before gc 被引用对象直接置为null后，但是并没有gc,关联的object:" + weakReference.get().hashCode());
       System.gc();
        //gc之后，引用关联的兑现置空了
        System.out.println("after gc reference object:" + weakReference.get());
        System.out.println("after gc reference hashCode:" + weakReference.hashCode());
        Reference referenceQueueQueue = weakQueue.poll();
        long hashCode = referenceQueueQueue != null ? referenceQueueQueue.hashCode() : 0L;
        System.out.println("after gc referenceQueue poll element hashCode:" +hashCode);
        System.out.println("=========================");

//        Object object1 = new Object();
//        final WeakReference<Object> weakReference1 = new WeakReference(object1, weakQueue);
//        System.out.println("before gc 被引用对象直接置为null后，但是并没有gc,reference:" + weakReference1.hashCode());
//        System.out.println("before gc 被引用对象直接置为null后，但是并没有gc,关联的object:" + weakReference1.get().hashCode());
//        System.gc();
//        System.out.println("after gc reference 关联的对象:" + weakReference1.get());
//        System.out.println("after gc reference:" + weakReference1.hashCode());
//        System.out.println("after gc referenceQueue poll element:" + weakQueue.poll().hashCode());

        System.out.println("SoftReference:软引用是Java中一个类, 它的Referent只有在内存不够的时候在抛出OutOfMemoryError前会被 JVM GC, " +
                "软引用一般用来实现内存敏感缓存(memory-sensitive caches)");
        /**
         *  1. 根据SoftReference引用实例的timestamp(每次调用softReference.get()会自动更新该字段
         *
         *  2. 当前JVM heap的内存剩余(free_heap)情况
         *
         *  3. 软引用通常会在最后一次引用后，还能保持一段时间，默认值是根据堆剩余空间计算的（以 M bytes 为单位）。从 Java  1.3.1 开始，提供了 -XX:SoftRefLRUPolicyMSPerMB 参数，我们可以以毫秒（milliseconds）为单位设置
         */
        byte[] b = new byte[2 * M];
        ReferenceQueue queue = new ReferenceQueue();
        SoftReference softReference = new SoftReference(b, queue);
        System.out.println("softReference is:" + softReference);
        System.out.println("softReference object is:" + softReference.get());
        b = null;
        System.out.println("被引用对象直接置为null后:" + softReference.get());
        try {
            while (true) {

                byte[] c = new byte[50 * M];
                if (c.length>10){
                   break;
                }
            }
        } catch (Error error) {
            System.out.println(error);
            System.out.println("after oom softReference get:" + softReference.get());
            System.out.println("after oom queue poll:" + queue.poll());
        }
        System.out.println();
        System.out.println("虚引用不会影响对象的生命周期, 它仅仅是一个对象生命周期的一个标记");
        /**
         * 虚引用不会影响对象的生命周期,它仅仅是一个对象生命周期的一个标记,他必须与ReferenceQueue一起使用, 构造方法必须传入ReferenceQueue, 因为它的作用就是在对象被JVM决定需要GC后,
         * 将自己enqueue到ReferenceQueue中.它通常用来在一个对象被GC前作为一个GC的标志,以此来做一些finalize操作,另外,PhantomReference.get()方法永远返回null;
         *
         *
         * PhantomReference 有两个好处:
         *
         * 1. 它可以让我们准确地知道对象何时被从内存中删除，这个特性可以被用于一些特殊的需求中(例如 Distributed GC， XWork 和 google-guice 中也使用 PhantomReference 做了一些清理性工作).
         *
         * 2. 它可以避免 finalization 带来的一些根本性问题, 上文提到 PhantomReference 的唯一作用就是跟踪 referent 何时GC, 但是 WeakReference 也有对应的功能, 两者的区别到底在哪呢 ?
         *
         *    这就要说到 Object 的 finalize() 方法, 此方法将在 gc 执行前被调用, 如果某个对象重载了 finalize 方法并故意在方法内创建本身的强引用, 这将导致这一轮的 GC 无法回收这个对象并有可能引起任意次 GC， 最后的结果就是明明 JVM 内有很多 Garbage 却 OutOfMemory，
         *    使用 PhantomReference 就可以避免这个问题， 因为 PhantomReference 是在 finalize 方法执行后回收的，也就意味着此时已经不可能拿到原来的引用, 也就不会出现上述问题, 当然这是一个很极端的例子, 一般不会出现
         */
        Reference reference1 = new PhantomReference(object, null);


        Object counter = new Object();
        ReferenceQueue refQueue = new ReferenceQueue<>();
        PhantomReference<Object> p = new PhantomReference<>(counter, refQueue);
        counter = null;
        System.gc();
        try {
            // Remove是一个阻塞方法，可以指定timeout，或者选择一直阻塞
            Reference<Object> ref = refQueue.remove(10);
            if (ref != null) {
                System.out.println("gc之后对象被放到引用队列里面,ref:"+ref.get());
            }else {
                System.out.println("队列为空");
            }
        } catch (InterruptedException e) {
            // Handle it
        }
        /**
         * ReferenceQueue.enqueue
         *
         * @params r : reference
         *
         * boolean enqueue(Reference<? extends T> r) {
         *         synchronized (lock) {
         *             ReferenceQueue<?> queue = r.queue;
         *             //如果初始化的时候未指定队列或者为了防止重复入队
         *             //ENQUEUED的作用是防止重复入队，reference后会把其queue字段赋值为ENQUEUED,当再次入队时会直接返回失败
         *             if ((queue == NULL) || (queue == ENQUEUED)) {
         *                 return false;
         *             }
         *             //reference 里的 referenceQueue是同一个对象
         *             assert queue == this;
         *             //设置队列为 ENQUEUED
         *             r.queue = ENQUEUED;
         *             //如果队列head为空，则r的下一个节点设置为r
         *             r.next = (head == null) ? r : head;
         *             //r 赋值给head
         *             head = r;
         *             queueLength++;
         *             if (r instanceof FinalReference) {
         *                 sun.misc.VM.addFinalRefCount(1);
         *             }
         *             //通知等待获取锁的线程
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
