package com.nivelle.spring.testng;

import org.testng.annotations.*;
import org.testng.Assert;

/**
 * testNg常用注解
 *
 * @author fuxinzhong
 * @date 2020/10/27
 */
public class TestNgCaseGroup {

    @Test(groups = "group1")
    public void test1() {
        System.out.println("test1 from group1");
        Assert.assertTrue(true);
    }

    @Test(groups = "group1")
    public void test11() {
        System.out.println("test11 from group1");
        Assert.assertTrue(true);
    }

    @Test(groups = "group2")
    public void test2() {
        System.out.println("test2 from group2");
        Assert.assertTrue(true);
    }

    /**
     * 该test标签内的类的所有测试方法之前执行
     */
    @BeforeTest
    public void beforeTest() {
        System.out.println("beforeTest");
    }

    /**
     * 该test标签内的类的所有测试方法之后执行
     */
    @AfterTest
    public void afterTest() {
        System.out.println("afterTest");
    }

    /**
     * 当前类的第一个测试方法之前运行，仅执行一次
     */
    @BeforeClass
    public void beforeClass() {
        System.out.println("beforeClass");
    }

    /**
     * 当前类的最后一个测试方法之后运行，仅执行一次
     */
    @AfterClass
    public void afterClass() {
        System.out.println("afterClass");
    }

    /**
     * 该 suite 其他方法执行之前，仅执行一次
     */
    @BeforeSuite
    public void beforeSuite() {
        System.out.println("beforeSuite");
    }

    /**
     * 该 suite 其他方法执行最后，仅执行一次
     */
    @AfterSuite
    public void afterSuite() {
        System.out.println("afterSuite");
    }

    /**
     * 分组最前执行;只对group1有效，即test1和test11
     */

    @BeforeGroups(groups = "group1")
    public void beforeGroups() {
        System.out.println("beforeGroups->group1 & group11");
    }

    /**
     * 分组最后执行；只对group1有效，即test1和test11
     */
    @AfterGroups(groups = "group1")
    public void afterGroups() {
        System.out.println("afterGroups->group1 & group11");
    }

    /**
     * 在每个测试方法之前执行
     */
    @BeforeMethod
    public void beforeMethod() {
        System.out.println("beforeMethod");
    }

    /**
     * 在每个测试方法之后执行
     */
    @AfterMethod
    public void afterMethod() {
        System.out.println("afterMethod");
    }
}
