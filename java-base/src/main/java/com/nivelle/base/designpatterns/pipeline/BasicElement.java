package com.nivelle.base.designpatterns.pipeline;

/**
 * 基础阀门，也就是最后一个阀门
 *
 * @author fuxinzhong
 * @date 2020/07/07
 */
public class BasicElement implements Element {

    private Element nextElement;

    @Override
    public Element getNext() {
        return this.nextElement;
    }

    @Override
    public void setNext(Element element) {
        this.nextElement =element;
    }

    @Override
    public void invoke(String handling) {
        handling=handling.replaceAll("aa", "bb");
        System.out.println("最后阀门处理完后：" + handling);
    }
}


