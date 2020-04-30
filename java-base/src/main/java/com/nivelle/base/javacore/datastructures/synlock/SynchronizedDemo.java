package com.nivelle.base.javacore.datastructures.synlock;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Synchronized 方法
 *
 * @author nivell
 * @date 2019/11/02
 */
public class SynchronizedDemo {
    public static ThreadPoolExecutor threadPoolExecutor;

    static {
        threadPoolExecutor = new ThreadPoolExecutor(10, 10, 200, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    public static void main(String[] args) {
        SynchronizedDemo synchronizedDemo = new SynchronizedDemo();
        if (synchronizedDemo == null) {
            System.out.println(synchronizedDemo + "is null");
        } else {
            System.out.println("main 方法里面corePoolSize:" + synchronizedDemo.threadPoolExecutor.getCorePoolSize());
        }
        int count = 1;
        for (int i = 0; i < 20; i++) {
            synchronizedDemo.threadPoolExecutor.execute(new SynchronizedTask(count));
        }
    }
}


/**
 * 1. synchronized
 */
class SynchronizedTask implements Runnable {

    int count;

    public SynchronizedTask(int count) {
        this.count = count;
    }

    @Override
    public synchronized void run() {
        int result = count++;
        System.out.println("threadName:" + Thread.currentThread().getName() + ";current int:" + result);
        return;
    }
}
