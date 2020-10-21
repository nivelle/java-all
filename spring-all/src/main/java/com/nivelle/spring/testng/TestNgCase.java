package com.nivelle.spring.testng;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TestNg
 *
 * @author fuxinzhong
 * @date 2020/10/21
 */
public class TestNgCase {


    @Test
    public void f1() {
        System.out.println("test ng");
        Assert.assertTrue(true);
    }

    /**
     * 超时设置
     *
     * @throws Exception
     */
    @Test(timeOut = 1000)
    public void f2() throws Exception {
        System.out.println("test ng");
        Thread.sleep(1500);
        Assert.assertTrue(true);
    }


}