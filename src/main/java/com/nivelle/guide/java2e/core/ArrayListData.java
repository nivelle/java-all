package com.nivelle.guide.java2e.core;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * ArrayList
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
@Slf4j
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
         *
         * 扩容会导致modCount+1
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

        /**
         * 底层扩容函数:
         *
         *
         * private void grow(int minCapacity) {
         *         // overflow-conscious code
         *         int oldCapacity = elementData.length;
         *         int newCapacity = oldCapacity + (oldCapacity >> 1);//1.5倍
         *         if (newCapacity - minCapacity < 0)
         *             newCapacity = minCapacity;
         *         if (newCapacity - MAX_ARRAY_SIZE > 0)
         *             newCapacity = hugeCapacity(minCapacity);
         *         // minCapacity is usually close to size, so this is a win:
         *         elementData = Arrays.copyOf(elementData, newCapacity);
         *     }
         *
         * private static int hugeCapacity(int minCapacity) {
         *         if (minCapacity < 0) // overflow
         *             throw new OutOfMemoryError();
         *         return (minCapacity > MAX_ARRAY_SIZE) ?
         *             Integer.MAX_VALUE :
         *             MAX_ARRAY_SIZE;
         *     }
         *
         *
         */
        //从hugeCapacity看出,最大容量是:Integer.MAX_VALUE

        /**
         * 底层规范的最大容量大小,因为一些虚拟机会存储一些头部信息,所以会大于实际申请的容量大小
         * The maximum size of array to allocate. Some VMs reserve some header words in an array.
         * Attempts to allocate larger arrays may result in OutOfMemoryError: Requested array size exceeds VM limit
         */
        //private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

        /**
         * 底层实现: indexOf(o) >= 0;
         * 判断指定元素的索引是否存在(查找到第一额元素就终止)
         */
        System.out.println("至少包含一个指定元素:" + arrayList.contains(null));


        /**
         * 查询指定元素的索引，需要关注null元素,null元素没有方法可调用,如果查询元素为null,复杂度为O(n),否则为O(n)
         * public int indexOf(Object o) {
         *         if (o == null) {
         *             for (int i = 0; i < size; i++)
         *                 if (elementData[i]==null)
         *                     return i;
         *         } else {
         *             for (int i = 0; i < size; i++)
         *                 if (o.equals(elementData[i]))
         *                     return i;
         *         }
         *         return -1;
         *     }
         */
        System.out.println("至少包含一个指定元素,第一个的索引:" + arrayList.indexOf(null));

        System.out.println("至少包含一个指定元素,最后一个的索引:" + arrayList.lastIndexOf(null));

        System.out.println("复制一个ArrayList:" + arrayList.clone());

        /**
         * 本质上是拷贝ArrayList的底层对象数组
         */
        for (int i = 0; i < arrayList.size(); i++) {
            System.out.print("转化成一个数组" + i + ":" + arrayList.toArray()[i]);
        }
        System.out.println();

        ArrayList arrayList4 = new ArrayList<String>();
        arrayList4.add("1");
        arrayList4.add("2");
        arrayList4.add("3");
        arrayList4.add("4");
        String[] destination = new String[10];
        Object[] b = arrayList4.toArray(destination);
        System.out.println("将list中的数据拷贝到指定数组:" + b.length);
        System.out.println("被拷贝的数组未设置的数组元素为空:" + b[9]);

        String[] destination1 = new String[2];
        Object[] b1 = arrayList4.toArray(destination1);

        System.out.println("destination1 被拷贝的数组未设置的数组元素为空:" + b1[0]);
        System.out.println("destination1 被拷贝的数组未设置的数组元素为空:" + b1[1]);
        System.out.println("destination1 被拷贝的数组未设置的数组元素为空:" + b1[2]);
        System.out.println("destination1 被拷贝的数组未设置的数组元素为空:" + b1[3]);
        /**
         * 超出目标数组长度的访问会报错
         */
        try {
            System.err.println("destination1 被拷贝的数组未设置的数组元素为空:" + b1[4]);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        /**
         * 获取指定索引的元素前首先会检查索引范围是否超过了底层数组保存元素的个数
         */
        System.out.println("获取指定索引的元素:" + arrayList4.get(1));
        try {
            System.out.println("获取指定索引的元素:" + arrayList4.get(6));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        /**
         * 设置指定索引位置的值,并返回旧值
         */
        System.out.println("设置指定索引的值,并返回旧值:" + arrayList4.set(0, 5));
        System.out.println("原位置的值被设置为5:" + arrayList4.get(0));

        /**
         * 在尾部新加值
         */
        ArrayList arrayList5 = new ArrayList<String>(5);
        arrayList5.add("1");
        arrayList5.add("2");
        arrayList5.add("3");
        arrayList5.add("4");
        arrayList5.add("5");
        System.out.println(arrayList5);
        arrayList5.add("6");
        
    }
}
