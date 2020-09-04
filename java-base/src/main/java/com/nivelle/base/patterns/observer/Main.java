package com.nivelle.base.patterns.observer;

import java.util.ArrayList;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/05/31
 */
public class Main {

    public static void main(String[] args) {
        //被观察者
        SubjectConcrete server = new SubjectConcrete(new ArrayList());

        Observer userZhang = new ObserverConcrete("ZhangSan");
        Observer userLi = new ObserverConcrete("LiSi");
        Observer userWang = new ObserverConcrete("WangWu");

        server.registerObserver(userZhang);
        server.registerObserver(userLi);
        server.registerObserver(userWang);
        server.setInfomation("PHP是世界上最好用的语言！");

        System.out.println("----------------------------------------------");
        server.removeObserver(userZhang);
        server.setInfomation("JAVA是世界上最好用的语言！");
    }
}
