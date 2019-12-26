package com.nivelle.base.designpatterns.proxy;

import com.nivelle.base.pojo.User;

public class Main {

    /**
     * 代理模式：直到有必要才创建真实对象，来提高速率。代理对象和真实对象是透明的。
     *
     * @param args
     */

    public static void main(String[] args) {

        CompanyUser realObject = new ProxyObject();

        User user = new User(1, "jessy");

        realObject.doEasyWork(user);
        realObject.doHardWork(user);

    }
}
