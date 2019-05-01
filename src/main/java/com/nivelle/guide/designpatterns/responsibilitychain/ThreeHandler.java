package com.nivelle.guide.designpatterns.responsibilitychain;

public class ThreeHandler extends HandlerUser {

    public ThreeHandler(int level) {
        super(level);
    }


    public boolean resolve() {

        System.out.println("3解决了问题");

        return true;
    }


}