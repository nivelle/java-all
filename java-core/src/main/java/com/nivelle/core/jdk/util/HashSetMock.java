package com.nivelle.core.jdk.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * HashSet
 *
 * @author nivelle
 * @date 2019/10/15
 */
public class HashSetMock {


    /**
     * （1）HashSet内部使用HashMap的key存储元素，以此来保证元素不重复；
     * <p>
     * （2）HashSet是无序的，因为HashMap的key是无序的；
     * <p>
     * （3）HashSet中允许有一个null元素，因为HashMap允许key为null；
     * <p>
     * （4）HashSet是非线程安全的；
     * <p>
     * （5）HashSet是没有get()方法的；
     * <p>
     * (6) 自定义了序列化方法 writeObject 和 readObject; 底层map 是 transient的
     */
    public static void main(String[] args) {
        HashSet hashSet = new HashSet();
        /**
         * 1. 所有的value = new Object();
         *
         * 2. 添加成功 hashMap 返回null 则 hashSet 返回true,若存在则hashMap 返回非null,则hashSet返回 false
         *
         *  public boolean add(E e) {
         *         return map.put(e, PRESENT)==null;
         *     }
         */
        boolean aResult = hashSet.add("a");
        System.out.println("aResult is: " + aResult);
        boolean bResult = hashSet.add("b");
        System.out.println("bResult is: " + bResult);
        boolean cResult = hashSet.add("a");
        System.out.println("cResult is: " + cResult);
        /**
         * public Iterator<E> iterator() {
         *         return map.keySet().iterator();
         *     }
         */
        Iterator iterator = hashSet.iterator();
        while (iterator.hasNext()) {
            System.err.print(iterator.next());
        }
        /**
         * public boolean remove(Object o) {
         *         return map.remove(o)==PRESENT;
         *     }
         */
        boolean removeResult = hashSet.remove("a");
        System.out.println("删除存在的元素结果：" + removeResult);
        boolean removeResult2 = hashSet.remove("c");
        System.out.println("删除不存在的元素结果：" + removeResult2);

        hashSet.clear();
        System.out.println("clear result is:" + hashSet);
        /**
         *
         */
        hashSet.add(null);
        hashSet.add(null);
        System.out.println("hashSet 添加null:" + hashSet);
        System.out.println("hashSet size:" + hashSet.size());
        /**
         * public boolean contains(Object o) {
         *         return map.containsKey(o);
         *     }
         */
        System.out.println("hashSet contains null:" + hashSet.contains(null));

        ArrayList arrayList = new ArrayList();
        arrayList.add(1);
        arrayList.add(1);
        arrayList.add(2);

        /**
         * 默认容量最小16，或者初始化集合的
         *  public HashSet(Collection<? extends E> c) {
         *         map = new<>(Math.max((int) (c.size()/.75f) + 1, 16));
         *         addAll(c);
         *     }
         */
        HashSet hashSet1 = new HashSet(arrayList);

        System.out.println("hashSet 构造函数入参为集合子类：" + hashSet1);

        Float initCapacity = 2 / .75f + 1;
        System.out.println(initCapacity);

        HashSet hashSet2 = new HashSet(16, 0.75F);
        hashSet2.add(1);
        hashSet2.add(2);
        System.out.println("构造函数指定初始容量和加载因子:" + hashSet2);


        /**
         *  1. LinkedHashSet专用的方法
         *
         *  2. dummy是没有实际意义的, 只是为了跟上上面那个操持方法签名不同而已
         *
         *   HashSet(int initialCapacity, float loadFactor, boolean dummy) {
         *         map = new LinkedHashMap<>(initialCapacity, loadFactor);
         *     }
         */
    }
}
