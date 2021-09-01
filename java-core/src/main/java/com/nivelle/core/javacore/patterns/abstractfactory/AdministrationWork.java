package com.nivelle.core.javacore.patterns.abstractfactory;

public class AdministrationWork implements AbstractWork {


    public boolean doSomting(String userName) {

        System.out.println("行政" + userName + "在查考勤！！！");

        return true;
    }
}
