package com.nivelle.core.javacore.patterns.pipeline;

/**
 * 管道接口
 *
 * @author fuxinzhong
 * @date 2020/07/07
 */
public interface Pipeline {

    Element getFirst();

    Element getBasic();

    void setBasic(Element element);

    void addElement(Element element);


}
