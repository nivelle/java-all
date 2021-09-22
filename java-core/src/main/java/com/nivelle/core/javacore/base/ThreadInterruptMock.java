package com.nivelle.core.javacore.base;

/**
 * 线程中断测试
 *
 * @author fuxinzhong
 * @date 2021/01/30
 */
public class ThreadInterruptMock {

    public static void main(String[] args) {
        System.out.println("当前线程名称：" + Thread.currentThread().getName());
        Thread mainThread = Thread.currentThread();
        System.out.println("1-》主线程状态：" + mainThread.getState());
        Thread thread = new Thread(() -> {
            System.out.println("子线程：" + Thread.currentThread().getName());
            System.out.println("主线程状态,中断前的标识: 2-》" + mainThread.getState());
            System.out.println("主线程状态,中断前的标识:" + mainThread.isInterrupted());
            mainThread.interrupt();
            System.out.println("主线程状态,中断后的标识 3-》" + mainThread.getState());
            System.out.println("中断后的标识：" + mainThread.isInterrupted());
        });
        thread.start();
        try {
            Thread.sleep(10000);
            System.out.println("当前主现场的状态:" + mainThread.getState());
        } catch (InterruptedException e) {
            System.out.println("主线程被中断了，" + e);
            System.out.println("当前线程的中断标识：" + mainThread.isInterrupted());
            System.out.println("5" + mainThread.getState());
        }
        System.out.println("6" + mainThread.getState());
    }
}
