package com.nivelle.guide.java2e.jdk;

import java.util.HashMap;

/**
 * HashMap
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class HashMapData {


    public static void main(String[] args) throws Exception {

        /**
         * Hash table based implementation of the <tt>Map</tt> interface.  This implementation provides all of the optional map operations,
         * and permits<tt>null</tt> values and the <tt>null</tt> key.
         * The HashMap class is roughly equivalent to HashTable, except that it is unsynchronized and permits nulls.)
         * //hashMap 允许 null key 和 null value ,大体上等于 hashTable, 不同点在于hashMap是非同步的以及允许空值
         * This class makes no guarantees as to the order of the map; in particular,
         * it does not guarantee that the order will remain constant over time.
         *
         */

        /**
         * 对于 get 和 put 操纵是常量时间级别的
         *
         * Iteration【迭代】 over collection views requires time proportional【成比例】 to the "capacity" of the HashMap instance (the number of buckets)
         * plus its size (the number of key-value mappings).it's very important not to set the initial capacity too high【初始容量不能太高】 (or the load factor too low)
         * if iteration performance is important.
         */


        /**
         * 影响hashMap性能的两个属性是：初始容量(initial capacity)和加载因子(load factor)
         *
         * When the number of entries in the hash table exceeds【超过】 the product of the load factor and the current capacity,
         * the hash table is rehashed (that is, internal data structures are rebuilt) so that the hash table has approximately twice【大约两倍】 the
         * number of buckets.
         */


        /**
         * 根据时间和空间的因素,默认的加载因子是 0.75
         */

        /**
         * hashMap 非同步，需要外部同步来实现同步。或者可以使用 Collections.synchronizedMap
         *
         * 在返回迭代器之后,除非通过迭代器的remove方法,其他改变hashMap结构的方法都有可能会在迭代期间抛出 ConcurrentModificationException 异常。
         *
         * 采用快速失败机制,而不是在一个不确定的未来时机抛出异常。同时快速失败机制并不是可靠的,仅仅是力所能及的抛出异常。不能依赖快速失败机制来
         *
         * Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally speaking, impossible to make any hard guarantees in the
         * presence of unsynchronized concurrent modification.  Fail-fast iterators throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
         * Therefore, it would be wrong to write a program that depended on this exception for its correctness: the fail-fast behavior of iterators
         * should be used only to detect bugs.
         */

        /**
         * treeNode 默认hashCode 排序,如果实现了Comparable 接口,则按照比较器进行排序。
         */

        /**
         *
         *    //初始化默认容量2的4次方,必须是2的整数倍
         *    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
         *
         *     //最大为2的30次方
         *     static final int MAXIMUM_CAPACITY = 1 << 30;
         *
         *     //默认加载因子
         *    static final float DEFAULT_LOAD_FACTOR = 0.75f;
         *
         *
         *     //链表数超过8个则考虑进行转换为红黑树
         *     static final int TREEIFY_THRESHOLD = 8;
         *
         *     //当执行resize操作时，当桶中bin的数量少于UNTREEIFY_THRESHOLD时使用链表来代替树。默认值是6
         *     static final int UNTREEIFY_THRESHOLD = 6;
         *
         *     //要转换为红黑树,桶的树木最少是64
         *     static final int MIN_TREEIFY_CAPACITY = 64;
         */


        /**
         * 默认无参构造函数,初始化的加载因子:0.75
         */
        HashMap hashMap = new HashMap();

        hashMap.put("1", "nivelle");
        hashMap.put("2", "jessy");
        System.out.println("无参初始化HashMap" + hashMap);


        /**
         * 默认加载因子:0.75
         *
         * final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
         *         int s = m.size();
         *         if (s > 0) {
         *             if (table == null) { // pre-size
         *                 float ft = ((float)s / loadFactor) + 1.0F;
         *                 int t = ((ft < (float)MAXIMUM_CAPACITY) ?
         *                          (int)ft : MAXIMUM_CAPACITY);
         *                 if (t > threshold)
         *                     //table的容量是离t最近的2的整次幂
         *                     threshold = tableSizeFor(t);
         *             }
         *             else if (s > threshold)
         *                 //若table已经初始化,容量不够则需要进行扩容
         *                 resize();
         *             for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
         *                 K key = e.getKey();
         *                 V value = e.getValue();
         *                 putVal(hash(key), key, value, false, evict);
         *             }
         *         }
         *     }
         */
        HashMap hashMap1 = new HashMap(hashMap);
        System.out.println("初始化参数是已经存在的HashMap" + hashMap1);

        HashMap hashMap2 = new HashMap();
        hashMap2.put("3", "xihui");
        hashMap2.put("4", "wangzheng");

        /**
         *
         * The table, initialized on first use, and resized as necessary. When allocated, length is always a power of two.
         * We also tolerate length zero in some operations to allow bootstrapping mechanics that are currently not needed.)
         *  transient Node<K, V>[] table;
         */
        HashMap hashMap3 = new HashMap(hashMap);


        System.out.println(hashMap1);

    }


}
