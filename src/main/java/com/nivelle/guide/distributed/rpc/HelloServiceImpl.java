package com.nivelle.guide.distributed.rpc;

@RpcService(HelloService.class)// 指定远程接口
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name){
        return "Hello! " + name;
    }
}
