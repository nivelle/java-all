package com.nivelle.guide.designpatterns.abstractfactory;

public class Main {


    /**
     * 抽象工厂模式基于模版方法，将工厂进一步抽象
     *
     * @param args
     */
    public static void main(String[] args) {

        CompanyUserFactory companyUserFactory = new AdministrationUserFactory();
        companyUserFactory.oneDay("jessy");


        CompanyUserFactory companyUserFactory2 = new ProgrommerUserFactory();
        companyUserFactory2.oneDay("nivelle");


    }
}
