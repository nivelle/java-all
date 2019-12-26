package com.nivelle.base.javacore.java8;

import com.google.common.collect.Lists;
import com.nivelle.base.pojo.User;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class Java8Demo {

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
        MethodTest something = new MethodTest();
        FunctionInterface<String, String> convertMethod2 = something::startsWith;
        String converted3 = convertMethod2.convert("Nivelle");
        System.out.println(converted3);


        FunctionInterface<String, Long> convertMethod3 = Long::valueOf;
        Long converted4 = convertMethod3.convert("23");
        System.out.println(converted4);

        // ::调用构造方法
        MethodTestFactory<MethodTest> methodTestFactory = MethodTest::new;
        MethodTest methodTest = methodTestFactory.create("nivelle", 18);
        System.out.println(methodTest);

        //Lambda 访问的局部变量隐式是final的
        //不能访问接口默认实现的方法


        //默认接口实现

        //1.判断 Predicates
        Predicate<String> predicate = (s) -> s.length() > 0;

        System.out.println(predicate.test("nivelle"));

        //2.函数 Functions
        //函数接收一个入参并返回一个结果。default方法被用于将多个功能函数链接在一起，（compose 之前执行、andThen 之后执行）
        Function<String, Integer> toInteger = Integer::valueOf;
        System.out.println(toInteger.apply("789"));
        Function<String, String> backToString = toInteger.andThen(String::valueOf);
        System.out.println(backToString.apply("101112"));

        //3.生产 Suppliers
        Supplier<MethodTest> personSupplier = MethodTest::new;
        personSupplier.get();

        //4.消费 Consumers
        Consumer<MethodTest> greeter = (p) -> System.out.println("Hello, " + p.getName());
        greeter.accept(new MethodTest("nivelle", 18));

        //比较 Comparators

        Comparator<MethodTest> comparator = (p1, p2) -> p1.getAge().compareTo(p2.getAge());

        MethodTest p1 = new MethodTest("jessy", 19);
        MethodTest p2 = new MethodTest("nivelle", 18);

        System.out.println(comparator.compare(p1, p2));
        System.out.println(comparator.reversed().compare(p1, p2));

        //防止空指针异常的Optional
        Optional<Integer> optional = Optional.of(123);
        System.out.println(optional.isPresent());
        System.out.println(optional.get());
        System.out.println(optional.orElse(new Integer(0)));
        //optional.ifPresent((s) -> System.out.println(s.charAt(0)));

        //Stream

        List<String> stringCollection = new ArrayList<>();
        stringCollection.add("ddd2");
        stringCollection.add("aaa2");
        stringCollection.add("bbb1");
        stringCollection.add("aaa1");
        stringCollection.add("bbb3");
        stringCollection.add("ccc");
        stringCollection.add("bbb2");
        stringCollection.add("bbb2");

        //filter
        stringCollection
                .stream()
                .filter((s) -> s.startsWith("a"))
                .forEach(System.out::println);

        //sorted
        stringCollection.stream().sorted((x, y) -> x.compareTo(y)).filter(s -> s.startsWith("b")).forEach(System.out::println);

        //map:map是一种中间过程操作，借助函数表达式将元素转换成另一种形式。可以使用map将每个对象转换为另一种类型。最终输出的结果类型依赖于你传入的函数表达式。
        stringCollection.stream().map(String::toUpperCase).sorted((x, y) -> x.compareTo(y)).forEach(System.out::println);

        System.err.println("映射然后去重复："+stringCollection.stream().map(String::toLowerCase).distinct().collect(Collectors.toList()));

        //Match 匹配 //任意一个元素满足
        boolean anyStartsWithA =
                stringCollection
                        .stream()
                        .anyMatch((s) -> s.startsWith("a"));

        System.out.println(anyStartsWithA);
        //所有元素满足
        boolean allStartsWithA =
                stringCollection
                        .stream()
                        .allMatch((s) -> s.startsWith("b"));

        System.out.println(allStartsWithA);
        //没有元素满足
        boolean noneStartsWithZ =
                stringCollection
                        .stream()
                        .noneMatch((s) -> s.startsWith("z"));

        System.out.println(noneStartsWithZ);

        //计数
        Long cout = stringCollection.stream().count();
        System.out.println(cout);

        //reduce 减少，终止型操作，通过给定的函数表达式来处理流中的前后两个元素、或者中间结果与下一个元素。并将最终返回的结果放入Optional
        Optional<String> reduced =
                stringCollection
                        .stream()
                        .sorted()
                        .reduce((s1, s2) -> s1 + "//" + s2);

        reduced.ifPresent(System.out::println);


        User user1 = new User(1, "nivelle");
        User user2 = new User(2, "nivelle2");
        User user3 = new User(2, "jessy");
        User user4 = new User(2, "jessy2");

        List userList = Lists.newArrayList();
        userList.add(user1);
        userList.add(user2);
        userList.add(user3);
        userList.add(user4);



        System.err.println("list转Map:" + userList.stream().collect(Collectors.toMap(User::getAge, a -> a, (k1, k2) -> k1)));

        System.err.println("根据age排序:" + userList.stream().sorted(Comparator.comparing(User::getAge)).collect(Collectors.toList()));

        System.err.println("根据age分组然:" + userList.stream().collect(Collectors.groupingBy(User::getAge)));

        //userList.stream().filter(x->x.getAge>1).toArray(User::new);

        // 时钟
        // 时钟提供了对当前日期和时间的访问。时钟知晓时区，可以用来代替System.currentTimeMillis()来检索自Unix EPOCH以来的当前时间（以毫秒为单位）。在时间轴上的某一时刻用Instant表示。Instant可以创建遗留的java.util.Date 对象。
        Clock clock = Clock.systemDefaultZone();
        long millis = clock.millis();
        System.out.println(millis);

        Instant instant = clock.instant();
        Date nowDate = Date.from(instant);
        System.out.println(nowDate);


        //时区:时区是通过 ZoneId来表示，它提供了很多静态方法。时区定义了在瞬间和本地日期和时间之间转换的重要偏移。
        //System.out.println(ZoneId.getAvailableZoneIds());

        System.out.print(ZoneId.of("Asia/Shanghai"));


        //本地时间:LocalTime表示没有时区的时间，如晚上10点 或者 17:30:15。
        ZoneId zone1 = ZoneId.of("Europe/Berlin");
        ZoneId zone2 = ZoneId.of("Asia/Shanghai");

        LocalTime now1 = LocalTime.now(zone1);
        LocalTime now2 = LocalTime.now(zone2);

        System.out.println(now1.isBefore(now2));
        long minutesBetween = ChronoUnit.HOURS.between(now1, now2);
        System.out.println(minutesBetween);


        LocalTime late = LocalTime.of(23, 59, 59);
        System.out.println(late);

        //格式化日期时间
        DateTimeFormatter germanFormatter = DateTimeFormatter.ofPattern("HHmm");

        LocalTime leetTime = LocalTime.parse("1237", germanFormatter);
        System.out.println(leetTime);

        //本地日期

        LocalTime time = LocalTime.of(23, 59, 59);
        System.out.println(time);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
        LocalDate yesterday = tomorrow.minusDays(2);
        System.out.println(today);
        System.out.println(tomorrow);
        System.out.println(yesterday);


    }


}
