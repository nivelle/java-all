package com.nivelle.core.javacore.patterns.responsibilitychain;

public class ThreeHandler extends HandlerUser {

    public ThreeHandler(int level) {
        super(level);
    }

    @Override
    public boolean resolve() {

        System.out.println("3解决了问题");

        return true;
    }


}