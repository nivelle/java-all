package com.nivelle.base.jdk.lang;

import java.lang.reflect.Method;

/**
 * 所有java类的父类
 *
 * @author nivelle
 * @date 2019/12/12
 */
public class ObjectDemo {

    public static void main(String[] args) {

        Method[] methods = Object.class.getMethods();
        for (int i = 0; i < methods.length; i++) {
            System.out.println(methods[i].getName());
        }

        System.out.println("Object对象默认的方法如下:");
        Object object = new Object();
        System.out.println("object默认toString()方法-》类名+hashCode的无符号16进制:" + object.toString());
        /**
         * public final native Class<?> getClass()
         *
         * ## 返回当前运行时对象的Class对象，final不允许复写的native方法
         */
        Class classInstance = object.getClass();
        System.out.println("object默认的getClass方法=》" + classInstance.getName());


        /**
         * 哈希码的约定:
         * 1. 在java程序执行过程中，在一个对象没有被改变的前提下，无论这个对象被调用多少次，hashCode方法都会返回相同的整数值。对象的哈希码没有必要在不同的程序中保持相同的值。
         * 2. 如果2个对象使用equals方法进行比较并且相同的话，那么这2个对象的hashCode方法的值也必须相等。
         * 3. 如果根据equals方法,得到两个对象不相等，那么这2个对象的hashCode值不需要必须不相同。但是，不相等的对象的hashCode值不同的话可以提高哈希表的性能。
         */
        /**
         * hashCode主要用在哈希表中用来定位,所以一般需要重写hashCode方法和equals方法(如果2个对象的equals方法相等，那么他们的hashCode值也必须相等，反之，如果2个对象hashCode值相等，但是equals不相等，这样会影响性能，所以还是建议2个方法都一起重写。)
         *
         * map判断重复数据的条件是 两个对象的哈希码相同并且(两个对象是同一个对象或者两个对象相等[equals为true])
         *
         */
        int objectHashCode = object.hashCode();
        System.out.println("object默认的hashCode方法=》" + objectHashCode);

        Object object1 = new Object();
        /**
         * public boolean equals(Object obj) {
         *         return (this == obj);
         *     }
         */
        System.out.println("Object默认equals方法比较两个对象的内存地址:" + object.equals(object1));
        System.out.println("Object默认equals方法比较两个对象的内存地址:" + object.equals(object));

        /**
         *  ## Object类的clone方法是一个protected的native方法。
         *  protected native Object clone() throws CloneNotSupportedException;
         *
         *  创建并返回当前对象的一份拷贝。一般情况下,对于任何对象 x,表达式 x.clone() != x 为true，x.clone().getClass() == x.getClass() 也为true。
         *
         *  由于Object本身没有实现Cloneable接口，所以不重写clone方法并且进行调用的话会发生CloneNotSupportedException异常。
         */


        /**
         * ## notify方法是一个native方法，并且也是final的，不允许子类重写。
         *
         * 1. 唤醒一个在此对象监视器上等待的线程(监视器相当于就是锁的概念)。如果所有的线程都在此对象上等待，那么只会选择一个线程。选择是任意性的，并在对实现做出决定时发生。一个线程在对象监视器上等待可以调用wait方法。
         *
         * 2. 直到当前线程放弃对象上的锁之后，被唤醒的线程才可以继续处理。被唤醒的线程将以常规方式与在该对象上主动同步的其他所有线程进行竞争。例如，唤醒的线程在作为锁定此对象的下一个线程方面没有可靠的特权或劣势。
         *
         * ## notify方法只能被作为此对象监视器的所有者的线程来调用。一个线程要想成为对象监视器的所有者，可以使用以下3种方法:
         *
         * (1). 执行对象的同步实例方法
         *
         * (2). 使用synchronized内置锁
         *
         * (3). 对于Class类型的对象，执行同步静态方法
         *
         * ## 一次只能有一个线程拥有对象的监视器。如果当前线程没有持有对象监视器调用次方法就会抛出: @throws  IllegalMonitorStateException  if the current thread is not the owner of this object's monitor.
         *
         * 也就是 因为notify只能在拥有对象监视器的所有者线程中调用，否则会抛出IllegalMonitorStateException异常
         */
        try {
            object.notify();

        } catch (IllegalMonitorStateException e) {
            System.err.println("notify 若调用notify的线程未持有对象监视器则抛出异常：" + e);
        }

        try {
            object.notifyAll();

        } catch (IllegalMonitorStateException e) {
            System.err.println("notifyAll 若调用notify的线程未持有对象监视器则抛出异常：" + e);
        }

        try {
            /**
             * wait()方法是一个native方法，并且也是final的，不允许子类重写。
             *
             * ## wait方法会让当前线程等待直到另外一个线程调用对象的notify或notifyAll方法，或者超过参数设置的timeout超时时间。
             *
             * ## 跟notify和notifyAll方法一样,当前线程必须是此对象的监视器所有者,否则还是会发生IllegalMonitorStateException异常。
             *
             * ## wait方法会让当前线程(我们先叫做线程T)将其自身放置在对象的等待集中,并且放弃该对象上的所有同步要求。出于线程调度目的,线程T是不可用并处于休眠状态，直到发生以下四件事中的任意一件:
             *
             * 1. 其他某个线程调用此对象的notify方法，并且线程T碰巧被任选为被唤醒的线程
             *
             * 2. 其他某个线程调用此对象的notifyAll方法
             *
             * 3. 其他某个线程调用Thread.interrupt方法中断线程T
             *
             * 4. 时间到了参数设置的超时时间。如果timeout参数为0,则不会超时,会一直进行等待
             *
             * ## 持有监视器的线程调用wait让出CPU后进入该监视器的等待队列，线程处于阻塞状态，当当前持有监视器的线程调用notify()然后唤醒等待线程；该线程以常规方式与其他线程竞争，以获得在该对象上同步的权利；一旦获得对该对象的控制权，该对象上的所有其同步声明都将被恢复到以前的状态，这就是调用wait方法时的情况。然后，线程T从wait方法的调用中返回。所以，从wait方法返回时，该对象和线程T的同步状态与调用wait方法时的情况完全相同。
             */
            object.wait();
            object.wait(12, 123);
            object.wait(13);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        /**
         * finalize方法是一个protected方法，Object类的默认实现是不进行任何操作。
         *
         * 该方法的作用是实例被垃圾回收器回收的时候触发的操作，就好比 “死前的最后一波挣扎”。
         */
    }
}
