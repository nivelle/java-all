package com.nivelle.base.utils;

import com.google.common.collect.Lists;

import java.util.Collections;
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
        System.out.println(list1);

        int value = Collections.binarySearch(list1, 3);
        System.out.println(value);

        Collections.reverse(list1);
        System.out.println("reverse after:" + list1);

        Collections.sort(list1);
        System.out.println("sort after:" + list1);

        Integer max = Collections.max(list1);
        System.out.println("max:" + max);

        Integer min = Collections.min(list1);
        System.out.println("min:" + min);

        /**
         * 打乱顺序
         */
        Collections.shuffle(list1);
        System.out.println("打乱 shuffle after:" + list1);

    }
}
