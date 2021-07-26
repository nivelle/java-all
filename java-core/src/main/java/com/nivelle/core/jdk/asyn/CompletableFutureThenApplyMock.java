package com.nivelle.core.jdk.asyn;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * supplyAsync-> 对 oneFuture的执行结果 应用 函数:thenApply
 *
 * @author fuxinzhong
 * @date 2021/04/03
 */
public class CompletableFutureThenApplyMock {

    public static void main(String[] args) throws Exception {
        thenAccept();
    }

    public static void thenAccept() throws Exception {
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
        //System.out.println("oneFuture after value:" + oneFuture.get());
        //基于thenRun()实现异步任务A，执行完毕后，激活异步任务B，这种激活的任务B能够获取A的执行结果的
        CompletableFuture twoFuture = oneFuture.thenApply(new Function<String, Object>() {

            @Override
            public String apply(String s) {
                try {
                    System.out.println("在 one future 的基础上，再次进行加工：" + s);
                } catch (Exception e) {

                }
                return s + "thenApplyValue";

            }
        });

        System.out.println("两个关键步骤执行完成：" + twoFuture.get());

    }
}
