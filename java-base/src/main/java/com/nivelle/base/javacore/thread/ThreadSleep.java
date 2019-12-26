package com.nivelle.base.javacore.thread;


/**
 * sleep
 */
public class ThreadSleep {
    public static void main(String[] args) {
        MySleepRunnable myRunnable = new MySleepRunnable();
        Thread thread = new Thread(myRunnable);
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
            if (i == 3) {
                thread.start();
                try {
                    Thread.sleep(1);   // 使得thread必然能够马上得以执行14
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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