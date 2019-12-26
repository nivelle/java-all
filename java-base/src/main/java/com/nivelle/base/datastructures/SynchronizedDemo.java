package com.nivelle.base.datastructures;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Synchronized 方法
 *
 * @author fuxinzhong
 * @date 2019/11/02
 */
public class SynchronizedDemo {


    public static void main(String[] args) {
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue(5), new ThreadPoolExecutor.CallerRunsPolicy());

        Long count = new Long(1);

        for (int i = 0; i < 50; i++) {
            executor.execute(new SynchronizedTask(count));
        }
    }
}


/**
 * 1. synchronized
 */
class SynchronizedTask implements Runnable {

    Long count;

    public SynchronizedTask(Long count) {
        this.count = count;
    }

    @Override
    public synchronized void run() {
        Long result = count++;
        System.out.println("threadName:" + Thread.currentThread().getName() + ";current int:" + result);
        return;
    }
}
