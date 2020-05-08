package com.nivelle.base.javacore.datastructures.thread;

/**
 * Thread类
 *
 * @author fuxinzhong
 * @date 2020/05/08
 */
public class ThreadMethodDemo {

    public static void main(String[] args) {
        System.out.println("守护线程 demo 开始");

        Thread myThread = new MyThreadHidden();
        for (int i = 0; i < 10; i++) {
            System.out.println("main thread i = " + i);
            if (i == 2) {
                myThread.setDaemon(true);
                myThread.setName("守护线程");
                myThread.start();
            }
        }
        System.out.println(myThread.isDaemon());
        System.out.println(myThread.isAlive());
        System.out.println("守护线程 demo 结束");

        System.out.println("线程锁 中断 demo 开始");

        System.out.println("thread join方法 demo");
        MyRunnable myRunnable = new MyRunnable();
        Thread thread = new Thread(myRunnable);
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
            if (i == 3) {
                thread.start();
                try {
                    //main线程需要等待thread线程执行完后才能继续执行
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("线程锁 中断 demo 借宿");

        System.out.println("thread 优先级 demo 开始");
        new MyThread("低级", 1).start();
        new MyThread("中级", 5).start();
        new MyThread("高级", 10).start();
        System.out.println("thread yield demo");

        Thread myThread1 = new MyThread1();
        Thread myThread2 = new MyThread2();
        myThread1.setPriority(Thread.MAX_PRIORITY);
        myThread2.setPriority(Thread.MIN_PRIORITY);
        for (int i = 0; i < 10; i++) {
            System.out.println("main thread i = " + i);
            if (i == 2) {
                /**
                 * 当前main线程让出线程
                 */
                myThread1.start();
                myThread2.start();
                Thread.yield();

            }
        }
        System.out.println("thread 优先级 demo 结束");

        System.out.println("thread sleep demo 开始");
        MySleepRunnable mySleepRunnable = new MySleepRunnable();
        Thread threadSleep = new Thread(mySleepRunnable);
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
            if (i == 3) {
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

    }
}

class MyThreadHidden extends Thread {

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("i = " + i);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class MyRunnable implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
        }
    }
}

class  MyThread extends Thread{
    public MyThread(String name,int pro){
        super(name);
        this.setPriority(pro);
    }

    @Override
    public void run(){
        for (int i=0;i<30;i++){
            System.out.println(this.getName()+"线程第"+i+"次执行!");
            if(i%5==0){
                Thread.yield();
            }
        }
    }
}

class MyThread1 extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("myThread 1 --  i = " + i);
        }
    }
}

class MyThread2 extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("myThread 2 --  i = " + i);
        }
    }
}

class MySleepRunnable implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
        }
    }
}