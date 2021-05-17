package com.nivelle.core.patterns.abstractfactory;

public class ProgrommerUserFactory implements CompanyUserFactory {

    //1:打卡
    @Override
    public boolean clickDick(String userName) {
        System.out.println(userName + "打卡");
        return true;
    }

    //2.工作具体的内容
    @Override
    public String doWork(String userName) {


        AbstractWork abstractWork = new CodeWork();
        abstractWork.doSomting(userName);

        return userName;
    }

    //3.下班
    @Override
    public boolean afterWork(String userName) {
        System.out.println(userName + "下班");
        return true;
    }
    @Override
    public void oneDay(String userName) {
        clickDick(userName);
        doWork(userName);
        afterWork(userName);

    }


}
