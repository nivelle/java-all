package com.nivelle.guide.javacore.annotion.myannotion;

public class ChineseUserImpl implements IUser {
    @Override
    public void login() {
        System.err.println("用户登录！");
    }
}

