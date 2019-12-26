package com.nivelle.base.javacore.generics;

/**
 * 范型类
 *
 * @author fuxinzhong
 * @date 2019/11/12
 */
public class Main {

    /**
     * 泛型类,是在实例化类的时候指明泛型的具体类型；泛型方法，是在调用方法的时候指明泛型的具体类型 。
     */

    public static void main(String[] args) {
        /**
         * new ParadigmClass<> 范型可以省略
         */
        ParadigmClass<Integer> paradigmClass = new ParadigmClass(123);
        System.out.println(paradigmClass.getElement().getClass().getTypeName());
        /**
         * 用特殊类型接受范型类的返回方法则需要在类型上声明ParadigmClass<Integer> = 。。。
         * 否则默认返回类型是Object
         */
        Integer classResult = paradigmClass.getElement();
        System.out.println(classResult);

        /**
         * 范型接口:不同的范性类型对应不同的实现
         */
        ParadigmInterface paradigmInterface = new ParadigmInterfaceService();
        System.out.println(paradigmInterface.getElement());

        ParadigmInterface paradigmInterface2 = new ParadigmInterfaceService2();
        System.out.println(paradigmInterface2.getElement());

        /**
         * 范型方法
         */
        ParadigmMethod paradigmMethod = new ParadigmMethod();
        Integer result = paradigmMethod.getParam1(123);
        System.out.println(result);
        paradigmMethod.getParam2(456);
        paradigmMethod.getParam3(789);

        System.out.println("===========");

        /**
         * 范型类里面的范型方法和非范型方法
         */
        ParadigmClassMethod<Integer> paradigmClassMethod = new ParadigmClassMethod();
        Integer paradigmClassMethodResult1 = paradigmClassMethod.getParam1(1123);
        System.out.println(paradigmClassMethodResult1);
        Object paradigmClassMethodResult2 = paradigmClassMethod.getParam1Sub(1123);
        System.out.println(paradigmClassMethodResult2);
        paradigmClassMethod.getParam2(1456);
        paradigmClassMethod.getParam3(1789);

        paradigmClassMethod.getParam4(101112);
        paradigmClassMethod.getParam5(10111213);
        paradigmClassMethod.getParam6(1011121314);

        /**
         * 静态范型方法
         */
        ParadigmClassMethod.showStaticType(124);

    }
}
