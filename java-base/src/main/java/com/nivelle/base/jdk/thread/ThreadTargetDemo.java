package com.nivelle.base.jdk.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ThreadTargetDemo {
    public static void main(String[] args) {
        Callable<Integer> myCallable = new MyCallable();
        //使用FutureTask来包装MyCallable对象
        FutureTask<Integer> futureTask = new FutureTask(myCallable);
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
            if (i == 3) {
                //FutureTask对象作为Thread对象的target创建新的线程
                Thread thread = new Thread(futureTask);
                thread.setName("myCallable thread");
                thread.start();
            }
        }
        System.out.println("主线程for循环执行完毕..");
        try {
            //取得新创建的新线程中的call()方法返回的结果
            int sum = futureTask.get();
            System.err.println("sum = " + sum);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
            if (i == 3) {
                MyRunnableDemo myRunnable = new MyRunnableDemo();
                Thread thread1 = new Thread(myRunnable);
                thread1.setName("myRunnable thread1");
                // 将myRunnable作为Thread target创建新的线程
                Thread thread2 = new Thread(myRunnable);
                thread2.setName("myRunnable thread2");

                thread1.start();
                thread2.start();
            }
        }
    }
}

class MyCallable implements Callable<Integer> {
    private int i = 0;

    /**
     * 与run()方法不同的是，call()方法具有返回值
     */
    @Override
    public Integer call() {
        int sum = 0;
        for (; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
            sum += i;
        }
        return sum;
    }
}

class MyRunnableDemo implements Runnable {
    private int i = 0;

    @Override
    public void run() {
        for (i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
        }
    }
}