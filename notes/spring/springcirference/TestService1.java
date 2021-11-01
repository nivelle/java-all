package com.nivelle.core.springcirference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 *
 * @author fuxinzhong
 * @date 2021/11/01
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TestService1 {
    @Autowired
    private TestService2 testService2;
//    @Autowired
//    private TestService3 testService3;

    //@Async 去掉注解，spring解决循环依赖问题
    public void test1() {
    }
}
