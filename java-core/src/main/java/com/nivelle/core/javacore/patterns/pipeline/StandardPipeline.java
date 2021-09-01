package com.nivelle.core.javacore.patterns.pipeline;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/07/07
 */
public class StandardPipeline implements Pipeline {
    private Element first = null;
    private Element basic = null;

    @Override
    public Element getFirst() {
        return first;
    }

    @Override
    public Element getBasic() {
        return basic;
    }

    @Override
    public void setBasic(Element element) {
        this.basic = element;
    }

    @Override
    public void addElement(Element element) {
        if (first == null) {
            first = element;
            element.setNext(basic);
        } else {
            Element current = first;
            while (current != null) {
                if (current.getNext() == basic) {
                    current.setNext(element);
                    element.setNext(basic);
                    break;
                }
                current = current.getNext();
            }
        }
    }


}
