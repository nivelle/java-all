package com.nivelle.base.jdk.jvm;

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
     * i=0 -> i= 1 -> i=constructor()=2
     * j=0 -> j->getI()
     * </p>
     */
    public int j = getI();
    public int i = 1;
    public int k = getI();//再次调用的时候，i已经实例化了

    public InitializerOrderDemo() {
        i = 2;
    }

    public int getI() {
        System.out.println("i实例化前i=：" + i);
        System.out.println("j=" + j);
        System.out.println("k=" + k);
        return i;
    }

    public static void main(String[] args) {
        InitializerOrderDemo initializerOrderDemo = new InitializerOrderDemo();
        System.out.println("实例化之后，构造函数调用完毕：");
        System.out.println("实例化之后k=" + initializerOrderDemo.k);
        System.out.println("实例化之后j=" + initializerOrderDemo.j);
        System.out.println("实例化之后i=" + initializerOrderDemo.i);

        System.out.println("===============");

        int i = initializerOrderDemo.getI();
        System.out.println("构造函数执行完之后i：" + i);
    }

}
