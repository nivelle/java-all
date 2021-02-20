package com.nivelle.base.jvm;

/**
 * 避免伪共享
 *
 * @author fuxinzhong
 * @date 2020/11/03
 */
public class FalseSharing implements Runnable {

    public final static int NUM_THREADS = 4;

    public final static long ITERATIONS = 500L * 1000L * 1000L;

    private final int arrayIndex;

    private static VolatileLong3[] longs = new VolatileLong3[NUM_THREADS];

    static {
        for (int i = 0; i < longs.length; i++) {
            longs[i] = new VolatileLong3();
        }
    }

    public FalseSharing(final int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    /**
     * 缓存系统中是以缓存行（cache line）为单位存储的。缓存行是2的整数幂个连续字节，一般为32-256个字节。最常见的缓存行大小是64个字节。
     * 当多线程修改互相独立的变量时，如果这些变量共享同一个缓存行，就会无意中影响彼此的性能，这就是伪共享。
     */
    public static void main(final String[] args) throws Exception {
        long start = System.nanoTime();
        runTest();
        System.out.println("duration = " + (System.nanoTime() - start));
    }

    private static void runTest() throws InterruptedException {
        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new FalseSharing(i));
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    @Override
    public void run() {
        long i = ITERATIONS + 1;
        while (0 != --i) {
            longs[arrayIndex].value = i;
        }
    }

    public final static class VolatileLong { //duration = 25935220258
        public volatile long value = 0L;
    }

    public final static class VolatileLong2 {//duration = 8469905713
        //可以在属性的前后进行padding
        volatile long p0, p1, p2, p3, p4, p5, p6;
        public volatile long value = 0L;
        volatile long q0, q1, q2, q3, q4, q5, q6;
    }

    /**
     * Unlock: -XX:-RestrictContended
     * <p>
     * //@sun.misc.Contended注解会在变量前后各加128字节
     */
    @sun.misc.Contended
    public final static class VolatileLong3 {//duration(sun.misc.Contended) = 6971730870 //duration(null) = 22158259623 约等于无补齐
        public volatile long value = 0L;
    }
}
