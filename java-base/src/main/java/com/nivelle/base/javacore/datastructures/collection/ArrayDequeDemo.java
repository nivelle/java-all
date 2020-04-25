package com.nivelle.base.javacore.datastructures.collection;

import java.util.ArrayDeque;

/**
 * 双端队列
 *
 * @author nivell
 * @date 2020/04/13
 */
public class ArrayDequeDemo {
    /**
     * 1. 双端队列是一种特殊的队列，它的两端都可以进出元素，故而得名双端队列。
     * <p>
     * 2. ArrayDeque是一种以数组方式实现的双端队列，它是非线程安全的。
     */
    public static void main(String[] args) {

        /**
         * public class ArrayDeque<E> extends AbstractCollection<E> implements Deque<E>, Cloneable, Serializable
         */

        /**
         * 默认构造函数,初始容量为16
         *
         * public ArrayDeque() {
         *         elements = new Object[16];
         *     }
         */
        ArrayDeque arrayDeque = new ArrayDeque();
        arrayDeque.add(1);
        arrayDeque.addFirst(2);
        arrayDeque.add(4);
        arrayDeque.addLast(3);
        System.out.println(arrayDeque);

        /**
         *  1. transient Object[] elements;//存储元素的数组
         *
         *  2. transient int head;//队列头位置=>The index of the element at the head of the deque
         *
         *  3. transient int tail;//队列尾位置=>The index at which the next element would be added to the tail
         *
         *  4. private static final int MIN_INITIAL_CAPACITY = 8;//最小初始容量
         */

        /**
         * public ArrayDeque(int numElements) {
         *         allocateElements(numElements);
         *     }
         *
         *
         * private void allocateElements(int numElements) {
         *         elements = new Object[calculateSize(numElements)];
         *     }
         *
         * //计算
         * private static int calculateSize(int numElements) {
         *         int initialCapacity = MIN_INITIAL_CAPACITY;//初始最小容量8
         *         // Find the best power of two to hold elements.
         *         // Tests "<=" because arrays aren't kept full.
         *         if (numElements >= initialCapacity) {//大于8时，计算最接近numElements的2的n次方的容量
         *             initialCapacity = numElements;
         *             initialCapacity |= (initialCapacity >>>  1);
         *             initialCapacity |= (initialCapacity >>>  2);
         *             initialCapacity |= (initialCapacity >>>  4);
         *             initialCapacity |= (initialCapacity >>>  8);
         *             initialCapacity |= (initialCapacity >>> 16);
         *             initialCapacity++;
         *
         *             if (initialCapacity < 0)   // Too many elements, must back off
         *                 initialCapacity >>>= 1;// Good luck allocating 2 ^ 30 elements
         *         }//否则直接返回最小初始容量8
         *         return initialCapacity;
         *     }
         */

        ArrayDeque arrayDeque1 = new ArrayDeque(3);
        /**
         * public void addFirst(E e) {
         *         if (e == null){ //不能设置null元素
         *             throw new NullPointerException();
         *         ]
         *         //类似hashMap 数组位置定位， x&(len-1) = x%len,head是
         *         elements[head = (head - 1) & (elements.length - 1)] = e;
         *         if (head == tail){ //如果首位相等了,扩容两倍。
         *             doubleCapacity();
         *         }
         *     }
         */
        arrayDeque1.addFirst(1);
        arrayDeque1.addFirst(2);
        System.out.println(arrayDeque1);
        System.out.println(arrayDeque1.getFirst());
        /**
         * 扩容: ![扩容](https://s1.ax1x.com/2020/04/25/J6PdRU.png)
         *
         * private void doubleCapacity() {
         *         assert head == tail;
         *         int p = head;//头指针的位置
         *         int n = elements.length;//旧数组长度
         *         int r = n - p; // 头指针离数组尾的距离
         *         int newCapacity = n << 1;//新容量是旧容量的2倍
         *         if (newCapacity < 0){ //检查是否益处
         *             throw new IllegalStateException("Sorry, deque too big");
         *         }
         *         Object[] a = new Object[newCapacity];//新建新容量大小的数组
         *         System.arraycopy(elements, p, a, 0, r);//将旧数组p以及p元素之后元素拷贝到新数组
         *         System.arraycopy(elements, 0, a, r, p);//将旧数组下标0开始元素到head之间的元素拷贝到新数组中
         *         elements = a;//赋值为新数组
         *         head = 0;//head指向0
         *         tail = n;//tail指向旧数组长度表示的位置
         *     }
         */

        arrayDeque1.addLast(4);
        arrayDeque1.addLast(5);
        /**
         * public void addLast(E e) {
         *         if (e == null)
         *             throw new NullPointerException();
         *         elements[tail] = e;
         *         //在尾指针位置放入元素，tail指向的是队列最后一个元素的下一个位置
         *         if ( (tail = (tail + 1) & (elements.length - 1)) == head){
         *             doubleCapacity();
         *         }
         *     }
         */
        System.out.println("arrayDeque1:" + arrayDeque1);
        /**
         *  public E getFirst() {
         *         @SuppressWarnings("unchecked")
         *         E result = (E) elements[head];
         *         if (result == null)
         *             throw new NoSuchElementException();
         *         return result;
         *     }
         */
        System.out.println("get first:"+arrayDeque1.getFirst());
        /**
         * public E getLast() {
         *         @SuppressWarnings("unchecked")
         *         E result = (E) elements[(tail - 1) & (elements.length - 1)];
         *         if (result == null)
         *             throw new NoSuchElementException();
         *         return result;
         *     }
         */
        System.out.println("get last:"+arrayDeque1.getLast());

        /**
         * public E pollFirst() {
         *         int h = head;
         *         @SuppressWarnings("unchecked")
         *         E result = (E) elements[h];//取出队首元素
         *         // Element is null if deque empty
         *         if (result == null)
         *             return null;
         *         elements[h] = null;     // 将原队列头置为null
         *         head = (h + 1) & (elements.length - 1); //重新设置新的head
         *         return result;
         *     }
         */
        System.out.println("出队操作:"+arrayDeque1.pollFirst());

        /**
         * public E pollLast() {
         *         int t = (tail - 1) & (elements.length - 1);
         *         @SuppressWarnings("unchecked")
         *         E result = (E) elements[t];
         *         if (result == null){
         *             return null;
         *         }
         *         elements[t] = null;
         *         tail = t;
         *         return result;
         *     }
         */
        System.out.println("出队操作:"+arrayDeque1.pollLast());

        arrayDeque1.push(10);
        System.out.println("作为栈来使用:"+arrayDeque1.poll());

    }
}
