package com.nivelle.core.javacore.patterns.workerthread;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/02/01
 */
public class Main {
    public static void main(String[] args) {
        Channel channel = new Channel(5);
        channel.startWorkers();
        new ClientThread("Alice", channel).start();
        new ClientThread("Bobby", channel).start();
        new ClientThread("Chris", channel).start();
    }
}
