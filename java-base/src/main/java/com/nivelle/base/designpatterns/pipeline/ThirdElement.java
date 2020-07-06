package com.nivelle.base.designpatterns.pipeline;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/07/07
 */
public class ThirdElement implements Element {
    private Element nextElement;
    @Override
    public Element getNext() {
        return this.nextElement;
    }

    @Override
    public void setNext(Element element) {
        this.nextElement = element;

    }

    @Override
    public void invoke(String handling) {
        handling = handling.replaceAll("zz", "yy");
        System.out.println("Third阀门处理完后：" + handling);
        getNext().invoke(handling);
    }
}
