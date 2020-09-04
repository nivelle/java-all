package com.nivelle.base.patterns.observer;

/**
 * 具体观察者
 *
 * @author fuxinzhong
 * @date 2020/05/31
 */
public class ObserverConcrete implements Observer {

    private String name;
    private String message;

    public ObserverConcrete(String name) {
        this.name = name;
    }

    @Override
    public void update(String message) {
        this.message = message;
        read();
    }

    public void read() {
        System.out.println(name + " 收到推送消息： " + message);
    }
}
