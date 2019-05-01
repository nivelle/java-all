package com.nivelle.guide.designpatterns.bridge;

public class CompanyUserWorkImpl extends AbstractCompanyUserWork {

    /**
     * 打开
     *
     * @param userName
     * @return
     */
    public boolean clickDick(String userName) {


        System.out.println(userName + "：打卡");

        return true;

    }

    /**
     * 下班结束工作
     *
     * @param userName
     * @return
     */
    public boolean endWork(String userName) {

        System.out.println(userName + "：下班");

        return true;

    }
}
