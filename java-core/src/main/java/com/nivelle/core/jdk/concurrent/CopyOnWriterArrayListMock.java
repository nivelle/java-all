package com.nivelle.core.jdk.concurrent;


import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CopyOnWriterArrayList 是 ArrayList的线程安全版本，写的时候将共享变量复制一份，这样做的好处就是读操作完全无锁
 *
 * @author nivelle
 * @date 2019/06/16
 */
public class CopyOnWriterArrayListMock {
    /**
     * public class CopyOnWriteArrayList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable
     * <p>
     * (1). CopyOnWriteArrayList实现了List, RandomAccess, Cloneable, java.io.Serializable等接口。
     * <p>
     * (2). CopyOnWriteArrayList实现了List，提供了基础的添加、删除、遍历等操作。
     * <p>
     * (3). CopyOnWriteArrayList实现了RandomAccess，提供了随机访问的能力。
     * <p>
     * (4). CopyOnWriteArrayList实现了Cloneable，可以被克隆。
     * <p>
     * (5). CopyOnWriteArrayList实现了Serializable，可以被序列化。
     */
    public static void main(String[] args) {


        /**
         * 主要属性:
         *
         * 1.  final transient ReentrantLock lock = new ReentrantLock();//用于修改时加锁，使用transient修饰表示不自动序列化。
         *
         * 2.  private transient volatile Object[] array;//真正存储元素的地方，使用transient修饰表示不自动序列化，使用volatile修饰表示一个线程对这个字段的修改另外一个线程立即可见。
         *
         */

        /**
         *  底层是通过 ReentrantLock 来实现线程安全
         *
         *  1. 内部持有一个 ReentrantLock lock = new ReentrantLock();
         *
         *  2. 底层是用volatile transient声明的数组 array
         *
         *  3. 读写分离,写时复制出一个新的数组,完成插入、修改或者移除操作后将新数组赋值给array
         */
        /**
         * 构造函数:
         *
         * public CopyOnWriteArrayList() {
         *      setArray(new Object[0]);// 所有对array的操作都是通过setArray()和getArray()进行
         * }
         */
        CopyOnWriteArrayList copyOnWriteArrayList = new CopyOnWriteArrayList();
        /**
         * public boolean add(E e) {
         *         final ReentrantLock lock = this.lock; //获取到该类的锁对象
         *         lock.lock();//加锁
         *         try {
         *             Object[] elements = getArray();//获取旧数组
         *             int len = elements.length;//旧数组元素个数
         *             Object[] newElements = Arrays.copyOf(elements, len + 1);//将旧数组元素拷贝到新数组中，新数组大小是旧数组大小+1
         *             newElements[len] = e;//将元素放在最后一位
         *             setArray(newElements);//设置CopyOnWriteArrayList
         *             return true;
         *         } finally {
         *             lock.unlock();//释放锁
         *         }
         *     }
         */
        copyOnWriteArrayList.add(1);
        copyOnWriteArrayList.add(2);
        System.out.println("copyOnWriteArrayList:" + copyOnWriteArrayList);
        Object element1 = copyOnWriteArrayList.get(1);
        System.out.println("copyOnWriteArrayList index 1 value is:" + element1);
        Object element2 = copyOnWriteArrayList.get(1);
        System.out.println("copyOnWriteArrayList index 1 value is:" + element2);

        /**
         * 构造函数初始化时,初始化参数是一个CopyOnWriteArrayList:
         *
         * public CopyOnWriteArrayList(Collection<? extends E> c) {
         *         Object[] elements;
         *         if (c.getClass() == CopyOnWriteArrayList.class){
         *            //如果c也是CopyOnWriterArrayList类型,那么直接把它的数组拿过来直接使用
         *             elements = ((CopyOnWriteArrayList<?>)c).getArray();
         *         } else {
         *             elements = c.toArray();
         *             // 这里c.toArray()返回的不一定是Object[]类(数组存放元素可以向上转型,但是存储实际类型不能向下转型【java本身bug】)
         *             if (elements.getClass() != Object[].class){
         *                 elements = Arrays.copyOf(elements, elements.length, Object[].class);
         *             }
         *         }
         *         setArray(elements);
         *     }
         */
        CopyOnWriteArrayList<Integer> copyOnWriteArrayList1 = new CopyOnWriteArrayList(copyOnWriteArrayList);
        System.out.println("直接初始化参数:" + copyOnWriteArrayList1);
        /**
         * 指定索引处添加元素:
         *
         * public void add(int index, E element) {
         *         final ReentrantLock lock = this.lock;
         *         lock.lock();//加锁
         *         try {
         *             Object[] elements = getArray();//获取当前数组
         *             int len = elements.length;//获取当前数组长度也及元素个数
         *             if (index > len || index < 0){
         *                 throw new IndexOutOfBoundsException("Index: "+index+", Size: "+len);
         *             }
         *             Object[] newElements;//新数组
         *             int numMoved = len - index;//需要移动的元素个数
         *             if (numMoved == 0){ //如果插入的位置是最后一位，那么拷贝一个n+1的数组，其前n个元素与旧数组一样
         *                 newElements = Arrays.copyOf(elements, len + 1);
         *             }else {//否则，构造一个len+1大小的数组
         *                 newElements = new Object[len + 1];
         *                 System.arraycopy(elements, 0, newElements, 0, index);//旧数组前index个数组拷贝到新数组
         *                 System.arraycopy(elements, index, newElements, index + 1,numMoved);//旧数组index开始复制到新数组index+1开始的位置
         *             }
         *             newElements[index] = element;//index 位置的元素设置为要添加的元素 element
         *             setArray(newElements);
         *         } finally {
         *             lock.unlock();//释放锁
         *         }
         *     }
         *
         */
        copyOnWriteArrayList1.add(1, 3);

        System.out.println("在index=1位置设置元素:" + copyOnWriteArrayList1);


        /**
         * 如果不存在则添加:
         *
         * public boolean addIfAbsent(E e) {
         *         Object[] snapshot = getArray();
         *         // 如果找到了，大于0返回 fa
         *         return indexOf(e, snapshot, 0, snapshot.length) >= 0 ? false : addIfAbsent(e, snapshot);
         *  }
         *
         * //获取指定元素的index
         * private static int indexOf(Object o, Object[] elements,int index, int fence) {
         *         if (o == null) { //如果要添加的元素为null
         *             for (int i = index; i < fence; i++){//遍历临时copy,找到第一个null返回index
         *                 if (elements[i] == null){
         *                     return i;
         *                  }
         *              }
         *         } else {
         *             for (int i = index; i < fence; i++)
         *                 if (o.equals(elements[i])){ //否则从前往后遍历，获取第一个等于o的元素
         *                     return i;
         *                 }
         *         }
         *         return -1;
         *     }
         *
         * //添加元素
         * private boolean addIfAbsent(E e, Object[] snapshot) {
         *         final ReentrantLock lock = this.lock;
         *         lock.lock();//加锁
         *         try {
         *             Object[] current = getArray();//获取旧数组数据源
         *             int len = current.length; //旧数组长度
         *             if (snapshot != current) { //如果快照与刚获取的数组不一致,说明有修改
         *                 int common = Math.min(snapshot.length, len);
         *                 for (int i = 0; i < common; i++){
         *                     if (current[i] != snapshot[i] && eq(e, current[i])){ // 重新检查元素是否在刚获取的数组里;到这个方法里面了,说明元素不在快照里面
         *                         return false;
         *                     }
         *                 }
         *                 if (indexOf(e, current, common, len) >= 0){ //元素存在，返回false
         *                         return false;
         *                 }
         *             }
         *             Object[] newElements = Arrays.copyOf(current, len + 1);//拷贝一份n+1的数组
         *             newElements[len] = e;//将元素放在最后一位
         *             setArray(newElements);
         *             return true;
         *         } finally {
         *             lock.unlock();//释放锁
         *         }
         *     }
         */
        boolean addResult = copyOnWriteArrayList1.addIfAbsent(3);
        System.out.println("不存在则添加:" + addResult);
        /**
         * public boolean remove(Object o) {
         *         Object[] snapshot = getArray();
         *         int index = indexOf(o, snapshot, 0, snapshot.length);//找到要删除元素的位置
         *         return (index < 0) ? false : remove(o, snapshot, index);
         * }
         *
         * public E remove(int index) {
         *         final ReentrantLock lock = this.lock;
         *         lock.lock();//加锁
         *         try {
         *             Object[] elements = getArray();//获取底层数组
         *             int len = elements.length;//获得数组元素个数
         *             E oldValue = get(elements, index);//获取elements数组里面index位置的元素
         *             int numMoved = len - index - 1;//需要移动的元素个数
         *             if (numMoved == 0){
         *                 setArray(Arrays.copyOf(elements, len - 1));//如果是最后一个元素,直接拷贝一个n-1长度的数组。
         *             }else {
         *                 Object[] newElements = new Object[len - 1];//构建一个len-1长度的数组
         *                 System.arraycopy(elements, 0, newElements, 0, index);//将旧数组index前元素拷贝到新数组
         *                 System.arraycopy(elements, index + 1, newElements, index,numMoved);//将旧数组index+1开始往后的元素拷贝到新数组
         *                 setArray(newElements);//底层数组设置为新数组
         *             }
         *             return oldValue;//返回旧值
         *         } finally {
         *             lock.unlock();//释放锁
         *         }
         *     }
         */
        try {
            int removeValue = copyOnWriteArrayList1.remove(3);
            System.out.println(removeValue);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e);
        }


        System.out.println("直接获取数组的长度:" + copyOnWriteArrayList1.size());

        /**
         * （1）CopyOnWriteArrayList使用ReentrantLock重入锁加锁，保证线程安全；
         *
         * （2）CopyOnWriteArrayList的写操作都要先拷贝一份新数组，在新数组中做修改，修改完了再用新数组替换老数组，所以空间复杂度是O(n)，性能比较低下；
         *
         * （3）CopyOnWriteArrayList的读操作支持随机访问，时间复杂度为O(1)；
         *
         * （4）CopyOnWriteArrayList采用读写分离的思想，读操作不加锁，写操作加锁，且写操作占用较大内存空间，所以适用于读多写少的场合；
         *
         * （5）CopyOnWriteArrayList只保证最终一致性，不保证实时一致性；
         *
         *  (6) 读操作基于原来的Array,写操作基于新的Array进行
         *
         *  (8) CopyOnWriteArrayList 迭代器是只读的，不支持增删改。因为迭代器遍历的仅仅是一个快照，而对快照进行增删改是没有意义的
         */
    }
}
