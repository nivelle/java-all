package com.nivelle.spring.springlife.lifecycle;

/**
 * 通过BeanDefinitionRegisterPostProcess注入的Bean定义
 * @author fuxinzhong
 * @date 2019/09/27
 */
public class Animal {
    public Animal(String color) {
        this.color = color;
    }

    private String color;

    public Animal() {
    }


    @Override
    public String toString() {
        return "Animal{" +
                "color='" + color + '\'' +
                '}';
    }
}
