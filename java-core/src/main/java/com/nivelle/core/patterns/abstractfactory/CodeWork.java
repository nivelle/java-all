package com.nivelle.core.patterns.abstractfactory;

import com.nivelle.core.patterns.abstractfactory.AbstractWork;

public class CodeWork implements AbstractWork {


    public boolean doSomting(String userName) {

        System.out.println("程序员" + userName + "在写代码！！！");
        return false;
    }


}
