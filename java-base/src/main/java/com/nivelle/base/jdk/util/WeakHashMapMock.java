package com.nivelle.base.jdk.util;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * WeakHashMap
 *
 * @author nivelle
 * @date 2019/10/15
 */
public class WeakHashMapMock {

    /**
     * WeakHashMap没有实现Clone和Serializable接口，所以不具有克隆和序列化的特性。
     * <p>
     * WeakHashMap因为gc的时候会把没有强引用的key回收掉，所以注定了它里面的元素不会太多，因此也就不需要像HashMap那样元素多的时候转化为红黑树来处理了。
     * <p>
     * 因此，WeakHashMap的存储结构只有（数组 + 链表）。
     * <p>
     * <p>
     * （1）WeakHashMap使用（数组 + 链表）存储结构；
     * <p>
     * （2）WeakHashMap中的key是弱引用，gc的时候会被清除；
     * <p>
     * （3）每次对map的操作都会剔除失效key对应的Entry；
     * <p>
     * （4）使用String作为key时，一定要使用new String()这样的方式声明key，才会失效，其它的基本类型的包装类型是一样的；
     * <p>
     * （5）WeakHashMap常用来作为缓存使用；
     */
    public static void main(String[] args) {
        Map<String, Integer> map = new WeakHashMap<>(3);

        /**
         * //引用队列，当弱键失效的时候会把Entry添加到这个队列中;Reference queue for cleared WeakEntries
         * private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
         *
         * WeakHashMap里声明了一个queue,Entry继承WeakReference,构造函数中用key和queue关联构造一个weakReference,
         * 当key不再被使用gc后会自动把把key注册到queue中
         */
        map.put(new String("1"), 1);
        map.put(new String("2"), 2);
        map.put(new String("3"), 3);
        map.put("4", 4);
        // 使用key强引用"3"这个字符串
        String key = null;
        for (String s : map.keySet()) {
            // 这个"3"和new String("3")不是一个引用
            if (s.equals("3")) {
                key = s;
                System.out.println("key is:" + key);
            }
        }
        System.out.println("GC before" + map);
        System.gc();
        System.out.println("GC after" + map);
        map.put("5", 5);
        System.out.println("gc after insert" + map);
        // key与"3"的引用断裂
        key = null;
        System.out.println("key is null after" + map);
        System.gc();
        System.out.println("key is null and gc after" + map);
    }

}


/**
 * public class WeakReference<T> extends Reference<T> {
 * public WeakReference(T referent, ReferenceQueue<? super T> q) {
 * super(referent, q);
 * }
 * }
 * <p>
 * public abstract class Reference<T> {
 * Reference(T referent, ReferenceQueue<? super T> queue) {
 * this.referent = referent;
 * this.queue = (queue == null) ? ReferenceQueue.NULL : queue;
 * }
 * }
 * <p>
 * private static class Entry<K, V> extends WeakReference<Object> implements Map.Entry<K, V> {
 * V value;
 * final int hash;
 * WeakHashMap.Entry<K, V> next;
 * <p>
 * <p>
 * //Creates new entry.
 * Entry(Object key, V value, ReferenceQueue<Object> queue, int hash, WeakHashMap.Entry<K, V> next) {
 * //key作为弱引用存到 Reference 类中，会被gc特殊对待在下一次gc时会被清除
 * super(key, queue);
 * this.value = value;
 * this.hash = hash;
 * this.next = next;
 * }
 * <p>
 * //删除失效的 Entry
 * <p>
 * （1）当key失效的时候gc会自动把对应的Entry添加到这个引用队列中;
 * <p>
 * （2）所有对map的操作都会直接或间接地调用到这个方法先移除失效的Entry,比如getTable(),size(),resize();
 * <p>
 * （3）这个方法的目的就是遍历引用队列，并把其中保存的Entry从map中移除掉，具体的过程请看类注释;
 * <p>
 * （4）从这里可以看到移除Entry的同时把value也一并置为null帮助gc清理元素,防御性编程。
 * <p>
 * private void expungeStaleEntries() {
 * //遍历队列，返回可用的引用对象,同时从队列中移除
 * for (Object x; (x = queue.poll()) != null; ) {
 * synchronized (queue) {
 *
 * @SuppressWarnings("unchecked") WeakHashMap.Entry<K, V> e = (WeakHashMap.Entry<K, V>) x;
 * int i = indexFor(e.hash, table.length);
 * //找到所在的桶
 * WeakHashMap.Entry<K, V> prev = table[i];
 * WeakHashMap.Entry<K, V> p = prev;
 * // 遍历链表
 * while (p != null) {
 * WeakHashMap.Entry<K, V> next = p.next;
 * if (p == e) {
 * if (prev == e) {
 * //前移
 * table[i] = next;
 * } else {
 * prev.next = next;
 * }
 * // Must not null out e.next;
 * // stale entries may be in use by a HashIterator
 * e.value = null; // Help GC
 * size--;
 * break;
 * }
 * prev = p;
 * p = next;
 * }
 * }
 * }
 * }
 * }
 **/




