package com.nivelle.guide.java2e.core;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * ArrayList
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class ArrayListData {


    /**
     * public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {

        /**
         *  Resizable【可变】-array implementation of the <tt>List</tt> interface.  Implements all optional list operations,
         *  and permits all elements, including <tt>null</tt>.  In addition to implementing the <tt>List</tt> interface,
         *  this class provides methods to manipulate【操纵】 the size of the array that is used internally to store the list.
         *  (This class is roughly equivalent to <tt>Vector</tt>, except that it is unsynchronized.)
         */

        /**
         * 可以存放任意类型的元素
         */
        ArrayList arrayList = new ArrayList();
        arrayList.add(7);
        arrayList.add(MyEnum.ONE);
        arrayList.add("nivelle");
        arrayList.add(null);
        arrayList.add(null);
        System.out.println(arrayList);

        /**
         * ArrayList数据底层都保存在Object[]数组中,而且空集合会在第一次添加元素的时候进行底层扩展
         *
         * The array buffer into which the elements of the ArrayList are stored.
         * The capacity of the ArrayList is the length of this array buffer. Any
         * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
         * will be expanded to DEFAULT_CAPACITY when the first element is added.
         **/
        //transient Object[] elementData; // non-private to simplify nested class access
        ArrayList arrayList2 = new ArrayList(18);
        arrayList2.add(null);

        /**
         * The size of the ArrayList (the number of elements it contains).
         * size返回的是数组中元素的数量
         */
        System.out.println("当前list集合的大小size():" + arrayList.size());


        Field field = arrayList2.getClass().getDeclaredField("elementData");
        field.setAccessible(true);
        Object[] result = (Object[]) field.get(arrayList2);
        System.out.println("通过反射获取当前 arrayList2 的容量大小" + result.length);
        System.out.println("当前 arrayList2 集合的大小size():" + arrayList2.size());
        /**
         * 释放没有使用的空间容量,会导致modCount +1
         */
        arrayList2.trimToSize();
        Object[] trimResult = (Object[]) field.get(arrayList2);
        System.out.println("trimToSize:通过反射获取当前arrayList的容量大小" + trimResult.length);
        System.out.println("当前 arrayList2 集合的大小size():" + arrayList2.size());

        /**
         * 如果不是初始空集合,则按照指定的minCapacity进行扩容;如果是初始空集合,则只有大于默认的容量
         *
         * DEFAULT_CAPACITY = 10 才进行扩容,否则不扩容。
         */
        ArrayList arrayList3 = new ArrayList();
        //必须进行一次添加操纵才有默认的容量
        arrayList3.add(null);
        arrayList3.ensureCapacity(9);
        Field field3 = arrayList3.getClass().getDeclaredField("elementData");
        field3.setAccessible(true);
        Object[] ensureResult = (Object[]) field3.get(arrayList3);
        System.out.println("ensureCapacity:通过反射获取当前 arrayList3 的容量大小" + ensureResult.length);
        System.out.println("当前 arrayList3 集合的大小size():" + arrayList3.size());


    }
}
