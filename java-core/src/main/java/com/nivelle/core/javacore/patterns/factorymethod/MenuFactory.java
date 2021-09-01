package com.nivelle.core.javacore.patterns.factorymethod;

import com.nivelle.core.pojo.Compont;
import com.nivelle.core.pojo.Menu;

import java.util.ArrayList;
import java.util.List;

public class MenuFactory extends AbstractFactory {

    private List<Menu> list = new ArrayList<>();

    @Override
    public Menu createMenu(Long id, String name) {
        return new Menu(id, name);
    }

    @Override
    public boolean registerMenu(Compont compont) {
        System.out.println("组件注册上了" + compont.toString());
        list.add((Menu) compont);
        return true;
    }

    public List<Menu> getList() {
        return list;
    }
}
