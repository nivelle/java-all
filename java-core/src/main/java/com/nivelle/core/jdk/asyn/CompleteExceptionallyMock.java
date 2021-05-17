package com.nivelle.core.jdk.asyn;

import java.util.concurrent.CompletableFuture;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/05
 */
public class CompleteExceptionallyMock {

    public static void main(String[] args) throws Exception {
        CompletableFuture<String> completableFuture = new CompletableFuture();

        new Thread(() -> {

            try {
                if (true) {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }

            completableFuture.complete("ok");

        }).start();

        System.out.println(completableFuture.get());
    }

}
