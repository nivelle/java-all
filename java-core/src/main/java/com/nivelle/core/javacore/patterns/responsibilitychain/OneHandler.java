package com.nivelle.core.javacore.patterns.responsibilitychain;

public class OneHandler extends HandlerUser {


    public OneHandler(int level) {
        super(level);
    }

    @Override
    public boolean resolve() {

        System.out.println("1解决了问题");

        return true;
    }


}
