package com.nivelle.core.javacore.jdk.asyn;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * supplyAsync 产生数据-> thenAccept 消费数据
 *
 * @author fuxinzhong
 * @date 2021/04/03
 */
public class CompletableFutureThenAcceptMock {

    public static void main(String[] args) throws Exception {
        thenAccept();
    }

    public static void thenAccept() throws Exception {
        CompletableFuture<String> oneFuture = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(3000);
                    System.out.println("exec one");
                } catch (InterruptedException e) {
                }
                return "one ok";
            }
        });
        System.out.println("one exec after");
        //System.out.println("oneFuture after value:" + oneFuture.get());
        CompletableFuture twoFuture = oneFuture.thenAccept(new Consumer<String>() {
            @Override
            public void accept(String s) {
                try {
                    Thread.sleep(3000);
                    System.out.println("exec two");
                } catch (InterruptedException e) {
                }

                System.out.println("tow accept one value:" + s);
            }
        });

        System.out.println("两个关键步骤执行完成");
        Thread.sleep(8000);
        System.out.println("消费者消费数据："+twoFuture.get());

    }
}
