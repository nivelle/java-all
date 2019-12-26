package com.nivelle.rpc.initstart;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * ApplicationRunner
 *
 * @author fuxinzhong
 * @date 2019/10/16
 */
@Component
public class MyApplicationRunner implements ApplicationRunner {


    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.err.println("ApplicationRunner 初始化资源 ");
    }

}
