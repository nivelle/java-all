package com.nivelle.base.javacore;

/**
 * 双重检查问题
 *
 * @author fuxinzhong
 * @date 2021/01/29
 */
public class DoubleCheck {

    static DoubleCheck doubleCheck;

    static DoubleCheck getInstance() {
        if (doubleCheck == null) {
            synchronized (DoubleCheck.class) {
                if (doubleCheck == null) {
                    doubleCheck = new DoubleCheck();
                }
            }
        }
        return doubleCheck;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            Thread thread1 = new Thread(() -> {
                DoubleCheck doubleCheck = DoubleCheck.getInstance();
                if (doubleCheck == null) {
                    System.err.println(Thread.currentThread().getName() + "doubleCheck is null");
                } else {
                    System.out.println(Thread.currentThread().getName() + "doubleCheck is not null");
                }
            });
            Thread thread2 = new Thread(() -> {
                DoubleCheck doubleCheck = DoubleCheck.getInstance();
                if (doubleCheck == null) {
                    System.err.println(Thread.currentThread().getName() + "doubleCheck is null");
                } else {
                    System.out.println(Thread.currentThread().getName() + "doubleCheck is not null");
                }
            });

            thread1.start();
            thread2.start();
        }

    }
}
