package com.nivelle.base.jdk.atom;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * LongAdder
 *
 * @author nivell
 * @date 2020/04/12
 */
public class LongAdderDemo {

    /**
     * ![LongAdder](https://s1.ax1x.com/2020/04/28/J42eIJ.jpg)
     *
     *
     * LongAdder是java8中新增的原子类,在多线程环境中,它比AtomicLong性能要高出不少,特别是写多的场景。
     */
    public static void main(String[] args) throws Exception {

        /**
         * public class LongAdder extends Striped64 implements Serializable
         *
         * 1. 继承自Striped64
         */


        /**
         * LongAdder的原理是:
         *
         * 1. 在最初无竞争时，只更新base的值，
         *
         * 2. 当有多线程竞争过时通过分段的思想，让不同的线程更新不同的段;
         *
         * 3. 最后把这些段相加就得到了完整的LongAdder存储的值。
         */

        LongAdder longAdder = new LongAdder();
        /**
         * public void add(long x) {
         *         Cell[] as;//as是Striped64中的cells属性
         *         long b;//b是Striped64中的base属性
         *         long v;//v是当前线程hash到的Cell中存储的值
         *         int m;//m是cells的长度减1，hash时作为掩码使用
         *         Cell a;//a是当前线程hash到的Cell
         *
         *         // 条件1:cells不为空，说明出现过竞争，cells已经创建
         *         // 条件2:cas操作base失败，说明其它线程先一步修改了base，正在出现竞争
         *         if ((as = cells) != null || !casBase(b = base, b + x)) {
         *             // true:表示当前竞争还不激烈
         *             // false:表示竞争激烈，多个线程hash到同一个Cell，可能要扩容
         *             boolean uncontended = true;
         *
         *             if (as == null   // 条件1:cells为空，说明正在出现竞争，上面是从条件2过来的
         *             || (m = as.length - 1) < 0 // 条件2:应该不会出现
         *             ||(a = as[getProbe() & m]) == null   // 条件3:当前线程所在的Cell为空，说明当前线程还没有更新过Cell，应初始化一个Cell
         *             ||!(uncontended = a.cas(v = a.value, v + x))) // 条件4:更新当前线程所在的Cell失败，说明现在竞争很激烈，多个线程hash到了同一个Cell，应扩容
         *              {
         *                 longAccumulate(x, null, uncontended);
         *              }
         *         }
         *     }
         */
        longAdder.add(1);
        /**
         * final void longAccumulate(long x, LongBinaryOperator fn,boolean wasUncontended) {
         *         int h;//存储线程的probe值
         *         if ((h = getProbe()) == 0) {// 如果getProbe()方法返回0，说明随机数未初始化
         *             ThreadLocalRandom.current(); //强制初始化
         *             h = getProbe();// 强制初始化
         *             wasUncontended = true;//都未初始化，肯定还不存在竞争激烈
         *         }
         *         boolean collide = false;//是否发生碰撞
         *         for (;;) {
         *             Cell[] as;
         *             Cell a;
         *             int n;//Cells数组长度
         *             long v;
         *             // cells已经初始化过
         *             if ((as = cells) != null && (n = as.length) > 0) {
         *                 // 当前线程所在的Cell未初始化
         *                 if ((a = as[(n - 1) & h]) == null) {
         *                     if (cellsBusy == 0) {       //当前无其它线程在创建或扩容cells,也没有线程在创建Cell
         *                         Cell r = new Cell(x);   //新建一个Cell，值为当前需要增加的值
         *                         if (cellsBusy == 0 && casCellsBusy()) {//再次检测cellsBusy,并尝试更新它为1;相当于当前线程加锁
         *                             boolean created = false;//是否创建成功
         *                             try {
         *                                 Cell[] rs;
         *                                 int m;//rs=>cells数组长度
         *                                 int j;
         *                                 //重新获取cells，并找到当前线程hash到cells数组中的位置
         *                                 //这里一定要重新获取cells，因为as并不在锁定范围内有可能已经扩容了，这里要重新获取
         *                                 if ((rs = cells) != null &&(m = rs.length) > 0 && rs[j = (m - 1) & h] == null) {
         *                                     rs[j] = r;// 把上面新建的Cell放在cells的j位置处
         *                                     created = true;// 创建成功
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
         *                     wasUncontended = true;      // 这里简单地设为true，相当于简单地自旋一次;通过下面的语句修改线程的probe再重新尝试
         *                 }else if (a.cas(v = a.value, ((fn == null) ? v + x :fn.applyAsLong(v, x)))){
         *                     break; //再次尝试CAS更新当前线程所在Cell的值，如果成功了就返回
         *                 }else if (n >= NCPU || cells != as){
         *                     collide = false;            //如果cells数组的长度达到了CPU核心数，或者cells扩容了;设置collide为false并通过下面的语句修改线程的probe再重新尝试
         *                 }else if (!collide){
         *                     collide = true; //上上个elseif都更新失败了，且上个条件不成立，说明出现冲突了
         *                 }else if (cellsBusy == 0 && casCellsBusy()) {// 明确出现冲突了，尝试占有锁，并扩容
         *                     try {
         *                         if (cells == as) { // 检查是否有其它线程已经扩容过了
         *                             Cell[] rs = new Cell[n << 1];//新数组为原数组的两倍
         *                             for (int i = 0; i < n; ++i){
         *                                 rs[i] = as[i];//把旧数组元素拷贝到新数组中
         *                             }
         *                             cells = rs;// 重新赋值cells为新数组
         *                         }
         *                     } finally {
         *                         cellsBusy = 0;// 释放锁
         *                     }
         *                     collide = false;// 已解决冲突
         *                     continue;// 使用扩容后的新数组重新尝试
         *                 }
         *                 h = advanceProbe(h);// 更新失败或者达到了CPU核心数，重新生成probe，并重试
         *             }
         *             //cells还未初始化;尝试占有锁并初始化cells数组
         *             else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
         *                 boolean init = false;// 是否初始化成功
         *                 try {
         *                     if (cells == as) {// 检测是否有其它线程初始化过
         *                         Cell[] rs = new Cell[2];// 新建一个大小为2的Cell数组
         *                         rs[h & 1] = new Cell(x);// 找到当前线程hash到数组中的位置并创建其对应的Cell
         *                         cells = rs;// 赋值给cells数组
         *                         init = true;// 初始化成功
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
         *
         *  add过程：
         *
         * （1）如果cells数组未初始化，当前线程会尝试占有cellsBusy锁并创建cells数组；
         *
         * （2）如果当前线程尝试创建cells数组时，发现有其它线程已经在创建了，就尝试更新base，如果成功就返回；
         *
         * （3）通过线程的probe值找到当前线程应该更新cells数组中的哪个Cell；
         *
         * （4）如果当前线程所在的Cell未初始化，就占有占有cellsBusy锁并在相应的位置创建一个Cell；
         *
         * （5）尝试CAS更新当前线程所在的Cell，如果成功就返回，如果失败说明出现冲突；
         *
         * （5）当前线程更新Cell失败后并不是立即扩容，而是尝试更新probe值后再重试一次；
         *
         * （6）如果在重试的时候还是更新失败，就扩容；
         *
         * （7）扩容时当前线程占有cellsBusy锁，并把数组容量扩大到两倍，再迁移原cells数组中元素到新数组中；
         *
         * （8）cellsBusy在创建cells数组、创建Cell、扩容cells数组三个地方用到；
         */
        longAdder.add(2);
        System.out.println(longAdder);
        /**
         *
         * 可以看到sum()方法是把base和所有段的值相加得到，如果前面已经累加到sum上的Cell的value有修改，就没法计算到了。所以它是一个最终一致性并不是强一致性。
         *
         * public long sum() {
         *         Cell[] as = cells; Cell a;
         *         long sum = base;// sum初始等于base
         *         if (as != null) {
         *             for (int i = 0; i < as.length; ++i) {// 遍历所有的Cell
         *                 if ((a = as[i]) != null)
         *                     sum += a.value;// 如果所在的Cell不为空，就把它的value累加到sum中
         *             }
         *         }
         *         return sum;
         *     }
         */
        System.out.println("sum()方法是获取LongAdder中真正存储的值的大小，通过把base和所有段相加得到:" + longAdder.sum());
        longAdder.reset();
        System.out.println(longAdder);
        longAdder.add(1);
        longAdder.add(2);
        System.out.println(longAdder.sumThenReset());
        System.out.println(longAdder);
        System.out.println(ThreadLocalRandom.current().nextInt());
        System.out.println("当前系统核心数目:" + Runtime.getRuntime().availableProcessors());
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue(5), new ThreadPoolExecutor.CallerRunsPolicy());

        LongAdder initLongAdder = new LongAdder();
        for (int i = 0; i < 100; i++) {
            executor.execute(new MyTask1(initLongAdder));
        }
        System.out.println("汇总准确值:" + initLongAdder.sum());
        System.out.println("initLongAdder当前值:" + initLongAdder);
        System.out.println("longAdder 保证的是最终一致性");
    }
}

/**
 * 线程安全异常模拟
 */
class MyTask1 implements Runnable {

    LongAdder initLongAdder = null;

    public MyTask1(LongAdder initLongAdder) {
        this.initLongAdder = initLongAdder;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();

        try {
            System.out.println(threadName + " is running:");
            initLongAdder.increment();
            System.out.println(threadName + " value is:" + initLongAdder);
        } catch (Exception e) {
            System.err.println(e.getCause());
        }
        System.out.println(threadName + "当前longAdder:" + initLongAdder);

    }
}
/***
 * 父类主要属性:
 *
 * 1. transient volatile Cell[] cells;//储存各个段的值
 *
 * 2. transient volatile long base;//最初无竞争的时候使用的(特殊段)
 *
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
