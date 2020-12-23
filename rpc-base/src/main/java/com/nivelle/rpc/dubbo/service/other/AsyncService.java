package com.nivelle.rpc.dubbo.service.other;

import java.util.concurrent.Future;

/**
 * 异步方法
 * @author nivelle
 * @date 2019/08/23
 */
public interface AsyncService {

    Future<String> asyncSayHello();


    Future<String> asyncSayHello2();

}
