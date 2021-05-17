package com.nivelle.core.patterns.guardedsuspension;

import java.util.Random;

/**
 * 客户端线程不断生成请求，插入请求队列
 *
 * @author fuxinzhong
 * @date 2021/02/01
 */
public class ClientThread extends Thread {

    private Random random;
    private RequestQueue requestQueue;

    public ClientThread(RequestQueue requestQueue, String name, long seed) {
        super(name);
        this.requestQueue = requestQueue;
        this.random = new Random(seed);
    }

    @Override
    public void run() {
        for (int i = 0; i < 2; i++) {
            MyRequest request = new MyRequest("No." + i);
            System.out.println(Thread.currentThread().getName() + " 发送请求 " + request);
            requestQueue.putRequest(request);
            try {
                Thread.sleep(random.nextInt(1000));
            } catch (InterruptedException e) {
            }
        }
    }
}
