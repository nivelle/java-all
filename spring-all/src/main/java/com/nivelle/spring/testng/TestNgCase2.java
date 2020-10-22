package com.nivelle.spring.testng;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TestNg
 *
 * @author fuxinzhong
 * @date 2020/10/21
 */
public class TestNgCase2 {


    @Test
    public void f3() {
        System.out.println("test3 ng");
        Assert.assertTrue(true);
    }

    /**
     * 超时设置
     *
     * @throws Exception
     */
    @Test
    public void f4() throws Exception {
        System.out.println("test4 ng");
        Thread.sleep(1500);
        Assert.assertTrue(true);
    }


}