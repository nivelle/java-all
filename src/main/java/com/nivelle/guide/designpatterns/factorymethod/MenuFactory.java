package com.nivelle.guide.designpatterns.factorymethod;

import com.nivelle.guide.springboot.pojo.Compont;
import com.nivelle.guide.springboot.pojo.Menu;

import java.util.ArrayList;
import java.util.List;

public class MenuFactory extends AbstractFactory {

    private List<Menu> list = new ArrayList<>();


    public Menu createMenu(Long id, String name) {

        return new Menu(id, name);
    }

    public boolean registMenu(Compont compont) {

        System.out.println("组件注册上了"+compont.toString());

        list.add((Menu) compont);

        return true;

    }


    public List<Menu> getList() {
        return list;
    }
}
