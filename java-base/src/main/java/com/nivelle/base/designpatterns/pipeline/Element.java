package com.nivelle.base.designpatterns.pipeline;

/**
 * 管道模式，阀门元素
 *
 * @author fuxinzhong
 * @date 2020/07/07
 */
public interface Element {

    Element getNext();

    void setNext(Element element);

    void invoke(String handling);

}
