package com.nivelle.base.jdk.util;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * TreeSet
 *
 * @author nivelle
 * @date 2020/04/13
 */
public class TreeSetMock {


    /**
     * public class TreeSet<E> extends AbstractSet<E> implements NavigableSet<E>, Cloneable, java.io.Serializable
     * <p>
     * public abstract class AbstractSet<E> extends AbstractCollection<E> implements Set<E>
     * <p>
     * public interface NavigableSet<E> extends SortedSet<E>
     * <p>
     * public interface SortedSet<E> extends Set<E>
     */
    public static void main(String[] args) {
        TreeSet treeSet = new TreeSet();
        treeSet.add(1);
        treeSet.add(3);
        treeSet.add(2);
        treeSet.add(5);
        treeSet.add(7);
        System.out.println(treeSet);
        /**
         * 大于等于给定值的最小节点
         */
        Object ceilingValue = treeSet.ceiling(5);
        System.out.println("ceilingValue:" + ceilingValue);
        Object higherValue = treeSet.higher(5);
        System.out.println("higherValue:" + higherValue);
        /**
         * 小于等于给你值的最大值
         */
        Object floorValue = treeSet.floor(7);
        System.out.println("floorValue:" + floorValue);
        Object lowerValue = treeSet.lower(7);
        System.out.println("lowerValue:" + lowerValue);

        Iterator iterator = treeSet.descendingIterator();
        while (iterator.hasNext()) {
            System.out.println("倒序:" + iterator.next());
        }
        Iterator iterator1 = treeSet.iterator();
        while (iterator1.hasNext()) {
            System.out.println("顺序:" + iterator1.next());
        }
        /**
         * 返回大于等于2 的子map
         */
        NavigableSet treeSet1 = treeSet.tailSet(2, true);
        System.out.println(treeSet1);
        /**
         * 返回小于等于2的子map
         */
        NavigableSet treeSet2 = treeSet.headSet(2, true);
        System.out.println(treeSet2);

        System.out.println("弹出最小值：" + treeSet.pollFirst());

        System.out.println("弹出最大值：" + treeSet.pollLast());

    }
}
