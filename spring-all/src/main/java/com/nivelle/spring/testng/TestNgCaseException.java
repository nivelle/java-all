package com.nivelle.spring.testng;

import org.testng.annotations.Test;

/**
 * @author fuxinzhong
 * @date 2020/10/27
 */
public class TestNgCaseException {

    /**
     * 预期异常测试
     */
    @Test(expectedExceptions = ArithmeticException.class)
    public void divisionWithException() {
//        Integer integer = null;
//        integer.intValue();
        int i = 1 / 0;
        System.out.println("after division the value of i is:" + i);
    }

    //@Test(enabled = false)
    @Test
    public void testNgIgnore() {
        System.out.println("this is TestNG ignore test");
    }
}
