package com.nivelle.guide.distributed.rpc;


// 指定远程接口
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name){
        return "Hello! " + name;
    }
}
