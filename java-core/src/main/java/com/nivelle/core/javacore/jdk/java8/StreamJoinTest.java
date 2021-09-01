package com.nivelle.core.javacore.jdk.java8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/07/02
 */
public class StreamJoinTest {

    public static void main(String[] args) {
        List<Person> personList = new ArrayList<Person>();
        personList.add(new Person("Tom", 8900, 23, "male", "New York"));
        personList.add(new Person("Jack", 7000, 25, "male", "Washington"));
        personList.add(new Person("Lily", 7800, 21, "female", "Washington"));
        String names = personList.stream().map(Person::getName).collect(Collectors.joining(","));
        System.out.println("所有员工的姓名：" + names);
        List<String> list = Arrays.asList("A", "B", "C");
        String string = list.stream().collect(Collectors.joining("-"));
        System.out.println("拼接后的字符串：" + string);
    }

}
