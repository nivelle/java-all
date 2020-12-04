package com.nivelle.spring.springcore.beanFactory;

import org.springframework.stereotype.Component;

/**
 * 自动装配
 *
 * @author fuxinzhong
 * @date 2020/12/03
 */

@Component
public class MyComponentAImpl implements MyComponent {

    public String mark = "a";

    @Override
    public void print(){
        System.out.println(mark);
    }
}
