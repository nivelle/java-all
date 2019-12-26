package com.nivelle.base.javacore.java8;

import com.google.common.collect.Lists;
import com.nivelle.base.pojo.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 1. Stream是元素的集合
 * <p>
 * 2. 支持顺序和并行的对原Stream进行汇聚的操作
 * <p>
 * 3. 创建Stream(1. of()；2. generator()//无限长度的stream,和limit配合使用；3. iterate()//无限长度，元素生产都是重复对给定的种子值调用用户指定函数老生成的)； 4. collection子类获取
 */

@Component(value = "streamtest")
public class StreamTest {


    List<User> users = new ArrayList<>();
    List<User> users2 = new ArrayList<>();


    {
        users = Arrays.asList(
                new User(50, "张三"),
                new User(18, "李四"),
                new User(20, "王五"),
                new User(17, "赵六"),
                new User(40, "田七"),
                new User(50, "张三")
        );

        users2 = Arrays.asList(
                new User(2, "小张三"),
                new User(3, "小李四"),
                new User(4, "小王五"),
                new User(5, "小赵六"),
                new User(6, "小田七"),
                new User(7, "小张三2")
        );
    }

    //对于Stream中包含的元素进行去重操作（去重逻辑依赖元素的equals方法），新生成的Stream中没有重复的元素
    public void distinctUser() {

        users.stream().distinct().forEach(System.out::println);

    }

    //对于Stream中包含的元素使用给定的过滤函数进行过滤操作，新生成的Stream只包含符合条件的元素；
    public void countUser() {
        Stream userStream = users.stream().filter(x -> (x.getAge() > 18));

        userStream.forEach(System.out::println);
    }

    // 对于Stream中包含的元素使用给定的转换函数进行转换操作，**新生成的Stream只包含转换生成的元素**。
    public void editAge() {
        Stream userStream = users.stream().map(x -> x.getAge() + 1);

        Stream userStream2 = users.stream().map(x -> x.getName().replace("三", "全蛋"));

        userStream2.forEach(System.out::println);

        //产生操作的数据源的流
        userStream.forEach(System.out::println);
        //不会改变数据源
        users.forEach(System.out::println);
    }

    public void flatUser() {

        List<List<User>> allUser = Lists.newArrayList();
        allUser.add(users);
        allUser.add(users2);

        //两个list合并在一个list里面展示
        List<User> users = allUser.stream().flatMap(x -> x.stream()).collect(Collectors.toList());

        users.forEach(System.out::println);

    }

    //peek是个中间操作，它提供了一种对流中所有元素操作的方法，而不会把这个流消费掉
    //peek：返回的流与原始流相同。当原始流中的元素被消费时，会首先调用 peek 方法中指定的 Consumer 实现对元素进行处理。
    public void peekUser() {

        users2.stream().peek(x -> x.setAge(3)).forEach(System.out::println);

    }

    public void limitUser() {
        Stream userStream = users.stream().filter(x -> x.getAge() > 20).limit(2);

        userStream.forEach(System.out::println);
    }

    public void skipUser() {
        Stream skipStream = users.stream().skip(3);
        skipStream.forEach(System.out::println);

    }

    public void ageList() {
        System.out.println(users.stream().map(x -> x.getAge()).collect(Collectors.toList()));
    }


}
