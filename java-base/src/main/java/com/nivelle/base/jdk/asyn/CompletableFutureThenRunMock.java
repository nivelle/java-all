package com.nivelle.base.jdk.asyn;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/02
 */
public class CompletableFutureThenRunMock {

    public static void main(String[] args) throws Exception {
        thenRun();
    }

    public static void thenRun() throws Exception {
        CompletableFuture<String> oneFuture = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(3000);

                } catch (InterruptedException e) {
                }
                return "one ok";
            }
        });
        System.out.println("one exec after");
        System.out.println("oneFuture after value:" + oneFuture.get());
        //基于thenRun()实现异步任务A，执行完毕后，激活异步任务B，这种激活的任务B是无法获取A的执行结果的
        CompletableFuture twoFuture = oneFuture.thenRun(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {

                }
                System.out.println("oneFuture 执行完之后，开始执行oneFuture thenRun 操作。。。");
                System.out.println(Thread.currentThread().getName() + "执行！");
            }
        });

        System.out.println("两个关键步骤执行完成：" + twoFuture.get());

    }
}
