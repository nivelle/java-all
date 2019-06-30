package com.nivelle.guide.javacore.thread;

import java.util.Random;

public class MyThreadLocal {

    public static ThreadLocal<String> local = new ThreadLocal();
    public static ThreadLocal<String> local2 = new ThreadLocal();

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            TestThread testThread = new TestThread();
            new Thread(testThread).start();
        }
    }

}

class TestThread implements Runnable {

    @Override
    public void run() {
        try {
            Thread.sleep(1l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int random = new Random().nextInt()+1;
        //System.out.println(random);
        MyThreadLocal.local.set(Thread.currentThread().getId() + ":" + random);
        //MyThreadLocal.local2.set(Thread.currentThread().getId() + ":" + 2);
        firstStep();
        //first2Step();
        try {
            Thread.sleep(1l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // secondS2tep();
        secondStep();
        try {
            Thread.sleep(1l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void firstStep() {
        System.out.println(MyThreadLocal.local.get() + ":first step");
    }

    public void secondStep() {
        System.out.println(MyThreadLocal.local.get() + ":second step");
    }

    public void first2Step() {
        System.out.println(MyThreadLocal.local2.get() + ":first step");
    }

    public void secondS2tep() {
        System.out.println(MyThreadLocal.local2.get() + ":second step");
    }
}