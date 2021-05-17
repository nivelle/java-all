package com.nivelle.core.patterns.guardedsuspension;

import java.util.LinkedList;

/**
 * 管程实现核心
 *
 * @author fuxinzhong
 * @date 2021/02/01
 */
public class RequestQueue {

    private final LinkedList<MyRequest> queue = new LinkedList<MyRequest>();

    public synchronized MyRequest getRequest() {
        while (queue.size() <= 0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        return (MyRequest) queue.removeFirst();
    }


    public synchronized void putRequest(MyRequest request) {
        queue.addLast(request);
        notifyAll();
    }
}
