package com.nivelle.base.javacore.datastructures.concurrent;


import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * CopyOnWriterArrayList
 *
 * @author nivell
 * @date 2019/06/16
 */
public class CopyOnWriterArrayListDemo {


    public static void main(String[] args) {

        /**
         *  底层是通过 ReentrantLock 来实现线程安全
         *
         *  1. 内部持有一个ReentrantLock lock = new ReentrantLock();
         *
         *  2. 底层是用volatile transient声明的数组 array
         *
         *  3. 读写分离，写时复制出一个新的数组，完成插入、修改或者移除操作后将新数组赋值给array
         */
        CopyOnWriteArrayList copyOnWriteArrayList = new CopyOnWriteArrayList();
        /**
         * public boolean add(E e) {
         *         final ReentrantLock lock = this.lock;
         *         lock.lock();
         *         try {
         *             Object[] elements = getArray();
         *             int len = elements.length;
         *             Object[] newElements = Arrays.copyOf(elements, len + 1);
         *             newElements[len] = e;
         *             setArray(newElements);
         *             return true;
         *         } finally {
         *             lock.unlock();
         *         }
         *     }
         */
        copyOnWriteArrayList.add(1);
        copyOnWriteArrayList.add(2);
        System.out.println(copyOnWriteArrayList);
        Object element = copyOnWriteArrayList.get(1);
        System.out.println(element);

        Object elementValue = copyOnWriteArrayList.get(1);
        System.err.println("copyOnWriteArrayList index 1 value is:"+ elementValue);


    }
}
