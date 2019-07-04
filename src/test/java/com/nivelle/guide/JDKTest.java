package com.nivelle.guide;

import com.nivelle.guide.javacore.java8.FormulaService;
import com.nivelle.guide.javacore.java8.StreamTest;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JDKTest {


    @Qualifier(value = "streamtest")
    @Resource
    StreamTest streamTest;

    @Autowired
    private FormulaService formulaService;


    @Test
    public void testCounUser() {
        streamTest.countUser();
    }

    @Test
    public void testDisdinct() {
        streamTest.distinctUser();
    }

    @Test
    public void testMapAge() {
        streamTest.editAge();
    }


    @Test
    public void testListAge() {
        streamTest.ageList();
    }


    @Test
    public void testFlatUser() {
        streamTest.flatUser();
    }

    @Test
    public void testPeekUser() {
        streamTest.peekUser();
    }

    @Test
    public void testLimitUser() {
        streamTest.limitUser();
    }

    @Test
    public void testSkipUser() {
        streamTest.skipUser();
    }


    //list遍历
    @Test
    public void lambdaTest() {
        List<Integer> list = Lists.newArrayList();
        list.add(1);
        list.add(2);
        list.add(3);

        list.forEach(System.out::println);

        list.forEach(integer -> {
            System.out.println(integer * 3);
        });

    }

    //实现匿名内部类
    @Test
    public void testAnonymous() {
        new Thread(() -> System.out.println("java8,test")).start();
    }

    //Predicate 谓语
    @Test
    public void testPredicate() {

        List<String> languages = Arrays.asList("java", "scala", "php", "go");

        filter(languages, (str) -> ((String) str).length() > 4);

        languages.forEach(System.out::println);


    }

    //允许将对象进行转换
    @Test
    public void testMap() {
        List<Integer> list = Lists.newArrayList();
        list.add(4);
        list.add(5);
        list.add(6);

        list.stream().map((i) -> i * 3).forEach(System.out::println);

        list.forEach(i -> i = i * 3);

        list.forEach(System.out::println);
    }

    //折叠操作
    @Test
    public void testReduce() {
        List<Integer> list = Lists.newArrayList();
        list.add(7);
        list.add(8);
        list.add(9);
        Integer integer = list.stream().map((k) -> k = k * 3).reduce((sum, count) -> sum += count).get();
        System.out.println("==========" + integer);

    }

    @Test
    public void testConact() {
        String concat = Stream.of("A", "b", "c", "d").reduce("", String::concat);

        System.out.println(concat);
    }

    public static void filter(List<String> names, Predicate condition) {
        for (String name : names) {
            if (condition.test(name)) {
                System.out.println(name + " ");
            }
        }
    }

    @Test
    public void testALL() {
        List<Integer> nums = Lists.newArrayList(1, 1, null, 2, 3, 4, null, 5, 6, 7, 8, 9, 10);

        System.out.println("sum is:" + nums.stream().filter(num -> num != null).

                distinct().mapToInt(num -> num * 2).

                peek(System.out::println).skip(2).limit(4).sum());

    }

    @Test
    public void testInterface() {
        System.out.println(formulaService.sqrt(4));
        System.out.println(formulaService.calculate(4));//sout自动生成System.out.println()
    }

    @Test
    public void testAgeList() {
        streamTest.ageList();
    }

    public static void main(String[] args) {//psvm 自动生成main方法


    }


}
