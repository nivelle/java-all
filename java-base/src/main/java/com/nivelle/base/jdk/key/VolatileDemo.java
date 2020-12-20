package com.nivelle.base.jdk.key;

import java.util.concurrent.Executors;

/**
 * volatile
 *
 * @author fuxinzhong
 * @date 2020/12/20
 */
public class VolatileDemo implements Runnable {

    private int a = 0;
    private volatile boolean flag = false;

    public void writer() {
        a = 1;
        flag = true;
    }

    public void reader() {
        if (flag) {
            int i = a;
        }
    }

    @Override
    public void run() {
        writer();
        reader();
        return;
    }

    public static void main(String[] args) {
        Executors.newFixedThreadPool(3).submit(new VolatileDemo());
    }
}
