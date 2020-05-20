package com.nivelle.base.jdk.base;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * 引用类型
 *
 * @author nivell
 * @date 2020/04/19
 */
public class ReferenceDemo {
    /**
     * Reference对象封装了其它对象的引用，可以和普通的对象一样操作，在一定的限制条件下，支持和垃圾收集器的交互。
     * <p>
     * 即可以使用Reference对象来引用其它对象，但是最后还是会被垃圾收集器回收。程序有时候也需要在对象回收后被通知，以告知对象的可达性发生变更。
     */
    public static void main(String[] args) {
        Object object = new Object();

        final WeakReference<Object> weakReference = new WeakReference(object);

        System.out.println(Objects.equals(object, weakReference.get()));

        object = null;
        System.gc();

        System.out.println(Objects.isNull(weakReference.get()));


    }
}
