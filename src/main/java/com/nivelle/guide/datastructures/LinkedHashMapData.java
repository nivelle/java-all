package com.nivelle.guide.datastructures;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * LinkedHashMap
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class LinkedHashMapData {

    public static void main(String[] args) {
        /**
         * 继承自HashMap,区别与HashMap,链表是双向列表
         *
         *  accessOrder 代表迭代顺序，默认按插入顺序迭代
         *  1. true  代表按访问顺序迭代
         *  2. false 代表按插入顺序迭代
         */
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put(1, 1);
        linkedHashMap.put(2, 2);
        System.out.println("linkedHashMap:" + linkedHashMap);

        LinkedHashMap linkedHashMap1 = new LinkedHashMap(4, 0.5F);
        linkedHashMap1.put(3, 3);
        linkedHashMap1.put(4, 4);
        System.out.println("linkedHashMap1:" + linkedHashMap1);


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
         * 值集合
         */
        Collection collections = linkedHashMap1.values();
        System.out.println("linkedHashMap1 to list:" + collections);

        /**
         * 1. 依次遍历, 判断方法:if (v == value || (value != null && value.equals(v)))
         *
         * 2.
         *
         */
        System.out.println("是否包含指定的值:"+linkedHashMap1.containsValue(4));


    }
}
