package com.nivelle.core.jdk.concurrent.atom;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * LongAdder
 *
 * @author nivelle
 * @date 2020/04/12
 */
public class LongAdderMock {

    /**
     * ![LongAdder](https://s1.ax1x.com/2020/04/28/J42eIJ.jpg)
     * <p>
     * <p>
     * LongAdder是java8中新增的原子类,在多线程环境中,它比AtomicLong性能要高出不少,特别是写多的场景。
     */
    public static void main(String[] args) throws Exception {

        /**
         * public class LongAdder extends Striped64 implements Serializable
         *
         * 1. 继承自Striped64（累加器）
         *
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
         *         Cell[] as;//as是Striped64中的cells属性,容量为2的次幂
         *         long b;//b是Striped64中的base属性,基础值，在更新操作时基于CAS无锁技术实现原子更新
         *         long v;//v是当前线程hash到的Cell中存储的值
         *         int m;//m是cells的长度减1，hash时作为掩码使用
         *         Cell a;//a是当前线程hash到的Cell
         *
         *         // 条件1:cells不为空，说明出现过竞争，cells已经创建
         *         // 条件2:cas操作base失败,说明其它线程先一步修改了base，正在出现竞争
         *         if ((as = cells) != null || !casBase(b = base, b + x)) {
         *             // true:表示当前竞争还不激烈
         *             // false:表示竞争激烈,多个线程hash到同一个Cell，可能要扩容
         *             boolean uncontended = true;
         *             // 条件1:cells为空，说明正在出现竞争，上面是从条件2过来的
         *             // 条件2:应该不会出现
         *             // 条件3:当前线程所在的Cell为空，说明当前线程还没有更新过Cell，应初始化一个Cell
         *             // 条件4:更新当前线程所在的Cell失败，说明现在竞争很激烈，多个线程hash到了同一个Cell，应扩容
         *             if (as == null|| (m = as.length - 1) < 0||(a = as[getProbe() & m]) == null||!(uncontended = a.cas(v = a.value, v + x)))
         *              {
         *                 //调用Striped64的方法
         *                 longAccumulate(x, null, uncontended);
         *              }
         *         }
         *     }
         */
        longAdder.add(1);
        /**
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

