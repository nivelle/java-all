package com.nivelle.base.jdk.java8;

import java.util.*;
import java.util.function.*;

/**
 * java8Method学习例子
 *
 * @author nivelle
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
        System.out.println(names2);

        FunctionInterface<String, String> convertMethodUp = (x) -> x.toUpperCase();
        String convertedUp = convertMethodUp.convert("nivelle");
        System.out.println(convertedUp);

        //静态方法
        FunctionInterface<String, Integer> convertMethod1 = Integer::valueOf;
        Integer converted2 = convertMethod1.convert("456");
        System.out.println(converted2);

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
        //1.Predicate 接口
        Predicate<String> predicate1 = (s) -> s.length() > 0;
        Predicate<String> predicate2 = (s) -> s.startsWith("a");
        Predicate<String> predicate3 = (s) -> s.contains("b");
        System.out.println("Predicate and :" + predicate1.and(predicate2).test("nivelle"));
        System.out.println("Predicate and :" + predicate1.and(predicate3).test("nivelleb"));
        System.out.println("Predicate or :" + predicate1.or(predicate2).test("nivelle"));
        System.out.println("Predicate negate :" + predicate3.negate().test("nivelle"));

        //2.Function 接口
        //函数接收一个入参并返回一个结果。default方法被用于将多个功能函数链接在一起，（compose 之前执行、andThen 之后执行）
        Function<Integer, Integer> functionApply = s -> s + 1; //apply执行
        Function<Integer, Integer> functionCompose = s -> s * 2;//before 执行
        Function<Integer, Long> andThen = Long::valueOf;//apply之后执行
        System.out.println("function compose->apply->andThen:" + functionApply.compose(functionCompose).andThen(andThen).apply(2));

        //3.生产 Suppliers
        Supplier<MethodFactoryImpl> personSupplier = MethodFactoryImpl::new;
        System.out.println("supply接口返回创建:" + personSupplier.get());

        //4.Consumer接口 的功能就是=右边的函数
        Consumer<MethodFactoryImpl> greeter1 = p -> System.out.println("首先执行:Hello, " + p.getName());
        Consumer<MethodFactoryImpl> greeter2 = p -> System.out.println("然后:执行完greeter功能后又执行andThen功能：" + p.getName().toUpperCase());
        Consumer<MethodFactoryImpl> greeter3 = p -> System.out.println("最后:Hello3, " + p.getName() + "你都" + p.getAge() + "岁了");
        greeter1.andThen(greeter2).andThen(greeter3).accept(new MethodFactoryImpl("jessy", 20));

        //5. Comparator接口
        Comparator<MethodFactoryImpl> comparator = (p1, p2) -> p1.getAge().compareTo(p2.getAge());
        MethodFactoryImpl p1 = new MethodFactoryImpl("jessy", 19);
        MethodFactoryImpl p2 = new MethodFactoryImpl("nivelle", 18);
        System.out.println("comparator比较器:" + comparator.compare(p1, p2));
        System.out.println("comparator比较器反转:" + comparator.reversed().compare(p1, p2));


        //防止空指针异常的Optional
        Optional<Integer> optional = Optional.of(123);
        System.out.println("isPresent:" + optional.isPresent());
        System.out.println("get:" + optional.get());
        System.out.println("orElse:" + optional.orElse(new Integer(0)));
        //optional.ifPresent((s) -> System.out.println(s.charAt(0)));
        Optional<Integer> optional1 = Optional.empty();
        System.out.println("orElseGet:" + optional1.orElseGet(() -> {
            return 100;
        }));
        //Value不为空则返回Value，否则返回传入的Supplier生成的值；
        System.out.println("ofNullAble:" + Optional.ofNullable(99).orElseGet(() -> {
            return 20;
        }));
        optional1.ifPresent(System.out::print);

        Optional<Integer> optional2 = Optional.of(456);
        System.out.println(optional2.orElseGet(() -> {
            return 87;
        }));


        Optional<Integer> optional3 = Optional.ofNullable(null);
        optional3.ifPresent(System.out::print);

        //BiFunction
        BiFunction<Integer, Double, Long> biFunction = (Integer t, Double u) -> {
            return new Double((int) t + u).longValue();
        };
        Long result = biFunction.apply(new Integer(1), new Double(1.0D));
        System.out.println("BiFunction after result:" + result);

        //BinaryOperator
        BinaryOperator<Integer> binaryOperator = (Integer a, Integer b) -> {
            return a + b;
        };
        Integer binaryOperatorResult = binaryOperator.apply(2, 4);
        System.out.println(binaryOperatorResult);

        //BiConsumer
        BiConsumer<Integer, Double> consumer = (Integer t, Double u) -> {
            System.out.println("biConsumer after:" + (t + u.intValue()));
        };
        consumer.accept(1, 3.0);

        //reduce:根据一定的规则将Stream中的元素进行计算后返回一个唯一的值

    }
}
