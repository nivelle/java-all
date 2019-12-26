package com.nivelle.base.datastructures;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * TreeMap
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class TreeMapDemo {

    public static void main(String[] args) {

        /**
         * 默认按照key的自然顺序排序
         */
        TreeMap treeMap = new TreeMap();
        treeMap.put(1, 1);
        treeMap.put(6, 2);
        treeMap.put(9, 3);
        treeMap.put(0, 4);
        System.out.println("treeMap is " + treeMap);

        TreeMap treeMap1 = new TreeMap(new MyComparator());

        treeMap1.put(1, 1);
        treeMap1.put(6, 2);
        treeMap1.put(9, 3);
        treeMap1.put(0, 4);
        System.out.println("treeMap1 is " + treeMap1);

        /**
         * 添加一个无比较器的treeMap,则使用默认key的自然排序
         */
        TreeMap treeMap2 = new TreeMap(treeMap);
        System.out.println("treeMap2 is " + treeMap2);

        /**
         * 添加一个有比较器的treeMap,则使用原来的比较器
         */
        TreeMap treeMap3 = new TreeMap(treeMap1);
        System.out.println("treeMap3 is " + treeMap3);

        /**
         * 若构造初始化的是无顺序的map,则需要重新排序
         */
        HashMap hashMap = new HashMap();
        hashMap.put(3, 3);
        hashMap.put(1, 1);
        TreeMap treeMap4 = new TreeMap(hashMap);
        System.out.println("treeMap4 is " + treeMap4);


    }

    static class MyComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            int a = (int) o1;
            int b = (int) o2;
            int result = a > b ? -1 : (a == b) ? 0 : 1;
            return result;
        }
    }
}




