package com.nivelle.core.jdk.util;

import java.util.PriorityQueue;

/**
 * 优先队列
 *
 * @author nivelle
 * @date 2020/04/13
 */


/**
 * （1）PriorityQueue是一个小顶堆；
 * <p>
 * （2）PriorityQueue是非线程安全的；
 * <p>
 * （3）PriorityQueue不是有序的，只有堆顶存储着最小的元素；
 * <p>
 * （4）入队就是堆的插入元素的实现；
 * <p>
 * （5）出队就是堆的删除元素的实现；
 * <p>
 * (6) 入队自下而上的堆调整；出队自上而下的堆调整；
 */

public class PriorityQueueMock {
    /**
     * （1）入队不允许null元素；
     * <p>
     * （2）如果数组不够用了，先扩容；
     * <p>
     * （3）如果还没有元素，就插入下标0的位置；
     * <p>
     * （4）如果有元素了，就插入到最后一个元素往后的一个位置
     * <p>
     * （5）自下而上堆化，一直往上跟父节点比较；
     * <p>
     * （6）如果比父节点小，就与父节点交换位置，直到出现比父节点大为止；
     * <p>
     * （7）由此可见，PriorityQueue是一个小顶堆
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
        /**
         * 扩容函数：
         *
         * 1. 如果旧容量小于64,则新容量等于旧容量的2倍+2，否则 为就容量的1.5倍
         *
         * private void grow(int minCapacity) {
         int oldCapacity = queue.length;
         int newCapacity = oldCapacity + ((oldCapacity < 64) ?(oldCapacity + 2) :(oldCapacity >> 1));
         if (newCapacity - MAX_ARRAY_SIZE > 0){
         newCapacity = hugeCapacity(minCapacity);
         }
         // 将旧队列元素拷贝到新队列
         queue = Arrays.copyOf(queue, newCapacity);
         }
         *
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
         grow(i + 1); // grow(minCapacity);
         }
         // 元素个数 +1
         size = i + 1;
         if (i == 0){
         //如果还没有元素,直接插入到数组第一个位置,但是堆是从1开始的
         queue[0] = e;
         }else{
         //自下而上的堆化
         // 否则，插入元素到数组size的位置，也就是最后一个元素的下一位,这里的size不是数组大小，而是数组元素个数;然后，再做自下而上的堆化
         siftUp(i, e);
         }
         return true;
         }
         //堆调整
         private void siftUp(int k, E x) {
         if (comparator != null){// 根据是否有比较器，使用不同的方法
         siftUpUsingComparator(k, x);
         }else{
         siftUpComparable(k, x);
         }
         }
         //使用元素自己的比较器调整
         private void siftUpComparable(int k, E x) {
         Comparable<? super E> key = (Comparable<? super E>) x;
         while (k > 0) {
         //找到父节点的位置，因为是从0开始的，所以-1 再除以2
         int parent = (k - 1) >>> 1;
         //父节点的值
         Object e = queue[parent];
         //比较插入的元素与父节点的值,如果比父节点大，则跳出循环;否则父节点交换位置
         if (key.compareTo((E) e) >= 0){
         break;
         }
         //与父节点交换位置，继续循环直到比父节点大退出循环
         queue[k] = e;
         k = parent;
         }
         //最后找到应该插入的位置，放入元素
         queue[k] = key;
         }
         //使用自定义比较器调整
         private void siftUpUsingComparator(int k, E x) {
         while (k > 0) {
         //找到父节点
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

        try {
            priorityQueue.offer(null);
        } catch (NullPointerException e) {
            System.out.println("不能添加null元素：" + e);
        }
        /**
         * 删除元素
         */
        PriorityQueue<Integer> priorityQueue1 = new PriorityQueue();
        priorityQueue1.add(1);
        priorityQueue1.add(2);
        priorityQueue1.add(3);
        priorityQueue1.add(4);
        int headElement1 = priorityQueue1.remove();
        System.out.println("headElement1:" + headElement1);
        int headElement2 = priorityQueue1.poll();
        System.out.println("headElement2:" + headElement2);

        PriorityQueue<Integer> priorityQueue2 = new PriorityQueue();
        try {
            System.out.println("remove删除不存在的元素,则会抛出 NoSuchElementException 的异常");
            priorityQueue2.remove();
        } catch (Exception e) {
            System.out.println("元素不存在:" + e);
        }

        try {
            System.out.println("poll 删除不存在的元素,不会抛出异常,直接返回null");
            int result = priorityQueue2.poll();
            System.out.println("取空元素返回值:" + result);
        } catch (Exception e) {
            System.out.println("元素不存在:" + e);
        }

        int firstElement = priorityQueue2.peek();
        System.out.println("返回首个元素,不存在返回 NullPointerException:" + firstElement);

        PriorityQueue<Integer> priorityQueue3 = new PriorityQueue();

        int firstElement2 = priorityQueue3.peek();
        System.out.println("第二次返回首个元素,不存在返回 NullPointerException:" + firstElement2);

        int element = priorityQueue1.element();
        System.out.println("第三次返回首个元素,不存在返回 NoSuchElementException:" + element);

        /**
         * public E poll() {
         *  //如果是空队列,直接返回null
         if (size == 0){
         return null;
         }
         //先减队列元素个数
         int s = --size;
         modCount++;
         // 获取队首元素，用来返回
         E result = (E) queue[0];
         // 获取队尾元素暂存为x
         E x = (E) queue[s];
         //队尾设置为null,方便回收
         queue[s] = null;
         if (s != 0){
         //调整堆,重新安放队尾元素;自上而下的堆调整
         siftDown(0, x);
         }
         // 返回队首元素
         return result;
         }
         **/

        /**
         * private void siftDown(int k, E x) {
         *         if (comparator != null)
         *             siftDownUsingComparator(k, x);
         *         else
         *             siftDownComparable(k, x);
         *     }
         *
         *  ## k =0 ; x = 堆尾元素;从上到下
         *  private void siftDownComparable(int k, E x) {
         *         Comparable<? super E> key = (Comparable<? super E>)x;
         *         //中间位置
         *         int half = size >>> 1;        // loop while a non-leaf
         *         while (k < half) {
         *             //k 除以 2的1次方 + 1
         *             int child = (k << 1) + 1; // assume left child is least
         *             //左节点
         *             Object c = queue[child];
         *             //右节点
         *             int right = child + 1;
         *             //选择左右节点中值小的元素
         *             if (right < size && ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0){
         *                 c = queue[child = right];
         *             }
         *             //如果key比根节点子节点还小，则退出循环
         *             if (key.compareTo((E) c) <= 0){
         *                 break;
         *             }
         *             queue[k] = c;
         *             k = child;
         *         }
         *         //否则直接赋值到数组k位置为key值
         *         queue[k] = key;
         *     }
         */

    }
}
