package com.nivelle.programming;

import com.nivelle.programming.distributed.rpc.HelloService;
import com.nivelle.programming.distributed.rpc.RpcProxy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/consumertest.xml")
public class HelloServiceTest {
    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest() {
        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.sayHello("nivelle");
        Assert.assertEquals("Hello! nivelle", result);
    }

}
