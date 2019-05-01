package com.nivelle.guide.designpatterns.templatemethod;

public class Main {

    /**
     * 模板方法模式：由模板来定义流程，子类负责具体实现。
     *
     * @param args
     */
    public static void main(String[] args) {
        AbstractCompany user = new Programmer();
        user.oneDay("nivelle");
        AbstractCompany user2 = new Boss();
        user.oneDay("fuck");

    }

}
