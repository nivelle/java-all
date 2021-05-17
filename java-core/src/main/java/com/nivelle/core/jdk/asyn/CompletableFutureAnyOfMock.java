package com.nivelle.core.jdk.asyn;

import org.assertj.core.util.Lists;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/05
 */
public class CompletableFutureAnyOfMock {

    public static void main(String[] args) throws Exception {

        List<CompletableFuture> list = Lists.newArrayList();
        list.add(doSomeThingOne("1"));
        list.add(doSomeThingOne("2"));
        list.add(doSomeThingOne("4"));
        list.add(doSomeThingOne("3"));

        CompletableFuture<Object> result = CompletableFuture.anyOf(list.toArray(new CompletableFuture[list.size()]));

        System.out.println(result.get());

    }

    public static CompletableFuture<String> doSomeThingOne(String params) {
        return CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
                System.out.println(Thread.currentThread().getName()+"执行完毕！");
                return params + ":1";
            }
        });
    }
}
