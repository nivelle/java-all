package com.nivelle.base.patterns.observer;

/**
 * 抽象被观察者接口
 *
 * @author fuxinzhong
 * @date 2020/05/31
 */
public interface Subject {

    void registerObserver(Observer o);

    void removeObserver(Observer o);

    void notifyObserver();

}
