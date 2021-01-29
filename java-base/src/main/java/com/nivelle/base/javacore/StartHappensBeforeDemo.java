package com.nivelle.base.javacore;

/**
 * Thread.start() happens-before
 *
 * @author fuxinzhong
 * @date 2021/01/29
 */
public class StartHappensBeforeDemo {


    public static void main(String[] args) {
        showVar();
    }

    public static void showVar() {
        int myVar = 77;

        int finalMyVar = myVar;
        Thread b = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "开始执行");
            System.out.println("当前 myVar 值：" + finalMyVar);
        }, "线程B");
        System.out.println(Thread.currentThread().getName() + "开始执行，myVar=" + myVar);
        //仅start之前的值对线程B可见，后面对变量的修改对于线程B不可见
        b.start();
        System.out.println("Thread.currentThread().getName() after b.start(),myVar1=" + myVar);
        myVar += 1;
        System.out.println("Thread.currentThread().getName() after b.start(),myVar2=" + myVar);
    }


}
