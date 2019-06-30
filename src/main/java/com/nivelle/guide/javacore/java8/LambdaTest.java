package com.nivelle.guide.javacore.java8;

import com.nivelle.guide.springboot.pojo.User;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class LambdaTest {
    /**
     * 1. 场景: 能够接收Lambda表达式的参数类型，是一个只包含一个方法的接口。它是一个能够被传递的匿名函数。
     * <p>
     * 1.1: 在函数接口中使用lambda表达式，函数接口里包含一个抽象方法
     * <p>
     * 2. 结构体：(parameters) -> {expressio}
     * <p>
     * 3. lambda表达式的体提供了函数接口中单个抽象方法的实现
     */

    List<User> users = new ArrayList<>();

    {
        users = Arrays.asList(
                new User(50, "张三"),
                new User(18, "李四"),
                new User(20, "王五"),
                new User(17, "赵六"),
                new User(40, "田七")
        );
    }


    //年龄大于18岁的人(流)

    //@todo test
    public void adultUser() {

        users.stream().filter((e) -> e.getAge() >= 18).forEach(System.out::println);

    }

    //java8List原生支持sort()
    public void sortUser() {
        users.sort((x, y) -> Integer.compare(x.getAge(), y.getAge()));
        users.forEach(System.out::println);
    }


    public void ageList() {
        System.out.println(users.stream().map(x -> x.getAge()).collect(Collectors.toList()));
    }


    public void sumAge() {
        System.out.println("大伙总共" + users.stream().map(x -> x.getAge()).reduce(0, Integer::sum) + "岁");
    }

//    public void maxAge() {
//        Optional<User> userOP= users.stream().map(x -> x.getAge()).mapToInt(User::getAge).max();
//        userOP.get();
//
//    }
}
