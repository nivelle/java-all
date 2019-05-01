package com.nivelle.guide.designpatterns.factorymethod;

import com.nivelle.guide.springboot.pojo.Compont;
import com.nivelle.guide.springboot.pojo.Menu;

/**
 * 使用工程方法模式实现
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
