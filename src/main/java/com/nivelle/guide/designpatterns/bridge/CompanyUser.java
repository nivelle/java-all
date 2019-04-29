package com.nivelle.guide.designpatterns.bridge;

public class CompanyUser {

    private AbstractCompanyUserWork companyUserWork;


    public CompanyUser(AbstractCompanyUserWork companyUserWork){
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
