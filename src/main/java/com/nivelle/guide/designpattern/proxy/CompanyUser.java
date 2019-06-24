package com.nivelle.guide.designpattern.proxy;

import com.nivelle.guide.springboot.pojo.User;

public interface CompanyUser {

    boolean doEasyWork(User user);

    boolean doHardWork(User user);
}
