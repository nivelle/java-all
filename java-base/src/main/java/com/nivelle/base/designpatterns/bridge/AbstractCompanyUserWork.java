 package com.nivelle.base.designpatterns.bridge;

public abstract class AbstractCompanyUserWork {


    /**
     * 打卡
     *
     * @param userName
     * @return
     */
    public abstract boolean clickDick(String userName);

    /**
     * 下班结束工作
     *
     * @param userName
     * @return
     */
    public abstract boolean endWork(String userName);

}
