package com.nivelle.base.javacore.datastructures.collection;

import java.util.PriorityQueue;

/**
 * 优先队列
 *
 * @author nivell
 * @date 2020/04/13
 */
public class PriorityQueueDemo {
    /**
     * 优先队列
     *
     * @param args
     */
    public static void main(String[] args) {

        /**
         * 1. 底层数据结构是个数组:transient Object[] queue;
         *
         * 2. 默认容量:private static final int DEFAULT_INITIAL_CAPACITY = 11;
         *
         * 3. 比较器,使用自定义比较器或者元素自己实现的比较逻辑:private final Comparator<? super E> comparator;
         */

        PriorityQueue priorityQueue = new PriorityQueue();
        System.out.println(priorityQueue.size());

        /**
         * 入队有两个方法，add(E e)和offer(E e)，两者是一致的，add(E e)也是调用的offer(E e)。
         */
        boolean addResult = priorityQueue.add(1);
        System.out.println(addResult);


        /**
         *
          public boolean offer(E e) {
            //不支持添加null元素
            if (e == null)
                throw new NullPointerException();
            modCount++;
            int i = size;
            // 元素个数达到最大容量了，扩容
            if (i >= queue.length){
                grow(i + 1);
            }
            // 元素个数 +1
            size = i + 1;
            if (i == 0){ //如果还没有元素,直接插入到数组第一个位置,但是堆是从1开始的
                queue[0] = e;
            }else{
                siftUp(i, e);//否则，插入元素到数组size的位置，也就是最后一个元素的下一位,这里的size不是数组大小，而是数组个数；然后，再做自下而上的堆化
             }
            return true;
        }
         */

        /**
         *
         *
         *
         * private void siftUp(int k, E x) {
            if (comparator != null){// 根据是否有比较器，使用不同的方法
               siftUpUsingComparator(k, x);
             }else{
                siftUpComparable(k, x);
              }
        }
        //使用元素自己的比较器
        private void siftUpComparable(int k, E x) {
            Comparable<? super E> key = (Comparable<? super E>) x;
            while (k > 0) {
                //找到父节点的位置，因为是从0开始的，所以-1 再除以2
                int parent = (k - 1) >>> 1;
                Object e = queue[parent];
                if (key.compareTo((E) e) >= 0){
                    break;
                }
                queue[k] = e;
                k = parent;
            }
            queue[k] = key;
        }
         //使用自定义比较器
        private void siftUpUsingComparator(int k, E x) {
            while (k > 0) {
                int parent = (k - 1) >>> 1;
                Object e = queue[parent];
                if (comparator.compare(x, (E) e) >= 0)
                    break;
                queue[k] = e;
                k = parent;
            }
            queue[k] = x;
        }
        *
        */
        boolean offerResult = priorityQueue.offer(1);
        System.out.println(offerResult);



    }
}
