package com.nivelle.core.javacore.patterns.balking;

/**
 * Balking是“退缩不前”的意思。Balking Pattern和Guarded Suspension Pattern 一样需要警戒条件。
 * 在Balking Pattern中，当警戒条件不成立时，会马上中断，而Guarded Suspension Pattern 则是等待到可以执行时再去执行。
 *
 * @author fuxinzhong
 * @date 2021/02/01
 */
public class Main {
    public static void main(String[] args) {
        Data data = new Data("data.txt", "(empty)");
        new ChangerThread("ChangerThread", data).start();
        new SaverThread("SaverThread", data).start();
    }
}
