package com.nivelle.core.javacore.concurrent;

import org.assertj.core.util.Lists;

import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

/**
 * LinkedTransferQueue
 *
 * @author nivelle
 * @date 2020/04/14
 */
public class LinkedTransferQueueMock {

    public static void main(String[] args) {
        /**
         * LinkedTransferQueue 融合 LinkedBlockingQueue 和 SynchronousQueue 的功能，性能比 LinkedBlockingQueue 更好
         */
        LinkedTransferQueue linkedTransferQueue = new LinkedTransferQueue();

        linkedTransferQueue.add(1);
        linkedTransferQueue.add(2);
        System.out.println(linkedTransferQueue);

        List<Integer> col = Lists.newArrayList();
        linkedTransferQueue.drainTo(col);
        System.out.println(col);
        System.out.println(linkedTransferQueue);
    }
}
