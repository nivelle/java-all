package com.nivelle.core.javacore.patterns.pipeline;

/**
 * 第二个阀门
 *
 * @author fuxinzhong
 * @date 2020/07/07
 */
public class SecondElement implements Element {

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
        handling = handling.replaceAll("11", "22");
        System.out.println("Second阀门处理完后：" + handling);
        getNext().invoke(handling);
    }
}
