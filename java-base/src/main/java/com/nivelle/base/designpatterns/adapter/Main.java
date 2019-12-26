package com.nivelle.base.designpatterns.adapter;

public class Main {

    /**
     * 使用类的适配器模式:版本升级兼容，重复利用已经写好的经过充分测试的代码
     **/
    public static void main(String[] args) {

        Print print = new PrintBanner();

        String result = print.printFix("nivelle");

        System.out.println(result);

        Target target = new Adapter(print);

        String result2 = target.showAround("love", "jessy");

        System.out.println(result2);

    }
}
