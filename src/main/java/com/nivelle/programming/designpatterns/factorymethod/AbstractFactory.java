package com.nivelle.programming.designpatterns.factorymethod;

import com.nivelle.programming.springboot.pojo.Compont;
import com.nivelle.programming.springboot.pojo.Menu;

/**
 * 使用模板方法模式实现
 */
public abstract class AbstractFactory {

    public boolean addMenu(Long id,String name){

        Menu menu = createMenu(id,name);

        Boolean result = registMenu(menu);

        return result;

    }

    public abstract Menu createMenu(Long id, String name);

    public abstract boolean registMenu(Compont compont);
}
