package com.nivelle.programming.java2e.annotion.myannotion;

public class EnglishUserImpl implements IUser {
    @Override
    public void login() {
        System.err.println("User Login！");
    }
}

