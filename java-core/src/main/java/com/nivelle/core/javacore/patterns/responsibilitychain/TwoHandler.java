package com.nivelle.core.javacore.patterns.responsibilitychain;

public class TwoHandler extends HandlerUser {


    public TwoHandler(int level) {
        super(level);
    }

    @Override
    public boolean resolve() {

        System.out.println("2解决了问题");

        return true;
    }


}