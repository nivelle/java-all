package com.nivelle.base.javacore.loadclass;

/**
 * 实例初始化顺序,成员变量有方法赋值
 *
 * @author
 */
public class InitializerOrderDemo {

    /**
     * 初始化顺序:成员变量->构造函数
     * <p>
     * 若成员变量未设置值,则使用默认值;若设置则调用设置过程
     * <p>
     *     i=0 -> i= 1 -> i=constructor()=2
     *     j=0 -> j->getI()
     * </p>
     */

    public int j = getI();
    public int i = 1;

    public InitializerOrderDemo() {
        i = 2;
    }

    public int getI() {
        System.out.println(i);
        System.out.println(j);
        return i;
    }

    public static void main(String[] args) {
        InitializerOrderDemo ii = new InitializerOrderDemo();
        System.out.println(ii.j);
        System.out.println(ii.i);
    }

}
