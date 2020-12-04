package com.nivelle.base.jdk.util;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Arrays 和 Collections
 *
 * @author nivelle
 * @date 2019/10/29
 */
public class ArrayDemo {

    public static void main(String[] args) {


        /**
         * 严格意义上说返回的是数组的一个视图，共享底层源数组
         *
         * （1）该方法不适用于基本数据类型（byte,short,int,long,float,double,boolean）
         *
         * （2）该方法将数组与列表链接起来，当更新其中之一时，另一个自动更新
         *
         * （3）不支持add和remove方法(该ArrayList是Arrays的一个内部类)
         */
        int[] intArray = {5, 1, 2, 3, 4, 0};
        for (int i = 0; i < intArray.length; i++) {
            System.out.println("遍历数组：" + intArray[i]);
        }
        List list = Arrays.asList(intArray);
        System.out.println("排序前:" + list);


        /**
         * 只有元素是对象类型的才能够转换为List,因为用到了范型
         */
        Integer[] integerArray = {1, 2, 3, 4};
        List<Integer> list2 = Arrays.asList(integerArray);
        System.out.println("list2:" + list2);


        List<Integer> integerList = new ArrayList<>();
        integerList.add(1);
        integerList.add(2);
        integerList.add(2);
        System.out.println("integerList is:" + integerList);

        /**
         * Collection.toArray() 方法返回的是当前集合的拷贝,修改不会修改源信息
         *
         *  public Object[] toArray() {
         *         // Estimate size of array; be prepared to see more or fewer elements
         *         Object[] r = new Object[size()];
         *         Iterator<E> it = iterator();
         *         for (int i = 0; i < r.length; i++) {
         *             if (! it.hasNext()) // fewer elements than expected
         *                 return Arrays.copyOf(r, i);
         *             r[i] = it.next();
         *         }
         *         return it.hasNext() ? finishToArray(r, it) : r;
         *     }
         */
        Object[] objects = integerList.toArray();
        objects[2] = 3;

        integerList.set(0, 2);
        System.out.println("integerList after set is:" + integerList);

        for (int i = 0; i < objects.length; i++) {
            System.out.print("object" + (i + 1) + ":" + objects[i] + " ");
        }

        /**
         * Collections排序:底层使用的是Arrays.sort()
         */
        List<Integer> intArraySort = Lists.newArrayList();
        intArraySort.add(1);
        intArraySort.add(2);
        intArraySort.add(3);
        intArraySort.add(5);
        intArraySort.add(0);
        System.out.println();
        System.out.println("排序前:" + intArraySort);


        /**
         * Collections.sort()->List.sort()->Arrays.sort()->TimSort()
         *
         * 底层实现都是 TimSort 实现的，这是jdk1.7新增的，以前是归并排序。TimSort算法就是找到已经排好序数据的子序列,然后对剩余部分排序,然后合并起来;
         */

        /**
         * TimSort 是一个归并排序做了大量优化的版本。对归并排序排在已经反向排好序的输入时表现O(n2)的特点做了特别优化。对已经正向排好序的输入减少回溯。
         * 对两种情况混合（一会升序，一会降序）的输入处理比较好。
         */
        rankSort(intArraySort);
        System.out.println("排序后:" + intArraySort);

        /**
         * 调用的是:Arrays.sort(a, (Comparator) c);
         */
        intArraySort.sort((x, y) -> {
            if (x > y) {
                return 0;
            } else if (x < y) {
                return -1;
            } else {
                return 1;
            }
        });
        System.out.println("第二次排序后:" + intArraySort);
        System.out.println("第三次排序后:");
        Arrays.sort(intArraySort.stream().mapToInt(x -> x.intValue()).toArray());
        System.out.println(intArraySort);

        String astr = "1";
        System.out.println("切割：" + astr.split(",")[0]);

    }

    private static void rankSort(List<Integer> list) {
        /**
         * Collections.sort()->List.sort()->Arrays.sort()->TimSort()
         */
        Collections.sort(list, (x, y) -> {
            if (x > y) {
                return -1;
            } else if (x < y) {
                return 1;
            } else {
                return 0;
            }
        });
    }


}
