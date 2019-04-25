package com.nivelle.programming.designpatterns.proxy;

import com.nivelle.programming.springboot.pojo.User;

import java.util.Objects;

public class ProxyObject implements CompanyUser {

    private RealObject realObject;

    public boolean doHardWork(User user) {

        System.out.println("代理对象做艰难的工作");

        return true;

    }

    public boolean doEasyWork(User user) {

        if (Objects.isNull(realObject)) {
            realObject = new RealObject();
            realObject.doEasyWork(user);
        } else {
            realObject.doEasyWork(user);
        }


        return true;
    }


}
