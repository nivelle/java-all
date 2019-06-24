package com.nivelle.guide.designpatterns.responsibilitychain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class HandlerUser {

    private static final int LEVEL = 4;

    private int level;

    private HandlerUser next;

    public HandlerUser setNext(HandlerUser next){
        this.next = next;
        return next;
    }

    public HandlerUser(int level){
        this.level = level;
    }

    public abstract boolean resolve();


    public boolean chainResolve(){

        if(level==LEVEL){
            return resolve();
        }else if(next!=null){
            return next.chainResolve();
        }else {
            System.out.println("这个问题解决不了");
        }
        return false;
    }


}
