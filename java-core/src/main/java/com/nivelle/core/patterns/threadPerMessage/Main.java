package com.nivelle.core.patterns.threadPerMessage;

/**
 * Thread-Per-Message模式是指每个message一个线程，message可以理解成“消息”、“命令”或者“请求”。
 * 每一个message都会分配一个线程，由这个线程执行工作，使用Thread-Per-Message Pattern时，“委托消息的一端”与“执行消息的一端”回会是不同的线程。
 *
 * @author fuxinzhong
 * @date 2021/02/01
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("main BEGIN");
        Host host = new Host();
        host.request(10, 'A');
        host.request(20, 'B');
        host.request(30, 'C');
        System.out.println("main END");
    }
}
