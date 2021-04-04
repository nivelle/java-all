package com.nivelle.base.jdk.asyn;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/03
 */
public class ManyCompletableMock {

    public static void main(String[] args) throws Exception {
        CompletableFuture<String> result = doSomeThingOne("fuck jessy").thenCompose(x -> doSomeThingTwo(x));
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
                return x + ":2";
            }
        });
    }

}
