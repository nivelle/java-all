package com.nivelle.guide.distributed.rpc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RpcBootstrap {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("classpath*:spring/providertest.xml");
    }
}
