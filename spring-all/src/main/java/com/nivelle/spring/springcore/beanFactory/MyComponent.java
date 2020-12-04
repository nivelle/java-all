package com.nivelle.spring.springcore.beanFactory;

/**
 * spring 自动装配
 *
 * @author fuxinzhong
 * @date 2020/12/03
 */
public interface MyComponent {
    /**
     * 在开启Spring的自动装配之后，一个放入到IOC中的对象中有setTestBean()方法:
     *
     * 1. 如果此时自动绑定的规则是 byName 那么Spring会去IOC中寻找testBean这个对象，如果有的会就会执行该setter方法;
     *
     * 2. 如果是byType的方式,就是会寻找当前setter方法里对应的参数类型的bean。如果找到多个同样抛出异常;
     */

    void print();
}
