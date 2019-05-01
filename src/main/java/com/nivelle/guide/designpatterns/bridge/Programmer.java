package com.nivelle.guide.designpatterns.bridge;

public class Programmer extends CompanyUser {

    public Programmer(AbstractCompanyUserWork abstractCompanyUserWork) {
        super(abstractCompanyUserWork);
    }

    public void ondDayWork(String userName) {
        System.out.println(userName + "：下班被叫回来，加班进行中");
    }


}
