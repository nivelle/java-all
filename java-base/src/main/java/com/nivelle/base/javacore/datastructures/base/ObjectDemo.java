package com.nivelle.base.javacore.datastructures.base;

import java.lang.reflect.Method;

/**
 * 所有java类的父类
 *
 * @author nivell
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

        object.notify();

        object.notifyAll();

        try {
            /**
             * wait()方法是一个native方法，并且也是final的，不允许子类重写。
             */
            object.wait();
            object.wait(12, 123);
            object.wait(13);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        /**
         * finalize方法是一个protected方法，Object类的默认实现是不进行任何操作。
         *
         * 该方法的作用是实例被垃圾回收器回收的时候触发的操作，就好比 “死前的最后一波挣扎”。
         */
    }
}
