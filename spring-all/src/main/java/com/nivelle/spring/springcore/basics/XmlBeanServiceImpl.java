package com.nivelle.spring.springcore.basics;

import org.springframework.stereotype.Component;

/**
 * @author fuxinzhong
 * @date 2019/08/21
 */
@Component
public class XmlBeanServiceImpl {

    public String helloXmlService() {
        System.out.println("通过xml实例化类！！！！！！");
        return "hello ->xmlService";
    }
}
