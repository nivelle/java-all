package com.nivelle.core.javabase;

/**
 * 双重检查问题
 *
 * @author fuxinzhong
 * @date 2021/01/29
 */
public class DoubleCheckMock {

    static DoubleCheckMock doubleCheckMock;

    static DoubleCheckMock getInstance() {
        if (doubleCheckMock == null) {
            synchronized (DoubleCheckMock.class) {
                if (doubleCheckMock == null) {
                    doubleCheckMock = new DoubleCheckMock();
                }
            }
        }
        return doubleCheckMock;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            Thread thread1 = new Thread(() -> {
                DoubleCheckMock doubleCheckMock = DoubleCheckMock.getInstance();
                if (doubleCheckMock == null) {
                    System.err.println(Thread.currentThread().getName() + "doubleCheck is null");
                } else {
                    System.out.println(Thread.currentThread().getName() + "doubleCheck is not null");
                }
            });
            Thread thread2 = new Thread(() -> {
                DoubleCheckMock doubleCheckMock = DoubleCheckMock.getInstance();
                if (doubleCheckMock == null) {
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
