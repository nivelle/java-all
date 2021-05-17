package com.nivelle.core.jdk.asyn;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/02
 */
public class CompletableFutureSupplyMock {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        //基于 supplyAsync 实现有返回值的异步计算
        CompletableFuture completableFuture = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {

                }
                return "fuck jessy";
            }
        });
        System.out.println("等待主线程执行。。。。");
        System.out.println(completableFuture.get());
    }
}
