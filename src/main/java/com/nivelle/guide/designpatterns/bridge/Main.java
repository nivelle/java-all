package com.nivelle.guide.designpatterns.bridge;

public class Main {
    /**
     * 类的功能层次结构和类的实现层次结构分离。继承是强关联，委托是弱关联。
     *
     * @param args
     */
    public static void main(String[] args) {

        AbstractCompanyUserWork abstractCompanyUserWork = new CompanyUserWorkImpl();
        CompanyUser companUser = new CompanyUser(abstractCompanyUserWork);
        companUser.oneDay("jessy");

        CompanyUser companUser1 = new Programmer(abstractCompanyUserWork);
        companUser1.oneDay("nivelle");
        ((Programmer) companUser1).ondDayWork("nivelle");
    }
}
