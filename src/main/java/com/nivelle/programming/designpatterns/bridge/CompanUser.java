package com.nivelle.programming.designpatterns.bridge;

public class CompanUser {

    private AbstractCompanyUserWork companyUserWork;


    public CompanUser(AbstractCompanyUserWork companyUserWork){
        this.companyUserWork = companyUserWork;
    }

    public void clickDick(String userName){
        companyUserWork.clickDick(userName);
    }

    public void endWork(String userName){
        companyUserWork.endWork(userName);
    }

    public void oneDay(String username){
        clickDick(username);
        endWork(username);
    }


}
