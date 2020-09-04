package com.nivelle.base.patterns.templatemethod;

public class Boss extends  AbstractCompany {

    @Override
    public boolean doWork(String userName) {

        System.out.println("睡大觉，和秘书唠嗑！！");
        return false;
    }
}
