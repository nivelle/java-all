package com.nivelle.core.springcirference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 循环引用
 *
 * @author fuxinzhong
 * @date 2021/11/01
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TestService2 {
    @Autowired
    private TestService1 testService1;
    public void test2() {
    }
}
