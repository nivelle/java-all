package com.nivelle.base.javacore.datastructures.collection;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * LinkedList 双向队列
 *
 * @author nivell
 * @date 2019/06/16
 */
public class LinkedListDemo {
    /**
     * 1. LinkedList是一个以双向链表实现的List，它除了作为List使用，还可以作为队列或者栈来使用。
     *
     * 2. 在队列首尾添加元素很高效，时间复杂度为O(1)。在中间添加元素比较低效，首先要先找到插入位置的节点，再修改前后节点的指针，时间复杂度为O(n)。
     *
     * 3. public class LinkedList<E> extends AbstractSequentialList<E> implements List<E>, Deque<E>, Cloneable, java.io.Serializable
     */
    public static void main(String[] args) {
        /**
         *  主要内部类:
         *
         *  private static class Node<E> {
         *         E item;
         *         Node<E> next;
         *         Node<E> prev;
         *
         *         Node(Node<E> prev, E element, Node<E> next) {
         *             this.item = element;
         *             this.next = next;
         *             this.prev = prev;
         *         }
         *     }
         */
        LinkedList linkedList1 = new LinkedList();

        /**
         * 默认在队尾添加元素，如果队列为空则为第一个元素
         */
        linkedList1.add(1);
        /**
         * public boolean add(E e) {
         *         linkLast(e);
         *         return true;
         *     }
         *
         *
         * void linkLast(E e) {
         *         final Node<E> l = last;//队列尾节点
         *         final Node<E> newNode = new Node<>(l, e, null);//创建新节点，新节点的前置节点是l尾节点
         *         last = newNode; //新节点成尾节点
         *         if (l == null)
         *             first = newNode;//如果是第一个添加的元素,则first也设置为新节点
         *         else
         *             l.next = newNode;//否则把原尾节点的next设置为新节点
         *         size++;
         *         modCount++;
         *
         */
        linkedList1.add(2);//默认添加到队尾巴
        /**
         * public void addFirst(E e) {
         *         linkFirst(e);
         *     }
         *
         * private void linkFirst(E e) {
         *         final Node<E> f = first;//获取首节点
         *         final Node<E> newNode = new Node<>(null, e, f);//构建新节点，其next节点为原来的首节点;节点构造函数:Node(Node<E> prev, E element, Node<E> next)
         *         first = newNode;//新节点赋值给当前首节点
         *         if (f == null){
         *             last = newNode;
         *         } //如果尾节点为空,则新节点同时赋值给尾节点
         *         else{
         *             f.prev = newNode;//将原来首节点的prev设置为newNode
         *         }
         *         size++;
         *         modCount++;
         *     }
         *
         */
        linkedList1.addFirst(3);

        System.out.println("linkedList1:" + linkedList1);
        /**
         * 在指定位置添加元素
         *
         *
         * public void add(int index, E element) {
         *         checkPositionIndex(index);
         *
         *         if (index == size){
         *             linkLast(element);
         *         }else{
         *             linkBefore(element, node(index));
         *         }
         *     }
         *
         *  //在某个指定节点之前添加节点
         *  void linkBefore(E e, Node<E> succ) {
         *         // assert succ != null;
         *         final Node<E> pred = succ.prev;// 待添加节点的前置节点, succ = node(index);=>index的前面添加节点e
         *         final Node<E> newNode = new Node<>(pred, e, succ); // 新构建节点的前置节点是succ的前置节点，后置节点是succ，也就是要放在succ的前面
         *         succ.prev = newNode;//后继节点succ的前置节点指向新加入的节点。
         *         if (pred == null)
         *             first = newNode;//如果前置节点为空，则新加入的节点及为首节点
         *         else
         *             pred.next = newNode;//原来succ前置节点的后置节点设置为新加入的节点
         *         size++;
         *         modCount++;
         *     }
         *
         *  //根据index找到指定的节点
         *  Node<E> node(int index) {
         *         // assert isElementIndex(index);
         *         // 因为是双向链表,根据index和size的大小比，判断是在前半段还是后半段（除以2）
         *         if (index < (size >> 1)) { //在左半段,从前往后遍历
         *             Node<E> x = first;
         *             for (int i = 0; i < index; i++){
         *                 x = x.next;
         *             }
         *             return x;
         *         } else { //在后半段，从后往前遍历
         *             Node<E> x = last;
         *             for (int i = size - 1; i > index; i--){
         *                 x = x.prev;
         *             }
         *             return x;
         *         }
         *     }
         *
         *
         *
         */
        linkedList1.add(2,2);
        /**
         * 构造函数初始化集合，默认先调用无参数构造函数,
         * 然后调用addAll()方法添加初始数据,默认在队尾依次添加元素
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
        /**
         * 删除首节点:
         */
        Object removedFirstElement = linkedList3.removeFirst();
        System.out.println("返回 linkedList3 被移除的首部元素:" + removedFirstElement);
        System.out.println("linkedList3 集合:" + linkedList3);
        /**
         * 删除尾接节点：
         */
        Object removedLastElement = linkedList3.removeLast();
        System.out.println("返回 linkedList3 被移除的尾部元素:" + removedLastElement);
        System.out.println("linkedList3 集合:" + linkedList3);

        /**
         * 删除指定节点元素
         */
        Object removedIndexElement = linkedList3.remove(2);
        System.out.println("删除指定位置的元素,removedIndexElement:"+removedIndexElement);


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
