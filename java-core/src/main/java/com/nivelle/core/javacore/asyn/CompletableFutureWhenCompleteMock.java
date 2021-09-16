package com.nivelle.core.javacore.asyn;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 *  supplyAsync 任务A 执行完毕之后 处理结果 -> whenComplete
 *
 * @author fuxinzhong
 * @date 2021/04/03
 */
public class CompletableFutureWhenCompleteMock {

    public static void main(String[] args) throws Exception {
        whenCompleteMock();
    }

    public static void whenCompleteMock() throws Exception {
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
        CompletableFuture twoFuture = oneFuture.whenComplete(new BiConsumer<String, Throwable>() {
            @Override
            public void accept(String s, Throwable throwable) {
                try {
                    Thread.sleep(1000);

                } catch (Exception e) {

                }
                if (throwable == null) {
                    System.out.println("没有异常信息");
                } else {
                    System.out.println(throwable.getMessage());
                }
                System.out.println("oneFuture.whenComplete value:" + s);

            }
        });

        System.out.println("两个关键步骤执行完成：" + twoFuture.get());

    }
}
