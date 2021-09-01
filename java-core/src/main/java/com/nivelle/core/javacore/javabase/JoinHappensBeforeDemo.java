package com.nivelle.base.javacore;

/**
 * Thread.join() happens-before
 *
 * @author fuxinzhong
 * @date 2021/01/29
 */
public class JoinHappensBeforeDemo {
    static long var = 12;

    public static void main(String[] args) throws InterruptedException {
        Thread b = new Thread(() -> {
            var = 19;
        });
        System.out.println("before join b:" + Thread.currentThread().getName() + "当前值：" + var);
        b.start();
        b.join();
        System.out.println("after join b:" + Thread.currentThread().getName() + "当前值：" + var);
    }

}
