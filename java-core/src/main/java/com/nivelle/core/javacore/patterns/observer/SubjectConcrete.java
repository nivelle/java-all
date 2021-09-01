package com.nivelle.core.javacore.patterns.observer;

import java.util.List;

/**
 * 具体被观察者
 *
 * @author fuxinzhong
 * @date 2020/05/31
 */
public class SubjectConcrete implements Subject {

    private List<Observer> list;

    private String message;

    public SubjectConcrete(List list) {
        this.list = list;
    }

    @Override
    public void registerObserver(Observer o) {

        list.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        if (!list.isEmpty())
            list.remove(o);
    }

    //遍历
    @Override
    public void notifyObserver() {
        for (int i = 0; i < list.size(); i++) {
            Observer observer = list.get(i);
            observer.update(message);
        }
    }

    public void setInfomation(String s) {
        this.message = s;
        System.out.println("服务更新消息： " + s);
        //消息更新，通知所有观察者
        notifyObserver();
    }
}
