package com.nivelle.base.designpatterns.templatemethod;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractCompany {

    private String userName;


    //1.打卡
    public boolean showCard(String userName) {
        System.out.println(userName + "打卡了！");
        return true;
    }
    //2.工作
    public abstract boolean doWork(String userName);

    //3.下班
    public boolean endDay(String userName) {
        System.out.println(userName + "下班啦！");
        return true;
    }

    public boolean oneDay(String userName){
        showCard(userName);
        doWork(userName);
        endDay(userName);
        return true;
    }
}
