package com.nivelle.base.javacore.generics;

/**
 * 范型接口实现类2
 *
 * @author fuxinzhong
 * @date 2019/11/12
 */
public class ParadigmInterfaceService2 implements ParadigmInterface<String> {

    @Override
    public String getElement() {
        System.out.println("ParadigmInterface T is:" + String.class.getName());
        return "1";
    }
}
