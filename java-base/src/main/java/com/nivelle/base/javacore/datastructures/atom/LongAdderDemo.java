package com.nivelle.base.javacore.datastructures.atom;

import java.util.concurrent.atomic.LongAdder;

/**
 * LongAdder
 *
 * @author nivell
 * @date 2020/04/12
 */
public class LongAdderDemo {

    /**
     * ![LongAdder](https://s1.ax1x.com/2020/04/28/J42eIJ.jpg)
     * <p>
     * LongAdder是java8中新增的原子类,在多线程环境中,它比AtomicLong性能要高出不少,特别是写多的场景。
     */
    public static void main(String[] args) {
        /**
         * LongAdder的原理是:
         *
         * 1. 在最初无竞争时，只更新base的值，
         *
         * 2. 当有多线程竞争过时通过分段的思想，让不同的线程更新不同的段;
         *
         * 3. 最后把这些段相加就得到了完整的LongAdder存储的值。
         */

        /**
         * public class LongAdder extends Striped64 implements Serializable
         *
         * 1. 继承自Striped64
         */
        LongAdder longAdder = new LongAdder();

        longAdder.add(1);
        longAdder.add(2);
        System.out.println(longAdder);

    }
}
/***
 * 父类主要属性:
 *
 * 1. transient volatile Cell[] cells;//储存各个段的值
 *
 * 2. transient volatile long base;//最初无竞争的时候使用的(特殊段)
 *
 * 3. transient volatile int cellsBusy;//标记当前是否有线程正在创建或者扩容cells,或者正在创建Cell,通过CAS更新该值,相当于一个锁
 *
 *
 *
 *
 *
 //Striped64中的内部类，使用@sun.misc.Contended 注解，说明里面的值消除伪共享
 @sun.misc.Contended
 static final class Cell {

    volatile long value;//存储元素的值，使用volatile修饰保证可见性

    Cell(long x) {
        value = x;
    }

    final boolean cas(long cmp, long val) {
        return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);//cas更新value
    }

    // Unsafe实例
    private static final sun.misc.Unsafe UNSAFE;
 // value字段的偏移量


 private static final long valueOffset;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> ak = Striped64.Cell.class;
            valueOffset = UNSAFE.objectFieldOffset(ak.getDeclaredField("value"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
 **/
