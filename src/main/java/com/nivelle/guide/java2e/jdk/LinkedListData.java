package com.nivelle.guide.java2e.jdk;

import java.util.LinkedList;

/**
 * LinkedList
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class LinkedListData {

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


    }
}
