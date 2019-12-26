package com.nivelle.base.javacore.generics;

/**
 * 范型接口实现类
 *
 * @author fuxinzhong
 * @date 2019/11/12
 */
public class ParadigmInterfaceService implements ParadigmInterface<Integer> {

    @Override
    public Integer getElement() {
        System.out.println("ParadigmInterface T is:" + Integer.class.getName());
        return 0;
    }
}
