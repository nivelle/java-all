package com.nivelle.base.javacore;

import com.nivelle.base.pojo.Parent;
import com.nivelle.base.pojo.Son;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 类型转换bug
 *
 * @author fuxinzhong
 * @date 2020/05/11
 */
public class ArrayAndListClass {

    public static void main(String[] args) {
        testArray();
        System.out.println("========");
        testArrayToList();
        System.out.println("========");
        testListToArray();

        System.err.println((Object) Object[].class);
    }

    public static void testArray() {
        Son[] sonArray = {new Son(1, "nivelle", 130), new Son(2, "jessy", 140)};
        System.err.println("son数组类型：" + sonArray.getClass());

        Parent[] parentArray = sonArray;
        System.err.println("parent数组类型:" + parentArray.getClass());

        System.out.println("父类数组指向子类数组实现,实际类型还是子类的类型");

        try {
            System.out.println("父类型不能向下转型放到子类型数组");
            //sonArray[0] = new Parent("nivelleFather");
        } catch (Exception e) {
            System.out.println(e);
        }
        Parent[] parentArrayNew = {new Parent("nivelle"), new Parent("jessy")};

        parentArrayNew[0] = new Son(1, "nivelle2", 130);

        System.out.println("子类向上转型parentArrayNew:" + parentArrayNew[0]);

        List<Son> sonList = Arrays.asList(sonArray);
        System.out.println("Arrays asList 设置Arrays.list底层 数组为入参数组类型也就是用范行来表示数组元素类型:" + sonList.getClass());
        /**
         * 返回底层数组的copy,类型保持
         */
        Object[] sonArray2 = sonList.toArray();
        System.out.println("Arrays.list toArray类型返回的是底层数组的实际类型:" + sonArray2.getClass());


    }

    public static void testArrayToList() {
        String[] array = {"abc", "def"};
        List<String> list = Arrays.asList(array);
        System.err.println("Arrays内部的list类型:" + list.getClass());
        /**
         * ## Arrays.ArrayList 内部的list底层是个范性数组
         * public Object[] toArray() {
         *     return a.clone();//ArrayList=>private final E[] a; => private static class ArrayList<E> extends AbstractList<E>
         * }
         */
        Object[] objArray = list.toArray();
        System.err.println("list.toArray 方法返回底层数组的实际类型:" + objArray.getClass());

        try {
            objArray[0] = new Object();
            System.out.println("数组只能接受向上转型不能接受向下转型:" + objArray[0]);
        } catch (ArrayStoreException e) {
            System.out.println(e);
        }
    }

    public static void testListToArray() {
        List<Son> dataList = new ArrayList();
        dataList.add(new Son(1, "1", 1));
        dataList.add(new Son(2, "2", 2));
        /**
         * public Object[] toArray() {
         *         //java.util.ArrayList 底层是个Object[]数组
         *         return Arrays.copyOf(elementData, size);
         *     }
         */
        Object[] listToArray = dataList.toArray();
        System.err.println("list to Array:" + listToArray.getClass());
        listToArray[0] = new Son(3, "3", 3);
        listToArray[1] = new Parent("4");

        System.out.println("listToArray[0] is:" + listToArray[0]);
        System.out.println("listToArray[1] is:" + listToArray[1]);

    }


}
