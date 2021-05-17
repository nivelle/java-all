package com.nivelle.core.patterns.guardedsuspension;

import java.util.Random;

/**
 * 服务端消费
 *
 * @author fuxinzhong
 * @date 2021/02/01
 */
public class ServerThread extends Thread {

    private Random random;
    private RequestQueue requestQueue;

    public ServerThread(RequestQueue requestQueue, String name, long seed) {
        super(name);
        this.requestQueue = requestQueue;
        this.random = new Random(seed);
    }

    @Override
    public void run() {
        for (int i = 0; i < 2; i++) {
            MyRequest request = requestQueue.getRequest();
            System.out.println(Thread.currentThread().getName() + " 收到请求：  " + request);
            try {
                Thread.sleep(random.nextInt(1000));
            } catch (InterruptedException e) {
            }
        }
    }

}
