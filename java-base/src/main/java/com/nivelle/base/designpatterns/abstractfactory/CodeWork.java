package com.nivelle.base.designpatterns.abstractfactory;

public class CodeWork implements AbstractWork {


    public boolean doSomting(String userName) {

        System.out.println("程序员" + userName + "在写代码！！！");
        return false;
    }


}
