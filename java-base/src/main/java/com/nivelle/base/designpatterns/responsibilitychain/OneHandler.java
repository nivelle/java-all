package com.nivelle.base.designpatterns.responsibilitychain;

public class OneHandler extends HandlerUser {


    public OneHandler(int level) {
       super(level);
    }


    public boolean resolve() {

        System.out.println("1解决了问题");

        return true;
    }


}
