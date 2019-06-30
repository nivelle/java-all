package com.nivelle.guide.javacore.annotion.myannotion;

public class EnglishUserImpl implements IUser {
    @Override
    public void login() {
        System.err.println("User Login！");
    }
}

