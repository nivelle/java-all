package com.nivelle.base.designpatterns.abstractfactory;

public interface CompanyUserFactory {

    //1:打卡
    boolean clickDick(String userName);
    //2.工作具体的内容
    String doWork(String  userName);
    //3.下班
    boolean afterWork(String userName);
    //整合逻辑
    void oneDay(String userName);
}
