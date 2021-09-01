package com.nivelle.core.javacore.patterns.factorymethod;

import com.nivelle.core.pojo.Compont;
import com.nivelle.core.pojo.Menu;

/**
 * 使用工程方法模式实现
 */
public abstract class AbstractFactory {

    public boolean addMenu(Long id, String name) {
        Menu menu = createMenu(id, name);
        Boolean result = registerMenu(menu);
        return result;
    }

    public abstract Menu createMenu(Long id, String name);

    public abstract boolean registerMenu(Compont compont);
}
