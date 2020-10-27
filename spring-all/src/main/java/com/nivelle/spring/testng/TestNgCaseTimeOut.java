package com.nivelle.spring.testng;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TestNg
 *
 * @author fuxinzhong
 * @date 2020/10/21
 */
public class TestNgCaseTimeOut {


    @Test
    public void f1() {
        System.out.println("test1 ng");
        Assert.assertTrue(true);
    }

    /**
     * 超时测试
     *
     * @throws Exception
     */
    @Test(timeOut = 1000)
    public void f2() throws Exception {
        System.out.println("test2 ng");
        Thread.sleep(1500);
        Assert.assertTrue(true);
    }


}