package com.nivelle.core.javacore.concurrent.atom;

/**
 * 并发 striped64
 *
 * @author fuxinzhong
 * @date 2020/11/03
 */
public class Striped64Mock {
    //abstract class Striped64 extends Number protected 级别,只能同同包或者子包实现
    /**
     * Table of cells. When non-null, size is a power of 2.
     */
    //transient volatile Striped64.Cell[] cells;

    /**
     * Base value, used mainly when there is no contention, but also as
     * a fallback during table initialization races. Updated via CAS.
     * //基础值，在更新操作时基于CAS无锁技术实现原子更新
     */
    //transient volatile long base;

    /**
     * Spinlock (locked via CAS) used when resizing and/or creating Cells.
     * //自旋锁  用于保护创建或者扩展Cell表。
     */
    //transient volatile int cellsBusy;


    /***
     * 父类主要属性:
     *
     * 1. transient volatile Cell[] cells;//储存各个段的值
     *     //1. 在开始没有竞争的情况下，将累加值累加到base
     *     //2. 在cells初始化的过程中，cells不可用，这时会尝试将值累加到base上
     * 2. transient volatile long base;//最初无竞争的时候使用的(特殊段)
     *     //cellsBusy，它有两个值0 或1，它的作用是当要修改cells数组时加锁，防止多线程同时修改cells数组，0为无锁，1为加锁，加锁的状况有三种
     *     //1. cells数组初始化的时候；
     *     //2. cells数组扩容的时候；
     *     //3. 如果cells数组中某个元素为null，给这个位置创建新的Cell对象的时候；
     * 3. transient volatile int cellsBusy;//标记当前是否有线程正在创建或者扩容cells,或者正在创建Cell,通过CAS更新该值,相当于一个锁
     *
     *
     * ## Striped64中的内部类，使用@sun.misc.Contended注解，说明里面的值消除伪共享原理是在使用此注解的对象或字段的前后各增加128字节大小的padding，使用2倍于大多数硬件缓存行的大小来避免相邻扇区预取导致的伪共享冲突
     * @sun.misc.Contended
     * static final class Cell {
     *         volatile long value;// 存储元素的值，使用volatile修饰保证可见性
     *         Cell(long x) { value = x; }
     *         final boolean cas(long cmp, long val) {// CAS更新value的值
     *             return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
     *         }
     *
     *         // Unsafe mechanics
     *         private static final sun.misc.Unsafe UNSAFE;// Unsafe实例
     *         private static final long valueOffset;// value字段的偏移量
     *         static {
     *             try {
     *                 UNSAFE = sun.misc.Unsafe.getUnsafe();
     *                 Class<?> ak = Cell.class;
     *                 valueOffset = UNSAFE.objectFieldOffset
     *                     (ak.getDeclaredField("value"));
     *             } catch (Exception e) {
     *                 throw new Error(e);
     *             }
     *         }
     *     }
     **/

/**
 @sun.misc.Contended static final class Cell { //被Contended修饰，目的是为了防止变量的伪共享
 volatile long value; //保存要累加的值
 Cell(long x) { value = x; }
 final boolean cas(long cmp, long val) {//使用Unsafe类的cas来更新value的值
 return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
 }

 // Unsafe mechanics
 private static final sun.misc.Unsafe UNSAFE;
 private static final long valueOffset;
 static {
 try {
 UNSAFE = sun.misc.Unsafe.getUnsafe();
 Class<?> ak = Striped64.Cell.class;
 //获取value值在Cell对象中的偏移量，以便迅速定位
 valueOffset = UNSAFE.objectFieldOffset(ak.getDeclaredField("value"));
 } catch (Exception e) {
 throw new Error(e);
 }
 }
 }
 **/

/**
 * final void longAccumulate(long x, LongBinaryOperator fn,boolean wasUncontended) {
 *         //存储线程的probe[探针]值
 *         int h;
 *         //如果getProbe()方法返回0，说明随机数未初始化
 *         if ((h = getProbe()) == 0) {
 *             ThreadLocalRandom.current(); //强制初始化
 *             h = getProbe();// 强制初始化
 *             wasUncontended = true;//都未初始化，肯定还不存在竞争激烈
 *         }
 *         boolean collide = false;//是否发生碰撞
 *         for (;;) {
 *             Cell[] as;
 *             Cell a;
 *             //Cells数组长度
 *             int n;
 *             long v;
 *
 *             // cells已经初始化过
 *             if ((as = cells) != null && (n = as.length) > 0) {
 *                 // 当前线程所在的Cell未初始化
 *                 if ((a = as[(n - 1) & h]) == null) {
 *                     //当前无其它线程在创建或扩容cells,也没有线程在创建Cell
 *                     if (cellsBusy == 0) {
 *                         //新建一个Cell，值为当前需要增加的值
 *                         Cell r = new Cell(x);
 *                         ////再次检测cellsBusy,并尝试更新它为1;相当于当前线程加锁
 *                         if (cellsBusy == 0 && casCellsBusy()) {
 *                             boolean created = false;//是否创建成功
 *                             try {
 *                                 Cell[] rs;
 *                                 int m;//rs=>cells数组长度
 *                                 int j;
 *                                 //重新获取cells，并找到当前线程hash到cells数组中的位置
 *                                 //这里一定要重新获取cells，因为as并不在锁定范围内有可能已经扩容了，这里要重新获取
 *                                 if ((rs = cells) != null &&(m = rs.length) > 0 && rs[j = (m - 1) & h] == null) {
 *                                    // 把上面新建的Cell放在cells的j位置处
 *                                     rs[j] = r;
 *                                     // 创建成功
 *                                     created = true;
 *                                 }
 *                             } finally {
 *                                 cellsBusy = 0;// 相当于释放锁
 *                             }
 *                             if (created){//创建成功则跳出循环
 *                                 break;
 *                               }
 *                             continue;           // Slot is now non-empty
 *                         }
 *                     }
 *                     // 标记当前未出现冲突
 *                     collide = false;
 *                 }
 *                 //当前线程所在的Cell不为空，且更新失败了
 *                 else if (!wasUncontended){       // CAS already known to fail
 *                     //这里简单地设为true,相当于简单地自旋一次;通过下面的语句修改线程的 probe 再重新尝试
 *                     wasUncontended = true;
 *                 }else if (a.cas(v = a.value, ((fn == null) ? v + x :fn.applyAsLong(v, x)))){
 *                     //再次尝试CAS更新当前线程所在Cell的值，如果成功了就返回
 *                     break;
 *                 }else if (n >= NCPU || cells != as){
 *                     //如果cells数组的长度达到了CPU核心数，或者cells扩容了;设置collide为false并通过下面的语句修改线程的 probe 再重新尝试
 *                     collide = false;
 *                 }else if (!collide){
 *                     collide = true; //上上个elseif都更新失败了，且上个条件不成立，说明出现冲突了
 *                 }else if (cellsBusy == 0 && casCellsBusy()) {// 明确出现冲突了，尝试占有锁，并扩容;casCellsBusy set cellsBusy=1
 *                     try {
 *                         // 检查是否有其它线程已经扩容过了
 *                         if (cells == as) {
 *                             //新数组为原数组的两倍
 *                             Cell[] rs = new Cell[n << 1];
 *                             for (int i = 0; i < n; ++i){
 *                                 //把旧数组元素拷贝到新数组中
 *                                 rs[i] = as[i];
 *                             }
 *                             cells = rs;// 重新赋值cells为新数组
 *                         }
 *                     } finally {
 *                         cellsBusy = 0;// 释放锁
 *                     }
 *                     collide = false;// 已解决冲突
 *                     continue;// 使用扩容后的新数组重新尝试
 *                 }
 *                 // 更新失败或者达到了CPU核心数，重新生成 probe，并重试
 *                 h = advanceProbe(h);
 *             }
 *             //cells还未初始化;尝试占有锁并初始化cells数组
 *             else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
 *                 boolean init = false;// 是否初始化成功
 *                 try {
 *                     if (cells == as) {// 检测是否有其它线程初始化过
 *                         // 新建一个大小为2的Cell数组
 *                         Cell[] rs = new Cell[2];
 *                         // 找到当前线程hash到数组中的位置并创建其对应的Cell
 *                         rs[h & 1] = new Cell(x);
 *                         // 赋值给cells数组
 *                         cells = rs;
 *                         // 初始化成功
 *                         init = true;
 *                     }
 *                 } finally {
 *                     cellsBusy = 0;// 释放锁
 *                 }
 *                 if (init){//初始化成功直接返回
 *                     break;//因为增加的值已经同时创建到Cell中了
 *                 }
 *             }
 *             // 如果有其它线程在初始化cells数组中,就尝试更新base;如果成功了就返回
 *             else if (casBase(v = base, ((fn == null) ? v + x :fn.applyAsLong(v, x)))){
 *                 break;
 *             }
 *         }
 *     }
 * */

}
