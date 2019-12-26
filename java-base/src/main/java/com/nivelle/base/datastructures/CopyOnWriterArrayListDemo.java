package com.nivelle.base.datastructures;


import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * CopyOnWriterArrayList
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class CopyOnWriterArrayListDemo {


    public static void main(String[] args) {

        /**
         *  底层是通过ReentrantLock来实现线程安全
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



        /**
         *
         * 底层数据结构是:CopyOnWriteArrayList
         *
         * 1. Set 大小通常保持很小，只读操作远多于可变操作，需要在遍历期间防止线程间的冲突,它是线程安全的
         *
         * 2. 因为通常需要复制整个基础数组，所以可变操作（add()、set() 和 remove() 等等）的开销很大。
         *
         * 3. 迭代器支持hasNext(), next()等不可变操作，但不支持可变 remove()等 操作
         *
         * 4. 使用迭代器进行遍历的速度很快，并且不会与其他线程发生冲突。在构造迭代器时，迭代器依赖于不变的数组快照
         */
        CopyOnWriteArraySet copyOnWriteArraySet = new CopyOnWriteArraySet();
        boolean setResult1 = copyOnWriteArraySet.add(1);
        System.out.println("copyOnWriteArraySet setResult1 is:" + setResult1);
        boolean setResult2 = copyOnWriteArraySet.add(2);
        System.out.println("copyOnWriteArraySet setResult2 is:" + setResult2);
        boolean setResult3 = copyOnWriteArraySet.add(3);
        System.out.println("copyOnWriteArraySet setResult3 is:" + setResult3);
        boolean setResult4 = copyOnWriteArraySet.add(3);
        System.out.println("copyOnWriteArraySet setResult4 is:" + setResult4);
        System.out.println("copyOnWriteArraySet is:" + copyOnWriteArraySet);

    }
}
