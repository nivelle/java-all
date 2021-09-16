package com.nivelle.core.javacore.asyn;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 基于 thenCombine 实现两个并发运行的CompletableFuture任务都执行完成之后，使用两者的结果作为参数再执行一个异步任务
 *
 * @author fuxinzhong
 * @date 2021/04/03
 */
public class CompletableFutureThenCombineMock {
    ////两个并发运行的 CompletableFuture 任务完成后，使用两者
    public static void main(String[] args) throws Exception {

        CompletableFuture<String> result = doSomeThingOne("fuck").thenCombine(doSomeThingTwo("456"), (one, two) -> {
            return one + "------" + two;
        });
        System.out.println(result.get());
    }

    public static CompletableFuture<String> doSomeThingOne(String params) {
        return CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {

                }
                return params + ":1";
            }
        });
    }

    public static CompletableFuture<String> doSomeThingTwo(String x) {
        return CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
                System.out.println("doSomeThingTwo return value:" + (x + ":2"));
                return x + ":2";
            }
        });
    }

}
