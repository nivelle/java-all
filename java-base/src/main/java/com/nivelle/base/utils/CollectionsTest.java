package com.nivelle.base.utils;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Collections
 *
 * @author nivelle
 * @date 2020/04/14
 */
public class CollectionsTest {

    public static void main(String[] args) {
        List<Integer> list1 = Lists.newArrayList();
        list1.add(1);
        list1.add(2);
        list1.add(5);
        list1.add(3);
        list1.add(4);
        //System.out.println(list1);
        Collections.sort(list1);
        /**
         * 首先需要排序，如果key在数组中，则返回搜索值的索引；否则返回-1或者”-“(插入点)。插入点是索引键将要插入数组的那一点，即第一个大于该键的元素索引
         */
        System.out.println(list1);
        int value = Collections.binarySearch(list1, 3);
        System.out.println("binarySearch has value:" + value);
        int noValue = Collections.binarySearch(list1, 8);
        System.out.println("binarySearch has no value:" + noValue);

        /**
         * 反排序
         */
        Collections.reverse(list1);
        System.out.println("reverse after:" + list1);

        Collections.sort(list1);
        System.out.println("sort after:" + list1);
        /**
         * 最大值
         */
        Integer max = Collections.max(list1);
        System.out.println("max:" + max);
        /**
         * 最小值
         */
        Integer min = Collections.min(list1);
        System.out.println("min:" + min);

        /**
         * 打乱顺序（利用随机数）
         */
        Collections.shuffle(list1);
        System.out.println("打乱 shuffle after:" + list1);

        List list2 = Lists.newArrayList();
        list2.add(1);
        list2.add(2L);
        list2.add(2.0);
        List<Integer> checkedList = Collections.checkedList(list2, Integer.class);
        System.out.println("checkedList:" + checkedList);
        List list3 = checkedList;
        //list3.add(3.0);
        System.out.println("checkedList:" + list3);

        List list = Collections.emptyList();
        System.out.println("list:" + list);

        List<Integer> list4 = new LinkedList();
        list4.add(1);
        list4.add(4);
        /**
         * 替换存在的元素为指定元素
         */
        Collections.fill(list4, 2);
        System.out.println("fill after:" + list4);

        List<Integer> list5 = Collections.singletonList(5);
        System.out.println("singletonList:" + list5);
        try {
            list5.add(6);
        } catch (UnsupportedOperationException e) {
            System.out.println(e);
        }
        System.out.println(list5);

        List<Integer> list6 = new LinkedList();
        list6.add(1);
        list6.add(2);
        list6.add(3);
        System.out.println("swap before:" + list6);
        Collections.swap(list6, 0, 1);
        System.out.println("swap after:" + list6);
        list6.add(4);
        System.out.println("rotate before:" + list6);
        Collections.rotate(list6, 2);
        System.out.println("rotate after:" + list6);
    }
}
