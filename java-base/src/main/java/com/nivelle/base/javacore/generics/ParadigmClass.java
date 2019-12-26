package com.nivelle.base.javacore.generics;

/**
 * 范型类
 *
 * @author fuxinzhong
 * @date 2019/11/12
 */
public class ParadigmClass<E> {

    private E element;


    /**
     *  虽然在方法中使用了泛型，但是这并不是一个泛型方法
     * @return
     */
    public E getElement() {
        return element;
    }

    public void setElement(E element) {
        this.element = element;
    }

    public ParadigmClass(E element) {
        this.element = element;
    }

}
