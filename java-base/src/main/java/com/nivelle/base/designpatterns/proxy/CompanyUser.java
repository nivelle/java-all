package com.nivelle.base.designpatterns.proxy;

import com.nivelle.base.pojo.User;

public interface CompanyUser {

    boolean doEasyWork(User user);

    boolean doHardWork(User user);
}
