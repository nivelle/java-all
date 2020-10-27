package com.nivelle.spring.testng;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 依赖测试
 *
 * @author fuxinzhong
 * @date 2020/10/27
 */
@Test(suiteName = "TestNgDepend", skipFailedInvocations = true)
public class TestNgDepend {
    /**
     * 1. hard依赖：默认为此依赖方式，即其所有依赖的methods或者groups必须全部pass，否则被标识依赖的类或者方法将会被略过，在报告中标识为skip，如后面的范例所示，此为默认的依赖方式；
     * <p>
     * 2. soft依赖：此方式下，其依赖的方法或者组有不是全部pass也不会影响被标识依赖的类或者方法的运行，注意如果使用此方式，则依赖者和被依赖者之间必须不存在成功失败的因果关系，否则会导致用例失败。此方法在注解中需要加入alwaysRun=true即可，如@Test(dependsOnMethods= {"TestNgLearn1"}， alwaysRun=true)；
     */
    @Test(enabled = true)
    public void TestNgDependent1() {
        System.out.println("this is TestNgDependent1 test case1");
        Assert.assertFalse(true);
    }

    @Test(dependsOnMethods = {"TestNgDependent1"}, alwaysRun = true)
    public void TestNgDependent2() {
        System.out.println("this is TestNgDependent1 test case2");
    }


    @Test(groups = {"init"})
    public void serverStartedOk() {
        Assert.assertTrue(true);
    }

    @Test(groups = {"init"})
    public void initEnvironment() {
        Assert.assertTrue(false);
    }

    @Test(dependsOnGroups = {"init.*"})
    public void method1() {
        System.out.println("this is TestNgGroupDependent1 test cast3");
    }

}



