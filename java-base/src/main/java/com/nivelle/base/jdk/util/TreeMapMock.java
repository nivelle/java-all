package com.nivelle.base.jdk.util;

import java.util.*;

/**
 * TreeMap
 *
 * @author nivelle
 * @date 2019/06/16
 */
public class TreeMapMock {

    public static void main(String[] args) {

        /**
         * 默认按照key的自然顺序排序
         */
        TreeMap<Integer, Integer> treeMap = new TreeMap();
        treeMap.put(1, 1);
        treeMap.put(6, 2);
        treeMap.put(9, 3);
        treeMap.put(0, 4);
        System.out.println("treeMap自然key顺序排序:" + treeMap);
        TreeMap treeMap1 = new TreeMap(new MyComparator());
        treeMap1.put(1, 1);
        treeMap1.put(6, 2);
        treeMap1.put(9, 3);
        treeMap1.put(0, 4);
        System.out.println("treeMap 自定义排序:" + treeMap1);
        /**
         * 添加一个无比较器的treeMap,则使用默认key的自然排序
         */
        TreeMap treeMap2 = new TreeMap(treeMap);
        treeMap2.put(10, 5);
        System.out.println("添加一个无比较器的treeMap:" + treeMap2);

        /**
         * 添加一个有比较器的treeMap,则使用原来的比较器
         */
        TreeMap treeMap3 = new TreeMap(treeMap1);
        treeMap3.put(10, 2);
        System.out.println("添加一个有比较器的treeMap:" + treeMap3);

        /**
         * 若构造初始化的是无顺序的map,则需要重新排序
         */
        HashMap hashMap = new HashMap();
        hashMap.put(2, 2);
        hashMap.put(3, 3);
        hashMap.put(1, 1);
        /**
         * 通过hashMap构造红黑树,key必须实现Comparable接口
         */
        TreeMap<Integer, Integer> treeMap4 = new TreeMap(hashMap);
        System.out.println("添加一个无序HashMap:" + treeMap4);

        Map.Entry lowerEntry = treeMap4.lowerEntry(2);
        System.out.println("小于给定key的最大节点:" + lowerEntry);
        int lowerKey = treeMap4.lowerKey(2);
        System.out.println("小于给定key的最大key:" + lowerKey);

        Map.Entry floorEntry = treeMap4.floorEntry(2);
        System.out.println("小于等于给定key的最大节点:" + floorEntry);
        int floorKey = treeMap4.floorKey(2);
        System.out.println("小于等于给定key的最大key:" + floorKey);

        Map.Entry higherEntry = treeMap4.higherEntry(2);
        System.out.println("大于给定key的最小节点:" + higherEntry);
        int higherKey = treeMap4.higherKey(2);
        System.out.println("大于给定key的最小节点:" + higherKey);

        Map.Entry ceilingEntry = treeMap4.ceilingEntry(2);
        System.out.println("大于等于给定key的最小节点:" + ceilingEntry);
        int ceilingKey = treeMap4.ceilingKey(2);
        System.out.println("大于等于给定key的最小key:" + ceilingKey);


        Map.Entry firstEntry = treeMap4.firstEntry();
        System.out.println("最小节点:" + firstEntry);

        Map.Entry lastEntry = treeMap4.lastEntry();
        System.out.println("最大节点:" + lastEntry);

        Map.Entry firstEntryPoll = treeMap4.pollFirstEntry();
        System.out.println("弹出最小的节点:" + firstEntryPoll);

        Map.Entry pollLastEntry = treeMap4.pollLastEntry();
        System.out.println("弹出最大的节点:" + pollLastEntry);

        System.out.println("弹出最大最小元素后的tree:" + treeMap4);
        treeMap4.put(3, 2);
        treeMap4.put(4, 3);
        treeMap4.put(5, 2);
        treeMap4.put(6, 1);
        treeMap4.put(1, 6);
        System.out.println("又添加元素后的tree:" + treeMap4);

        System.out.println("返回key倒序的map:" + treeMap4.descendingMap());
        System.out.println("返回key正序的key集合:" + treeMap4.navigableKeySet());
        System.out.println("返回key反序的key集合:" + treeMap4.descendingKeySet());
        System.out.println("返回key反序的key集合:" + treeMap4.descendingKeySet());
        NavigableMap subTreeMap = treeMap4.subMap(1, true, 3, true);
        System.out.println("子treeMap 包括 from he to" + subTreeMap);
        NavigableMap headMap = treeMap4.headMap(2, true);
        System.out.println("小于toKey的NavigableMap:" + headMap);
        NavigableMap tailMap = treeMap4.tailMap(2, true);
        System.out.println("大于 fromKey的NavigableMap:" + tailMap);

        int value = treeMap4.get(4);
        System.out.println("get 方法:" + value);
        try {
            int value2 = treeMap4.get(null);
            System.out.println("get 方法:" + value2);
        } catch (Exception e) {
            System.out.println(e);
        }
        Collection collection = treeMap4.values();
        System.out.println("collection is: " + collection);

        System.out.println("删除key=6之前的树:" + treeMap);
        Integer toDelValue = treeMap.remove(6);
        System.out.println("删除key=6的值:" + toDelValue);
        System.out.println("删除key=6之后的树:" + treeMap);

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




