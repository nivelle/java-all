package com.nivelle.spring.springcore.lifecycle;

/**
 * @author nivelle
 * @date 2019/08/21
 */
public class XmlBean {

    public String helloXmlService() {
        System.out.println("通过xml实例化类！！！！！！");
        return "hello ->xmlService";
    }
}
