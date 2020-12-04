package com.nivelle.spring.springcore.beanFactory;

/**
 * 自动装配
 *
 * @author fuxinzhong
 * @date 2020/12/04
 */
public class Person {


    /**
     * 开启自动绑定之后会将我们放入到IOC中的对象自动执行其setter方法，在javaBean中一个属性对应着一个setter和一个getter方法
     * 根据约定setXxx其Xxx把首字母改为小写之后就是这个对象中有的属性名。
     */
    private User user;

    public void setUser(User user) {
        this.user = user;
    }
}
