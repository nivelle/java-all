package com.nivelle.base.jdk.concurrent;

import com.nivelle.base.pojo.MyPhaser;
import com.nivelle.base.pojo.StudentTask;

/**
 * Phaser 移相位
 *
 * @author nivelle
 * @date 2020/04/14
 */
public class PhaserMock {

    /**
     * Phaser这个类的使用场景为N个线程分阶段并行的问题。
     * <p>
     * 有这么一个任务为“做3道题“，每个学生一个进程，5个学生可以并行做，这个就是常规的并发，但是如果加一个额外的 限制条件，
     * 必须等所有人都做完类第一题，才能开始做第二题，必须等所有人都做完了第二题，才能做第三题
     * ，这个问题就转变成了分阶段并发的问题，最适合用Phaser来解题
     */
    public static void main(String[] args) {
        MyPhaser phaser = new MyPhaser();
        StudentTask[] studentTask = new StudentTask[5];
        for (int i = 0; i < studentTask.length; i++) {
            studentTask[i] = new StudentTask(phaser);
            phaser.register();  //注册一次表示phaser维护的线程个数
        }
        Thread[] threads = new Thread[studentTask.length];
        for (int i = 0; i < studentTask.length; i++) {
            threads[i] = new Thread(studentTask[i], "Student " + i);
            threads[i].start();
        }

        for (int i = 0; i < studentTask.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Phaser has finished:" + phaser.isTerminated());
    }
}