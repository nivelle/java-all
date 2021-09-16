package com.nivelle.core.javacore.lang;

/**
 * Thread类
 *
 * @author fuxinzhong
 * @date 2020/05/08
 */
public class ThreadMethodMock {

    public static void main(String[] args) throws Exception {
        System.out.println("守护线程 demo 开始:");
        Thread myThread = new MyDaemonThread();
        for (int i = 0; i < 2; i++) {
            if (i == 1) {
                myThread.setDaemon(true);
                myThread.setName("守护线程");
                myThread.start();//新线程执行MyDaemonThread的逻辑
            }
        }
        System.out.println("main is daemon:" + Thread.currentThread().isDaemon());
        System.out.println("myThread is daemon:" + myThread.isDaemon());
        System.out.println("myThread is alive:" + myThread.isAlive());
        /**
         * public final synchronized void join(long millis)
         *     throws InterruptedException {
         *         long base = System.currentTimeMillis();
         *         long now = 0;
         *         ## 超时时间不能为负值
         *         if (millis < 0) {
         *             throw new IllegalArgumentException("timeout value is negative");
         *         }
         *
         *         if (millis == 0) {
         *             ## native方法判断调用方线程是否还活着,如果活着别的线程就得一直等着
         *             while (isAlive()) {
         *                 ## main线程调用Thread对象的wait方法,让出CPU
         *                 wait(0);
         *             }
         *         } else {
         *             while (isAlive()) {
         *                 long delay = millis - now;
         *                 if (delay <= 0) {
         *                     break;
         *                 }
         *                 wait(delay);
         *                 now = System.currentTimeMillis() - base;
         *             }
         *         }
         *     }
         */
        // main线程调用 myThread的join方法，join并不是myThread的逻辑
        System.out.println("当前线程:" + Thread.currentThread().getName() + "调用myThread的join方:");
        myThread.join();
        System.out.println("守护线程 demo 结束");
        System.out.println("thread yield demo 方法开始");
        /**
         * 使当前线程从执行状态（运行状态）变为可执行态（就绪状态）。
         * cpu会从众多的可执行态里选择，也就是说，当前也就是刚刚的那个线程还是有可能会被再次执行到的，并不是说一定会执行其他线程而该线程在下一次中不会执行到了。
         */
        Thread.yield();
        System.out.println(Thread.currentThread().getName() + "让出CPU执行权限");
        System.out.println("thread yield demo 方法结束");

        System.out.println("线程锁 中断 demo 开始");
        System.out.println("线程锁 中断 demo 结束");

        System.out.println("thread join方法 demo 开始");
        MyJoinRunnable myJoinRunnable = new MyJoinRunnable();
        Thread thread = new Thread(myJoinRunnable);
        for (int i = 1; i <= 2; i++) {
            System.out.println(Thread.currentThread().getName() + "第" + i + "次执行");
            if (i == 1) {
                thread.setName("join线程");
                thread.start();
                try {
                    //main线程需要等待thread线程执行完后才能继续执行
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("thread join方法 demo结束");

        System.out.println("thread 优先级 demo 开始");
        new MyPriorityThread("低级", 1).start();
        new MyPriorityThread("中级", 5).start();
        new MyPriorityThread("高级", 10).start();
        System.out.println("thread yield demo");

        Thread myThread1 = new MyThread1();
        Thread myThread2 = new MyThread2();
        myThread1.setPriority(Thread.MAX_PRIORITY);
        myThread2.setPriority(Thread.MIN_PRIORITY);
        for (int i = 0; i < 2; i++) {
            System.out.println("main thread i = " + i);
            if (i == 1) {
                /**
                 * 当前main线程让出线程
                 */
                myThread1.start();
                myThread2.start();
            }
        }
        System.out.println("thread 优先级 demo 结束");

        System.out.println("thread sleep demo 开始");
        MySleepRunnable mySleepRunnable = new MySleepRunnable();
        Thread threadSleep = new Thread(mySleepRunnable);
        for (int i = 0; i < 2; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
            if (i == 1) {
                threadSleep.start();
                try {
                    // 使得thread必然能够马上得以执行14
                    threadSleep.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("thread sleep demo 结束");
        System.out.println("thread interrupt demo 开始");
        /**
         * Thread提供了interrupt方法，中断线程的执行:
         *
         * 1. 如果线程堵塞在object.wait、Thread.join和Thread.sleep，将会抛出InterruptedException,同时清除线程的中断状态;
         *
         * 2. 如果线程堵塞在java.nio.channels.InterruptibleChannel的IO上，Channel将会被关闭，线程被置为中断状态，并抛出java.nio.channels.ClosedByInterruptException；
         *
         * 3. 如果线程堵塞在java.nio.channels.Selector上，线程被置为中断状态，select方法会马上返回，类似调用wakeup的效果；
         */


    }
}

class MyDaemonThread extends Thread {

    @Override
    public void run() {
        for (int i = 1; i <= 2; i++) {
            System.out.println("MyDaemonThread 第 " + i + "次执行");
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class MyJoinRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + "插队开始执行");
        try {
            Thread.sleep(3000);
            System.out.println(Thread.currentThread().getName() + "Waits for this thread to die,当前线程不死就不让出CPU");
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        System.out.println(Thread.currentThread().getName() + "插队执行完毕");
    }
}

class MyPriorityThread extends Thread {
    public MyPriorityThread(String name, int pro) {
        super(name);
        this.setPriority(pro);
    }

    @Override
    public void run() {
        for (int i = 0; i < 2; i++) {
            System.out.println(this.getName() + "线程第" + i + "次执行!");
            if (i % 5 == 0) {
                Thread.yield();
            }
        }
    }
}

class MyThread1 extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 2; i++) {
            System.out.println("myThread 1 --  i = " + i);
        }
    }
}

class MyThread2 extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 2; i++) {
            System.out.println("myThread 2 --  i = " + i);
        }
    }
}

class MySleepRunnable implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < 2; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
        }
    }
}