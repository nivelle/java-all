package com.nivelle.core.jdk.util;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * linkedHashSet
 *
 * @author nivelle
 * @date 2019/10/15
 */
public class LinkedHashSetMock {


    /**
     * （1）LinkedHashSet的底层使用LinkedHashMap存储元素。
     *
     * （2）LinkedHashSet是有序的，它是按照插入的顺序排序的。
     *
     *  (3) crud用的都是hashSet的方法
     */

    /**
     * 1. hashSet 无序,linkedHash有序
     * <p>
     * 2. linkedHashSet继承自从 HashSet,所有构造方法调用的父类的HashSet的 构造方法,用linkedHashMap来构造linkedHashSet
     */
    public static void main(String[] args) {
        /**
         * HashSet(int initialCapacity, float loadFactor, boolean dummy) {
         *         map = new LinkedHashMap<>(initialCapacity, loadFactor);
         *     }
         *
         * public LinkedHashMap(int initialCapacity, float loadFactor) {
         *         super(initialCapacity, loadFactor);
         *         accessOrder = false;//只能按照插入顺序访问而不能按照访问顺序访问
         *     }
         */
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        linkedHashSet.add(1);
        linkedHashSet.add(2);
        System.out.println(linkedHashSet);

        linkedHashSet.add(3);
        linkedHashSet.add(0);
        System.out.println("设置顺序访问" + linkedHashSet);

        Iterator iterator = linkedHashSet.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

}
