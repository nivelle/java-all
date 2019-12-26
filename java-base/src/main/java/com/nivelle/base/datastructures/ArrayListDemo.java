package com.nivelle.base.datastructures;

import com.nivelle.base.enums.MyEnum;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * ArrayList
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class ArrayListDemo {

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
            System.out.println(e.getMessage());
        }
        /**
         * 获取指定索引的元素前首先会检查索引范围是否超过了底层数组保存元素的个数
         */
        System.out.println("获取指定索引的元素:" + arrayList4.get(1));
        try {
            System.out.println("获取指定索引的元素:" + arrayList4.get(6));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        /**
         * 设置指定索引位置的值,并返回旧值
         */
        System.out.println("设置指定索引的值,并返回旧值:" + arrayList4.set(0, 5));
        System.out.println("原位置的值被设置为5:" + arrayList4.get(0));

        /**
         * 在尾部新加值时会判断是否需要扩容。第一次是默认值，发现底层数组没有元素，会第一次扩容到默认容量:10;
         * 第二次添加元素时发现elementData.length=10，不需要进行拓容,直接添加就行.
         */
        ArrayList arrayList5 = new ArrayList<String>(5);
        arrayList5.add("1");
        arrayList5.add("2");
        arrayList5.add("3");
        arrayList5.add("4");
        arrayList5.add("5");
        System.out.println(arrayList5);
        arrayList5.add("6");
        arrayList5.add("7");
        arrayList5.add("8");
        arrayList5.add("9");
        arrayList5.add("10");
        /**
         * 添加第11个元素的时候,会进行第二次扩容,扩容后的容量的为原来的1.5倍
         */
        arrayList5.add("11");
        /**
         *  1.在指定的位置插入指定的元素,底层实现是将指定位置后移，通过复制实现。
         *
         *  System.arraycopy(elementData, index, elementData, index + 1,size - index);
         */
        arrayList5.add(9, "12");
        System.out.println("经过插入的 arrayList5:" + arrayList5);
        System.out.println("移除元素前的大小:" + arrayList5.size());
        arrayList5.remove(9);
        System.out.println("被移除指定元素后的 arrayList5:" + arrayList5);
        System.out.println("移除元素后的大小:" + arrayList5.size());

        /**
         * 把所有元素设置为null,让垃圾回收器能够回收
         */
        //arrayList5.clear();

        ArrayList arrayList6 = new ArrayList();
        arrayList6.add(1);
        arrayList6.add(2);
        arrayList6.add(3);
        ArrayList arrayList7 = new ArrayList();
        arrayList7.add(4);
        arrayList7.add(5);
        arrayList7.add(6);
        arrayList7.add(3);

        /**
         * Object[] a = c.toArray();
         * int numNew = a.length;
         * ensureCapacityInternal(size + numNew);  // Increments modCount
         * System.arraycopy(a, 0, elementData, size, numNew);
         * size += numNew;
         * return numNew != 0;
         */
        //arrayList6.addAll(arrayList7);
        System.out.println("集合中添加另一个集合:" + arrayList6);
        /**
         * 只保留同时在arrayList6 和arrayList7同时存在的元素，返回值为是否修改过结构
         */
        Boolean retain = arrayList7.retainAll(arrayList6);
        System.out.println("只保留同时在arrayList6 和arrayList7同时存在的元素:" + retain + ":" + arrayList7);

        ArrayList arrayList8 = new ArrayList();
        arrayList8.add(11);
        ArrayList arrayList9 = new ArrayList();
        arrayList9.add(11);
        arrayList9.add(12);
        System.out.println("removeAll操作之前的 arrayList8:" + arrayList8);
        System.out.println("removeAll操作之前的 arrayList9:" + arrayList9);
        /**
         * 返回值是:被删除元素的集合是否修改过结构
         */
        Boolean remove = arrayList9.removeAll(arrayList8);
        System.out.println("删除同时在 arrayList9 和 arrayList8 同时存在的元素:" + remove + ":" + arrayList9);

        ListIterator<Integer> iterator = arrayList5.listIterator();
        //顺序
        while (iterator.hasNext()) {
            System.out.print(iterator.next());
            System.out.print(";");
        }
        iterator.add(12);
        System.out.println();
        System.out.println("被遍历器添加了元素的集合:" + arrayList5);
        //倒叙
        while (iterator.hasPrevious()) {
            System.out.print(iterator.previous());
            System.out.print(";");
        }

        System.out.println();

        System.out.println("replace 之前的 arrayList5:" + arrayList5);
        /**
         * ArrayList自带的遍历元素,并执行对应操作
         */
        arrayList5.replaceAll(x -> Integer.parseInt(x.toString()) + 1);
        System.out.println("replace 之后的 arrayList5:" + arrayList5);

        arrayList5.forEach(System.out::print);

        List<String> list = new ArrayList();
        list.add("1");
        list.add("2");
        /**
         * 条件删除
         */
        list.removeIf(s -> s.contains("1"));
        System.out.println(list);


        List<String> list2 = new ArrayList();
        list2.add("9");
        list2.add("10");
        list2.add("11");
        list2.add("11");
        list2.sort(new CompareAble());
        System.out.println("排序之后的 list2 集合:" + list2);
    }


}

class CompareAble implements Comparator<String> {

    @Override
    public int compare(String a, String b) {
        if (Integer.parseInt(a) >= Integer.parseInt(b)) {
            return -1;
        } else {
            return 1;
        }
    }

}
