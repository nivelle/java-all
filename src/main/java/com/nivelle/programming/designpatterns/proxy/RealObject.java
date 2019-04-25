package com.nivelle.programming.designpatterns.proxy;

import com.nivelle.programming.springboot.pojo.User;

public class RealObject implements CompanyUser {

    public boolean doHardWork(User user) {

        System.out.println("真实对象做艰难的工作");

        return true;

    }

    public boolean doEasyWork(User user) {
        System.out.println("真实对象做简单的工作");

        return true;
    }
}
