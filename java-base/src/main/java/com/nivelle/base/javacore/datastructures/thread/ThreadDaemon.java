package com.nivelle.base.javacore.datastructures.thread;

/**
 * daemon
 *
 * @author nivellefu
 */
public class ThreadDaemon {
    public static void main(String[] args) {
        Thread myThread = new MyThreadHidden();
        for (int i = 0; i < 10; i++) {
            System.out.println("main thread i = " + i);
            if (i == 2) {
                myThread.setDaemon(true);
                myThread.start();
            }
        }
        System.out.println(myThread.isDaemon());
        System.out.println(myThread.isAlive());

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