package com.nivelle.spring.testng;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * 参数化测试
 *
 * @author fuxinzhong
 * @date 2020/10/27
 */
public class TestNgParams {

    @Test(enabled = true)
    @Parameters({"param1", "param2"})
    public void paramsTest1(String param1, String param2) {
        System.out.println("this is testParam1 param1=" + param1 + ",param2=" + param2);
        Assert.assertTrue(true);
    }

    @Test(dependsOnMethods = {"paramsTest1"})
    public void paramsTest2() {
        System.out.println("this is testParam2 is ok");
        Assert.assertTrue(true);
    }

    @DataProvider(name = "provideNumbers")
    public Object[][] provideData() {
        return new String[][]{{"fuck", "20"}, {"100", "100"}, {"200", "20"}};
    }

    @Test(dataProvider = "provideNumbers")
    public void paramsTest3(String param1, String param2) {
        System.out.println("this is TestNG test case1, and param1 is:" + param1 + "; param2 is:" + param2);
        Assert.assertTrue(true);
    }
}
