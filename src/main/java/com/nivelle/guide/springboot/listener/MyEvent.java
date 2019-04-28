package com.nivelle.guide.springboot.listener;

import org.springframework.context.ApplicationEvent;

public class MyEvent extends ApplicationEvent {


    public MyEvent(Object source){
        super(source);
    }


}
