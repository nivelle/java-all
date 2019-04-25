package com.nivelle.programming.designpatterns.proxy;

import com.nivelle.programming.springboot.pojo.User;

public interface CompanyUser {

    boolean doEasyWork(User user);

    boolean doHardWork(User user);
}
