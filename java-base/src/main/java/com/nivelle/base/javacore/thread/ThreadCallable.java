package com.nivelle.base.javacore.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author nivellefu  Callable 执行demo
 */
public class ThreadCallable {
    public static void main(String[] args) {
        Callable<Integer> myCallable = new MyCallable();
        //使用FutureTask来包装MyCallable对象
        FutureTask<Integer> futureTask = new FutureTask(myCallable);
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
            if (i == 3) {
                //FutureTask对象作为Thread对象的target创建新的线程
                Thread thread = new Thread(futureTask);
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