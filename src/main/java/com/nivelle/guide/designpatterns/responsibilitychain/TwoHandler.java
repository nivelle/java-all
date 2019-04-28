package com.nivelle.guide.designpatterns.responsibilitychain;

public class TwoHandler extends HandlerUser {


    public TwoHandler(int level) {
        super(level);
    }


    public boolean resolve() {

        System.out.println("2解决了问题");

        return true;
    }


}