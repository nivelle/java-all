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
public class LinkedHashMapDemo {

    public static void main(String[] args) {
        /**
         * 继承自HashMap,区别与HashMap,链表是双向列表
         *
         *  accessOrder 代表迭代顺序，默认按插入顺序迭代
         *  1. true  代表按访问顺序迭代
         *  2. false 代表按插入顺序迭代
         */

        /**
         * HashMap是无序的，当我们希望有顺序地去存储key-value时，就需要使用LinkedHashMap了
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
         * 默认是安装插入顺序遍历,accessOrder将按照访问顺讯。
         * 构造函数 指定accessOrder= true 在元素被访问后将其移动到链表的末尾,最近最少使用的在前
         */

        LinkedHashMap linkedHashMap4 = new LinkedHashMap(16, 0.76F);
        linkedHashMap4.put("a", 100);
        linkedHashMap4.put("b", 200);
        System.out.println(linkedHashMap4);
        linkedHashMap4.put("a", 300);
        System.out.println("linkedHashMap4 插入顺序遍历:" + linkedHashMap4);


        LinkedHashMap linkedHashMap3 = new LinkedHashMap(16, 0.76F, true);
        linkedHashMap3.put("a", 100);
        linkedHashMap3.put("b", 200);
        System.out.println(linkedHashMap3);
        linkedHashMap3.put("a", 300);
        System.out.println("linkedHashMap3 访问顺序遍历:" + linkedHashMap3);


    }
}
