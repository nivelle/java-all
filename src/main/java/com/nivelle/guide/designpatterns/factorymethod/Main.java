package com.nivelle.guide.designpatterns.factorymethod;

import com.nivelle.guide.springboot.pojo.Menu;

public class Main {


    /**
     * 用模板方法模式生成实例的模式就是工厂模式
     * @param args
     */

    public static void main(String[] args) {
        AbstractFactory factory = new MenuFactory();

        Menu menu = factory.createMenu(1L, "用户管理");
        factory.registMenu(menu);
        menu.canShow();

        Menu menu2 = factory.createMenu(2L, "权限管理");
        factory.registMenu(menu2);
        menu2.canShow();

        System.out.println("总共有" + ((MenuFactory) factory).getList().size() + "个菜单");
    }


}
