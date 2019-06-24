package com.nivelle.guide.java2e.jdk;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class HashMapData {


    public static void main(String[] args) {

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
        Map map = new HashMap();


    }


}
