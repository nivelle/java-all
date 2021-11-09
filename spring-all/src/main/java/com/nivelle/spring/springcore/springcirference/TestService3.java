package com.nivelle.spring.springcore.springcirference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/11/01
 */
@Service
public class TestService3 {
    @Autowired
    private TestService1 testService1;

    public void test3() {
    }
}
