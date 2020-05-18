package com.nivelle.base.jdk.java8;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * java8Method学习例子
 *
 * @author nivell
 * @date 2020/04/02
 */
public class Java8MethodTest {

    public static void main(String[] args) {
        List<String> names = Arrays.asList("peter", "anna", "mike", "xenia");

        Collections.sort(names, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return b.compareTo(a);
            }
        });

        System.out.println(names);
        List<String> names2 = Arrays.asList("peter", "anna", "mike", "xenia");
        names2.sort((x, y) -> x.compareTo(y));
        System.err.println(names2);

        FunctionInterface<String, String> convertMethodUp = (x) -> x.toUpperCase();
        String convertedUp = convertMethodUp.convert("nivelle");
        System.err.println(convertedUp);

        //静态方法
        FunctionInterface<String, Integer> convertMethod1 = Integer::valueOf;
        Integer converted2 = convertMethod1.convert("456");
        System.err.println(converted2);

        //::方法引用
        MethodFactoryImpl something = new MethodFactoryImpl();
        FunctionInterface<String, String> convertMethod2 = something::startsWith;
        String converted3 = convertMethod2.convert("Nivelle");
        System.out.println(converted3);


        FunctionInterface<String, Long> convertMethod3 = Long::valueOf;
        Long converted4 = convertMethod3.convert("23");
        System.out.println(converted4);

        //::调用构造方法
        FunctionMethodFactory<MethodFactoryImpl> functionMethodFactory = MethodFactoryImpl::new;
        MethodFactoryImpl functionMethodFactoryImpl = functionMethodFactory.create("nivelle", 18);
        System.out.println(functionMethodFactoryImpl);

        //Lambda 访问的局部变量隐式是final的
        //不能访问接口默认实现的方法
        /**
         * 默认接口实现
         */
        //1.判断 Predicates
        Predicate<String> predicate = (s) -> s.length() > 0;
        System.err.println(predicate.test("nivelle"));

        //2.函数 Functions
        //函数接收一个入参并返回一个结果。default方法被用于将多个功能函数链接在一起，（compose 之前执行、andThen 之后执行）
        Function<String, Integer> toInteger = Integer::valueOf;
        System.err.println("functions Integer valueOf :" + toInteger.apply("789"));
        Function<String, String> backToString = toInteger.andThen(String::valueOf);
        System.err.println("functions:" + backToString.apply("101112"));

        //3.生产 Suppliers
        Supplier<MethodFactoryImpl> personSupplier = MethodFactoryImpl::new;
        personSupplier.get();

        //4.消费 Consumers
        Consumer<MethodFactoryImpl> greeter = p -> System.out.println("Hello, " + p.getName());
        greeter.accept(new MethodFactoryImpl("nivelle", 18));

        //比较 Comparators
        Comparator<MethodFactoryImpl> comparator = (p1, p2) -> p1.getAge().compareTo(p2.getAge());
        MethodFactoryImpl p1 = new MethodFactoryImpl("jessy", 19);
        MethodFactoryImpl p2 = new MethodFactoryImpl("nivelle", 18);
        System.out.println(comparator.compare(p1, p2));
        System.out.println(comparator.reversed().compare(p1, p2));

        //防止空指针异常的Optional
        Optional<Integer> optional = Optional.of(123);
        System.out.println(optional.isPresent());
        System.out.println(optional.get());
        System.out.println(optional.orElse(new Integer(0)));
        //optional.ifPresent((s) -> System.out.println(s.charAt(0)));
    }
}
