package com.nivelle.guide.java2e.core;

import java.util.ArrayList;
import java.util.List;

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
    public static void main(String[] args) {

        /**
         *  Resizable【可变】-array implementation of the <tt>List</tt> interface.  Implements all optional list operations,
         *  and permits all elements, including <tt>null</tt>.  In addition to implementing the <tt>List</tt> interface,
         *  this class provides methods to manipulate【操纵】 the size of the array that is used internally to store the list.
         *  (This class is roughly equivalent to <tt>Vector</tt>, except that it is unsynchronized.)
         */

        /**
         * 可以存放任意类型的元素
         */
        List list = new ArrayList();
        list.add(7);
        list.add(MyEnum.ONE);
        list.add("nivelle");
        list.add(null);
        list.add(null);
        System.out.println(list);

        /**
         * ArrayList数据底层都保存在Object[]数组中,而且空集合会在第一次添加元素的时候进行底层扩展
         *
         * The array buffer into which the elements of the ArrayList are stored.
         * The capacity of the ArrayList is the length of this array buffer. Any
         * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
         * will be expanded to DEFAULT_CAPACITY when the first element is added.
         **/
        //transient Object[] elementData; // non-private to simplify nested class access
        List list1 = new ArrayList(18);

    }
}
