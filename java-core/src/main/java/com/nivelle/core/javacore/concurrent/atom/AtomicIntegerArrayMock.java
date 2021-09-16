package com.nivelle.core.javacore.concurrent.atom;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * int 数组 原子操作
 *
 * @author fuxinzhong
 * @date 2020/11/06
 */
public class AtomicIntegerArrayMock {

    public static void main(String[] args) {
        AtomicIntegerArray atomicIntegerArray = new AtomicIntegerArray(5);
        atomicIntegerArray.addAndGet(0, 1);
        atomicIntegerArray.addAndGet(1, 2);
        atomicIntegerArray.addAndGet(3, 4);

        System.out.println("原子操作数组1:" + atomicIntegerArray);

        atomicIntegerArray.compareAndSet(0, 1, 3);
        System.out.println("原子操作数组2:" + atomicIntegerArray);
        System.out.println(atomicIntegerArray.get(0));

        atomicIntegerArray.incrementAndGet(1);
        System.out.println("原子操作数组3:" + atomicIntegerArray);
    }
}
