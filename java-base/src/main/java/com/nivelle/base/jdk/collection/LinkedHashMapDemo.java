package com.nivelle.base.jdk.collection;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * LinkedHashMap
 * <p>
 * LinkedHashMap内部维护了一个双向链表，能保证元素按插入的顺序访问，也能以访问顺序访问，可以用来实现LRU缓存策略。
 *
 * @author nivell
 * @date 2019/06/16
 */
public class LinkedHashMapDemo {

    public static void main(String[] args) {
        /**
         * 继承自HashMap,区别与HashMap,链表是双向列表
         *
         *  accessOrder 代表迭代顺序，默认按插入顺序迭代(afterNodeAccess:在put已经存的元素或者get时调用)
         *  1. true  代表按访问顺序存迭代
         *  2. false 代表按插入顺序存储迭代,默认值
         */

        /**
         * HashMap是无序的，当我们希望有顺序地去存储key-value时，就需要使用LinkedHashMap了
         */
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put(2, 1);
        linkedHashMap.put(1, 2);
        System.out.println("插入顺序访问->linkedHashMap:" + linkedHashMap);

        LinkedHashMap linkedHashMap1 = new LinkedHashMap(4, 0.5F, false);
        linkedHashMap1.put(3, 3);
        linkedHashMap1.put(4, 4);
        System.out.println("插入顺序访问->linkedHashMap1:" + linkedHashMap1);
        /**
         * entry视图
         */
        Set<Map.Entry> entrySet = linkedHashMap1.entrySet();

        entrySet.forEach(x -> {
            System.out.print(x.getKey() + ":");
            System.out.println(x.getValue());
        });
        /**
         * key视图
         */
        Set set = linkedHashMap1.keySet();

        set.forEach(x -> {
            System.out.print(x);
            System.out.print("?");
            System.out.println(linkedHashMap1.get(x));
        });
        /**
         * ## 插入一个HashMap节点后-》是否要删除最老元素,在插入节点之后的动作:
         *
         * ```
         * void afterNodeInsertion(boolean evict) {
         *         LinkedHashMap.Entry<K,V> first;
         *         if (evict && (first = head) != null && removeEldestEntry(first)) {
         *             K key = first.key;
         *             removeNode(hash(key), key, null, false, true);
         *         }
         *     }
         *
         * ```
         * （1）如果evict为true，且头节点不为空，且确定移除最老的元素，那么就调用HashMap.removeNode()把头节点移除（这里的头节点是双向链表的头节点，而不是某个桶中的第一个元素);
         *
         * （2）HashMap.removeNode()从HashMap中把这个节点移除之后，会调用afterNodeRemoval()方法；
         *
         * （3）afterNodeRemoval()方法在LinkedHashMap中也有实现，用来在移除元素后修改双向链表；//把节点从双向链表中删除
         *
         * （4）默认removeEldestEntry()方法返回false，也就是不删除元素。
         *
         *
         * ## 删除一个HashMap节点后-》双向链表删除这个节点:
         *
         *  ```
         *  void afterNodeRemoval(Node<K,V> e) {
         *         LinkedHashMap.Entry<K,V> p =(LinkedHashMap.Entry<K,V>)e;
         *         LinkedHashMap.Entry<K,V> b = p.before;
         *         LinkedHashMap.Entry<K,V> a = p.after;
         *         p.before = p.after = null;
         *         if (b == null)
         *             head = a;
         *         else
         *             b.after = a;
         *         if (a == null)
         *             tail = b;
         *         else
         *             a.before = b;
         *     }
         *
         *  ```
         *
         * ## afterNodeAccess(Node e)方法 //在节点访问之后被调用,主要在put()已经存在的元素或get()时被调用,如果accessOrder为true，调用这个方法把访问到的节点移动到双向链表的末尾。
         *
         * ```
         * void afterNodeAccess(Node<K,V> e) { // move node to last
         *         LinkedHashMap.Entry<K,V> last;
         *         if (accessOrder && (last = tail) != e) { // 如果accessOrder为true，并且访问的节点不是尾节点
         *             LinkedHashMap.Entry<K,V> p = (LinkedHashMap.Entry<K,V>)e;
         *             LinkedHashMap.Entry<K,V> b = p.before;
         *             LinkedHashMap.Entry<K,V> a = p.after;
         *             // 把p节点从双向链表中移除
         *             p.after = null;
         *             if (b == null){
         *                 head = a;
         *             }else{
         *                 b.after = a;
         *             }
         *             if (a != null){
         *                 a.before = b;
         *             }else{
         *                 last = b;
         *              }
         *             //把p节点移到双向链表的尾部
         *             if (last == null)
         *                 head = p;
         *             else {
         *                 p.before = last;
         *                 last.after = p;
         *             }
         *             //尾节点等于p
         *             tail = p;
         *             ++modCount;
         *         }
         *     }
         *
         * ```
         */
        Collection collections = linkedHashMap1.values();
        System.out.println("linkedHashMap1 to list:" + collections);

        /**
         * 1. 依次遍历, 判断方法:if (v == value || (value != null && value.equals(v)))
         *
         * 2. 从前往后遍历
         *
         */
        System.out.println("是否包含指定的值:" + linkedHashMap1.containsValue(4));
        LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        linkedHashMap2.put(1, 1);
        linkedHashMap2.put(2, 3);

        /**
         * 只能操作value值
         */
        System.out.println("linkedHashMap2 before :" + linkedHashMap2);
        linkedHashMap2.replaceAll((x, y) -> y.hashCode() + x.hashCode());
        System.out.println("linkedHashMap2 after :" + linkedHashMap2);

        /**
         * 移除指定键的元素
         */
        Object oldValue = linkedHashMap2.remove(1);
        System.out.println(oldValue);


        /**
         * 默认是按照插入顺序遍历,accessOrder将按照访问顺序。
         *
         * 构造函数 指定 accessOrder= true 在元素被访问后将其移动到链表的末尾,最近最少使用的在前
         */

        LinkedHashMap linkedHashMap4 = new LinkedHashMap(16, 0.76F);
        linkedHashMap4.put("a", 100);
        linkedHashMap4.put("b", 200);
        System.out.println(linkedHashMap4);
        linkedHashMap4.put("a", 300);
        System.out.println("最近使用linkedHashMap4 插入顺序遍历:" + linkedHashMap4);
        /**
         * LRU缓存策略的关键
         */
        LinkedHashMap linkedHashMap3 = new LinkedHashMap(16, 0.76F, true);
        linkedHashMap3.put("a", 100);
        linkedHashMap3.put("b", 200);
        System.out.println(linkedHashMap3);
        linkedHashMap3.put("a", 300);
        System.out.println("最近最少使用linkedHashMap3 访问顺序遍历:" + linkedHashMap3);

    }
}
