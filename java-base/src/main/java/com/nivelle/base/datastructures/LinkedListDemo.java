package com.nivelle.base.datastructures;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * LinkedList
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class LinkedListDemo {

    public static void main(String[] args) {

        LinkedList linkedList1 = new LinkedList();

        /**
         * 默认在队尾添加元素，如果队列为空则为第一个元素
         */
        linkedList1.add(1);
        linkedList1.add(2);
        System.out.println("linkedList1:" + linkedList1);

        /**
         * 构造函数初始化集合，默认先调用无参数构造函数,然后调用addAll()方法添加初始数据,默认在队尾依次添加元素
         *
         * 若添加的集合为空则返回失败
         * */
        LinkedList linkedList2 = new LinkedList(linkedList1);
        System.out.println("linkedList2:" + linkedList2);

        LinkedList linkedList3 = new LinkedList();
        boolean result = linkedList3.addAll(new LinkedList());
        System.out.println("添加结果:" + result);
        System.out.println("linkedList3:" + linkedList3);

        boolean result2 = linkedList3.addAll(linkedList2);
        System.out.println("添加结果:" + result2);
        System.out.println("linkedList3:" + linkedList3);

        linkedList3.addFirst(0);
        System.out.println("在 linkedList3 列表头部添加元素:" + linkedList3);
        linkedList3.addLast(3);
        System.out.println("在 linkedList3 列表尾部添加元素:" + linkedList3);

        /**
         * 若元素为空,则抛出异常
         */
        Object firstElement = linkedList3.getFirst();
        System.out.println("返回 linkedList3 的首元素:" + firstElement);
        System.out.println("linkedList3 集合:" + linkedList3);

        Object lastElement = linkedList3.getLast();
        System.out.println("返回 linkedList3 的尾元素:" + lastElement);
        System.out.println("linkedList3 集合:" + linkedList3);

        Object removedFirstElement = linkedList3.removeFirst();
        System.out.println("返回 linkedList3 被移除的首部元素:" + removedFirstElement);
        System.out.println("linkedList3 集合:" + linkedList3);

        Object removedLastElement = linkedList3.removeLast();
        System.out.println("返回 linkedList3 被移除的尾部元素:" + removedLastElement);
        System.out.println("linkedList3 集合:" + linkedList3);

        linkedList3.addFirst(0);
        System.out.println("添加一个首部元素:" + linkedList3);
        linkedList3.addLast(3);
        System.out.println("添加一个尾部元素:" + linkedList3);

        System.out.println("判断是否包含指定元素:" + linkedList3.contains(3));

        System.out.println("返回首部元素:" + linkedList3.element());
        System.out.println("linkedList3 集合:" + linkedList3);

        linkedList3.addLast(2);
        System.out.println("返回指定元素的第一索引:" + linkedList3.indexOf(2));
        System.out.println("返回指定元素的最后一个索引:" + linkedList3.lastIndexOf(2));
        System.out.println(linkedList3.get(2).equals(linkedList3.get(4)));

        System.out.println("在首部再加一个元素:" + linkedList3.offerFirst(-1));
        System.out.println("linkedList3 集合:" + linkedList3);
        System.out.println("在尾部再加一个元素:" + linkedList3.offerLast(2));
        System.out.println("linkedList3 集合:" + linkedList3);

        System.out.println("返回尾部元素,不移除元素:" + linkedList3.peekLast());
        System.out.println("返回首部元素,不移除元素:" + linkedList3.peekFirst());
        System.out.println("linkedList3 集合:" + linkedList3);

        System.out.println("移除 linkedList3 元素 之前集合:" + linkedList3);
        System.out.println("返回首部元素,并移除" + linkedList3.pollFirst());
        System.out.println("返回尾部元素,并移除" + linkedList3.pollLast());
        System.out.println("移除 linkedList3 之后集合:" + linkedList3);

        System.out.println("当前linkedList3:" + linkedList3);


        LinkedList linkedList = new LinkedList();
        linkedList.addLast(9);
        linkedList.addLast(8);
        linkedList.addLast(7);
        linkedList.addLast(6);
        linkedList.addLast(5);

        /**
         * Iterator 顺序遍历
         */
        Iterator iterator = linkedList.descendingIterator();
        while (iterator.hasNext()) {
            System.out.print("倒序排序:" + iterator.next() + ";");
        }

        System.out.println();

        /**
         * ListIterator 倒叙遍历
         */
        ListIterator listIterator = linkedList.listIterator();
        while (listIterator.hasNext()) {
            System.out.print("顺序排序:" + listIterator.next() + ";");
        }
        System.out.println();
        while (listIterator.hasPrevious()) {
            System.out.print("倒叙排序:" + listIterator.previous() + ";");
        }
    }
}
