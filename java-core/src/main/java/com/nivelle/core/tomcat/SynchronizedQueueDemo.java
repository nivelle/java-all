package com.nivelle.core.tomcat;

import org.apache.tomcat.util.collections.SynchronizedQueue;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 同步队列
 *
 * @author nivelle
 * @date 2020/04/14
 */
public class SynchronizedQueueDemo {

    public static void main(String[] args) {
        /**
         * 底层是数组,主要靠synchronized来实现同步
         */
        SynchronizedQueue<Integer> synchronizedQueue = new SynchronizedQueue();
        synchronizedQueue.offer(1);
        synchronizedQueue.offer(2);
        System.out.println("synchronizedQueue values is:" + synchronizedQueue.size());
        Integer value = synchronizedQueue.poll();
        System.out.println(value);

        //synchronizedQueue.clear();
        System.out.println("poll 方法" + synchronizedQueue.poll());
        System.out.println("=========================================");
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue(5), new ThreadPoolExecutor.CallerRunsPolicy());
        for (int i = 0; i < 129; i++) {
            executor.execute(new MyTask(synchronizedQueue));
        }
        System.out.println("queue size:" + synchronizedQueue.size());
        if (synchronizedQueue.size() > 0) {
            int i = 0;
            while (true) {
                i++;
                System.out.println("i:" + i + " " + "value:" + synchronizedQueue.poll());
                if (i > 20) {
                    break;
                }
            }
        }
    }
}

class MyTask implements Runnable {
    SynchronizedQueue synchronizedQueue;

    public MyTask(SynchronizedQueue synchronizedQueue) {
        this.synchronizedQueue = synchronizedQueue;
    }

    @Override
    public void run() {
        try {
            Random random = new Random();
            int rand = random.nextInt(100);
            synchronizedQueue.offer(rand);
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        }
    }
}
