package com.nivelle.base.designpatterns.abstractfactory;

public class ProgrommerUserFactory implements CompanyUserFactory {

    //1:打卡
    public boolean clickDick(String userName) {
        System.out.println(userName + "打卡");
        return true;
    }

    //2.工作具体的内容
    public String doWork(String userName) {


        AbstractWork abstractWork = new CodeWork();
        abstractWork.doSomting(userName);

        return userName;
    }

    //3.下班
    public boolean afterWork(String userName) {
        System.out.println(userName + "下班");
        return true;
    }

    public void oneDay(String userName) {
        clickDick(userName);
        doWork(userName);
        afterWork(userName);

    }


}
