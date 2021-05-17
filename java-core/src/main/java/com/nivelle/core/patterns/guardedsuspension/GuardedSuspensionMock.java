package com.nivelle.core.patterns.guardedsuspension;

/**
 * 等待唤醒机制的实现
 *
 * @author fuxinzhong
 * @date 2021/02/01
 */
public class GuardedSuspensionMock<T> {

    public static void main(String[] args) {
        RequestQueue requestQueue = new RequestQueue();
        new ClientThread(requestQueue, "Alice", 3141592L).start();
        new ServerThread(requestQueue, "Bobby", 6535897L).start();
    }
}
