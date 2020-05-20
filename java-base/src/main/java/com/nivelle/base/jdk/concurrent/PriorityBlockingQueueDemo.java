package com.nivelle.base.jdk.concurrent;

import com.nivelle.base.pojo.PriorityBlockingQueueComparator;

import java.lang.reflect.Field;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 优先阻塞队列
 *
 * @author nivell
 * @date 2020/04/16
 */
public class PriorityBlockingQueueDemo {

    public static void main(String[] args) {
        /**
         * 主要属性:
         *
         * ## 默认容量为11
         * 1. private static final int DEFAULT_INITIAL_CAPACITY = 11;
         *
         * ## 最大数组大小
         * 2. private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
         *
         * ## 存储元素的地方
         * 3. private transient Object[] queue;
         *
         * ## 元素个数
         * 4. private transient int size;
         *
         * ## 比较器
         * 5. private transient Comparator<? super E> comparator;
         *
         * ## 重入锁
         * 6. private final ReentrantLock lock;
         *
         * ## 非空条件
         * 7. private final Condition notEmpty;
         *
         * ## 扩容的时候使用的控制变量，CAS更新这个值，谁更新成功了谁扩容，其它线程让出CPU
         * 8.private transient volatile int allocationSpinLock;
         *
         * ## 不阻塞的优先级队列，非存储元素的地方，仅用于序列化/反序列化时
         * 9.private PriorityQueue<E> q;
         */
        PriorityBlockingQueue priorityBlockingQueue = new PriorityBlockingQueue();


        PriorityBlockingQueue priorityBlockingQueue1 = new PriorityBlockingQueue(2);

        /**
         *  public PriorityBlockingQueue(int initialCapacity, Comparator<? super E> comparator) {
         *         if (initialCapacity < 1){
         *             throw new IllegalArgumentException();
         *         }
         *         this.lock = new ReentrantLock();
         *         this.notEmpty = lock.newCondition();
         *         this.comparator = comparator;
         *         this.queue = new Object[initialCapacity];
         *     }
         */
        PriorityBlockingQueue<Float> priorityBlockingQueue2 = new PriorityBlockingQueue(4, new PriorityBlockingQueueComparator());
        /**
         * public boolean offer(E e) {
         *         ## 元素不能为空
         *         if (e == null){
         *             throw new NullPointerException();
         *         }
         *         final ReentrantLock lock = this.lock;
         *         ## 加锁
         *         lock.lock();
         *         int n;
         *         int cap;
         *         Object[] array;
         *         ##  判断是否需要扩容，即元素个数达到了数组容量
         *         while ((n = size) >= (cap = (array = queue).length)){
         *             tryGrow(array, cap);
         *         }
         *         try {
         *             Comparator<? super E> cmp = comparator;
         *             if (cmp == null){
         *                 ## 根据是否有比较器选择不通的方法,使用默认的比较方法
         *                 siftUpComparable(n, e, array);
         *             }else{
         *                 ## 使用自定义的比较器
         *                 siftUpUsingComparator(n, e, array, cmp);
         *             }
         *             ## 插入元素完毕，元素个数+1
         *             size = n + 1;
         *             ## 唤醒notEmpty条件
         *             notEmpty.signal();
         *         } finally {
         *             ## 解锁
         *             lock.unlock();
         *         }
         *         return true;
         *     }
         *  ## 默认比较器(自下而上的堆化)
         *  private static <T> void siftUpComparable(int k, T x, Object[] array) {
         *         Comparable<? super T> key = (Comparable<? super T>) x;
         *         while (k > 0) {
         *             ## 父节点index
         *             int parent = (k - 1) >>> 1;
         *             ## 父节点的值
         *             Object e = array[parent];
         *             ## 如果key大于父节点，堆化结束
         *             if (key.compareTo((T) e) >= 0){
         *                 break;
         *             }
         *             ## 和父节点交换位置，父节点的值设置到k位置处
         *             array[k] = e;
         *             ## 父节点index赋值给k
         *             k = parent;
         *         }
         *         ## 将添加值设置到原来父节点的index
         *         array[k] = key;
         *     }
         */

        /**
         * ## 扩容方法
         *  private void tryGrow(Object[] array, int oldCap) {
         *         ## 先释放锁，因为是从offer()方法的锁内部过来的；先释放锁，使用allocationSpinLock变量控制扩容的过程
         *         ## 防止阻塞的线程过多
         *         lock.unlock(); // must release and then re-acquire main lock
         *         Object[] newArray = null;
         *         ## CAS更新allocationSpinLock变量为1的线程获得扩容资格
         *         if (allocationSpinLock == 0 && UNSAFE.compareAndSwapInt(this, allocationSpinLockOffset, 0, 1)) {
         *             try {
         *                 ## 旧容量小于64则翻倍，旧容量大于64则增加一半
         *                 int newCap = oldCap + ((oldCap < 64) ? (oldCap + 2) : (oldCap >> 1));
         *                 ## 判断新容量是否溢出
         *                 if (newCap - MAX_ARRAY_SIZE > 0) {
         *                     int minCap = oldCap + 1;
         *                     if (minCap < 0 || minCap > MAX_ARRAY_SIZE)
         *                         throw new OutOfMemoryError();
         *                     newCap = MAX_ARRAY_SIZE;
         *                 }
         *                 ## 创建新数组
         *                 if (newCap > oldCap && queue == array)
         *                     newArray = new Object[newCap];
         *             } finally {
         *                 ## 相当于于解锁
         *                 allocationSpinLock = 0;
         *             }
         *         }
         *         ## 只有没进入了上面条件的才会满足这个条件;意思是让其它线程让出CPU
         *         if (newArray == null){
         *             Thread.yield();
         *         }
         *         ## 再次加锁
         *         lock.lock();
         *         ## 判断新数组创建成功并且旧数据没有被替换过
         *         if (newArray != null && queue == array) {
         *             ## 队列赋值为新数组
         *             queue = newArray;
         *             ## 并拷贝旧数组元素到新数组
         *             System.arraycopy(array, 0, newArray, 0, oldCap);
         *         }
         *     }
         */
        priorityBlockingQueue2.offer(3.0F);
        priorityBlockingQueue2.offer(8.0F);
        priorityBlockingQueue2.offer(1.0F);
        priorityBlockingQueue2.offer(2.0F);
        priorityBlockingQueue2.offer(10.0F);

        System.out.println(priorityBlockingQueue2);
        try {
            Field field = priorityBlockingQueue2.getClass().getDeclaredField("size");
            field.setAccessible(true);
            System.out.println("initialCapacity 就是size:" + field.get(priorityBlockingQueue2));
        } catch (NoSuchFieldException e) {
            System.out.println(e);
        } catch (IllegalAccessException e1) {
            System.out.println(e1);
        }
        /**
         * ## 获取堆顶元素:
         *
         *  public E take() throws InterruptedException {
         *         final ReentrantLock lock = this.lock;
         *         ## 加锁
         *         lock.lockInterruptibly();
         *         E result;
         *         try {
         *             ## 队列没有元素，就阻塞在notEmpty条件上
         *             ## 出队成功，就跳出循环
         *             while ( (result = dequeue()) == null){
         *                 notEmpty.await();
         *             }
         *         } finally {
         *             ## 解锁
         *             lock.unlock();
         *         }
         *         ## 返回出队的元素
         *         return result;
         *     }
         *
         *  ## 真正的出队
         *  private E dequeue() {
         *         int n = size - 1;
         *         ## 数组元素不足，返回null
         *         if (n < 0){
         *             return null;
         *         }
         *         else {
         *             Object[] array = queue;
         *             ## 弹出堆index=0的元素
         *             E result = (E) array[0];
         *             ## 堆尾元素index = n
         *             E x = (E) array[n];
         *             array[n] = null;
         *             Comparator<? super E> cmp = comparator;
         *             ## 并做自上而下的堆化
         *             if (cmp == null){
         *                 siftDownComparable(0, x, array, n);
         *             }else{
         *                 siftDownUsingComparator(0, x, array, n, cmp);
         *             }
         *             ## 修改size
         *             size = n;
         *             ## 返回出队的元素
         *             return result;
         *         }
         *     }
         *
         *  ## 堆化
         *  private static <T> void siftDownComparable(int k, T x, Object[] array, int n) {
         *         if (n > 0) {
         *             Comparable<? super T> key = (Comparable<? super T>)x;
         *             int half = n >>> 1;           // loop while a non-leaf
         *             ## 只需要遍历到叶子节点就够了
         *             while (k < half) {
         *                 ## 左子节点
         *                 int child = (k << 1) + 1; // assume left child is least
         *                 ## 左字节的值
         *                 Object c = array[child];
         *                 ## 右子节点
         *                 int right = child + 1;
         *                 ## 取左右子节点中最小的值
         *                 if (right < n && ((Comparable<? super T>) c).compareTo((T) array[right]) > 0){
         *                     c = array[child = right];
         *                 }
         *                 ## key如果比左右子节点都小，则堆化结束
         *                 if (key.compareTo((T) c) <= 0){
         *                     break;
         *                 }
         *                 ## 否则，交换key与左右子节点中最小的节点的位置
         *                 array[k] = c;
         *                 k = child;
         *             }
         *             ## 找到了放元素的位置，放置元素
         *             array[k] = key;
         *         }
         *     }
         */
        try {
            Float element = priorityBlockingQueue2.take();
            System.out.println("获取堆顶元素:" + element);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        /**
         * （1）PriorityBlockingQueue整个入队出队的过程与PriorityQueue基本是保持一致的；
         *
         * （2）PriorityBlockingQueue使用一个锁+一个notEmpty条件控制并发安全；
         *
         * （3）PriorityBlockingQueue扩容时使用一个单独变量的CAS操作来控制只有一个线程进行扩容；
         *
         * （4）入队使用自下而上的堆化；
         *
         * （5）出队使用自上而下的堆化；
         */
    }
}
