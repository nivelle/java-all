package com.nivelle.programming.designpatterns.templatemethod;

public class Programmer extends  AbstractCompany{

    @Override
    public boolean doWork(String userName) {

        System.out.println(userName+"coding day and night!!");
        return true;
    }
}
