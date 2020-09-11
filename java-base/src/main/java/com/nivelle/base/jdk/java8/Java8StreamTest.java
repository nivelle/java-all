package com.nivelle.base.jdk.java8;

import com.google.common.collect.Lists;
import com.nivelle.base.pojo.Bar;
import com.nivelle.base.pojo.Foo;
import com.nivelle.base.pojo.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * java8流学习例子
 *
 * @author nivellefu
 */
public class Java8StreamTest {
    static List<User> users;
    static List<User> users2;

    /**
     * 中间操作：
     * sequential:返回一个相等的串行的Stream对象，如果原Stream对象已经是串行就可能会返回原对象
     * parallel:返回一个相等的并行的Stream对象，如果原Stream对象已经是并行的就会返回原对象
     * unordered:返回一个不关心顺序的Stream对象，如果原对象已经是这类型的对象就会返回原对象
     * onClose:返回一个相等的Steam对象，同时新的Stream对象在执行Close方法时会调用传入的Runnable对象
     * close:关闭Stream对象
     * filter:元素过滤：对Stream对象按指定的Predicate进行过滤，返回的Stream对象中仅包含未被过滤的元素
     * map:元素一对一转换：使用传入的Function对象对Stream中的所有元素进行处理，返回的Stream对象中的元素为原元素处理后的结果
     * mapToInt:元素一对一转换：将原Stream中的使用传入的IntFunction加工后返回一个IntStream对象
     * flatMap:元素一对多转换：对原Stream中的所有元素进行操作，每个元素会有一个或者多个结果，然后将返回的所有元素组合成一个统一的Stream并返回；
     * distinct:去重：返回一个去重后的Stream对象
     * sorted:排序：返回排序后的Stream对象
     * peek:使用传入的Consumer对象对所有元素进行消费后，返回一个新的包含所有原来元素的Stream对象
     * limit:获取有限个元素组成新的Stream对象返回
     * skip:抛弃前指定个元素后使用剩下的元素组成新的Stream返回
     * takeWhile:如果Stream是有序的（Ordered），那么返回最长命中序列（符合传入的Predicate的最长命中序列）组成的Stream；如果是无序的，那么返回的是所有符合传入的Predicate的元素序列组成的Stream。
     * dropWhile:与takeWhile相反，如果是有序的，返回除最长命中序列外的所有元素组成的Stream；如果是无序的，返回所有未命中的元素组成的Stream。
     *
     * 终结操作：
     * iterator:返回Stream中所有对象的迭代器;
     * spliterator:返回对所有对象进行的spliterator对象
     * forEach:对所有元素进行迭代处理，无返回值
     * forEachOrdered:按Stream的Encounter所决定的序列进行迭代处理，无返回值
     * toArray:回所有元素的数组
     * reduce:使用一个初始化的值，与Stream中的元素一一做传入的二合运算后返回最终的值。每与一个元素做运算后的结果，再与下一个元素做运算。它不保证会按序列执行整个过程。
     * collect:根据传入参数做相关汇聚计算
     * min:返回所有元素中最小值的Optional对象；如果Stream中无任何元素，那么返回的Optional对象为Empty
     * max:与Min相反
     * count:所有元素个数
     * anyMatch:只要其中有一个元素满足传入的Predicate时返回True，否则返回False
     * allMatch:所有元素均满足传入的Predicate时返回True，否则False
     * noneMatch:所有元素均不满足传入的Predicate时返回True，否则False
     * findFirst:返回第一个元素的Optioanl对象;如果无元素返回的是空的Optional;如果Stream是无序的,那么任何元素都可能被返回。
     * findAny:返回任意一个元素的Optional对象，如果无元素返回的是空的Optioanl。
     * isParallel:判断是否当前Stream对象是并行的
     */
    static {
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

    /**
     * 中间操作（Intermediate operations）和终结操作（Terminal operations）。
     * 中间操作只对操作进行了记录,即只会返回一个流,不会进行计算操作,而终结操作是实现了计算操作;
     * 中间操作又可以分为无状态（Stateless）与有状态（Stateful）操作,前者是指元素的处理不受之前元素的影响,后者是指该操作只有拿到所有元素之后才能继续下去
     *
     * @param args
     */
    public static void main(String[] args) {

        Stream.of("a1", "a2", "a3").filter(x -> x.equals("a2")).findFirst().ifPresent(System.out::print);
        System.out.println();
        /**
         * 基础类型流支持一些特殊用法
         */
        IntStream.range(1, 10).forEach(System.out::print);
        System.out.println();
        System.out.println("求平均值之前先一对一处理➕2-> start ===");
        Arrays.stream(new int[]{100, 100, 100}).map(n -> n + 2).average().ifPresent(System.out::print);
        System.out.println();
        System.out.println("end ===");
        /**
         * 基础数据和对象数据的转换
         */
        IntStream.range(1, 4).forEach(System.out::print);//range范围是[n,m)
        System.out.println();
        IntStream.range(1, 4).mapToObj(i -> "a+" + i).forEach(System.out::print);

        System.out.println();

        Stream.of(1.0, 2.0, 3.0).mapToInt(Double::intValue).mapToObj(i -> "a-" + i).forEach(System.out::print);
        System.out.println();
        //Stream
        /**
         * Laziness（延迟加载）是中间操作（intermediate operations）的一个重要特性。
         * 如下面这个例子：中间操作（terminal operation）缺失，当执行这个代码片段的时候，并不会在控制台打印相应的内容，这是因为只有最终操作（terminal operation）存在的时候，
         * 中间操作（intermediate operations）才会执行。
         */
        Stream.of("d2", "a2", "b1", "b3", "c").filter(s -> {
            System.out.print("filter: " + s);
            return true;
        });
        /**
         * 每一个元素沿着链垂直移动，第一个字符串"d2"执行完filter和forEach后第二个元素"a2"才开始执行
         */
        Stream.of("d2", "a2", "b1", "b3", "c").filter(s -> {
            System.out.print("每一个元素沿着链垂直移动:filter: " + s + "-->");
            return true;
        }).forEach(s -> System.out.println("每一个元素沿着链垂直移动:forEach: " + s + ";"));


        System.out.println();
        boolean result = Stream.of("d2", "a2", "f1", "b3", "c")
                .map(s -> {
                    System.out.print("anyMatch map: " + s);
                    return s.toUpperCase();
                })
                .anyMatch(s -> {
                    System.out.println("anyMatch是一个短路运算: " + s);
                    return s.startsWith("A");
                });
        System.out.println(result);
        System.out.println();
        /**
         * 执行效率与Stream执行链顺序的关系,可以将 filter 提前大幅提高效率
         */
        Stream.of("d2", "a2", "b1", "b3", "c")
                .map(s -> {
                    //System.out.print("map: " + s);
                    return s.toUpperCase();
                })
                .filter(s -> {
                    // System.out.print("filter: " + s);
                    return s.startsWith("A");
                })
                .forEach(s -> System.out.print("过滤展示以A开头的字母: " + s));
        System.out.println();

        /**
         * Sorting 是一种特殊的中间操作（intermediate operation），在对集合中元
         * 素进行排序过程中需要保存元素的状态，因此Sorting 是一种有状态的操作（stateful operation)
         */
        Stream.of("d2", "a2", "b1", "b3", "c")
                //先两两比较，比较完毕后再挨个元素进行filter和map
                .sorted((s1, s2) -> {
                    System.out.printf("中间有状态操作,需要先执行完获取状态,然后再链式操作, sorted: %s-%s=%s\n", s1, s2, (s1.compareTo(s2)));
                    return s1.compareTo(s2);
                })
                .filter(s -> {
                    System.out.print("filter: " + s + "->");
                    return s.startsWith("a");
                })
                .map(s -> {
                    System.out.print("map: " + s + "->");
                    return s.toUpperCase();
                })
                .forEach(s -> System.out.print("forEach: " + s + "\n"));
        /**
         * Java 8 streams不能被复用，当你执行完任何一个最终操作（terminal operation）的时候流就被关闭了。
         */
        Stream<String> stream = Stream.of("d2", "a2", "b1", "b3", "c").filter(s -> s.startsWith("a"));
        stream.anyMatch(s -> true);
        System.out.println();
        try {
            stream.forEach(System.out::println);
        } catch (Exception e) {
            //java.lang.IllegalStateException: stream has already been operated upon or closed
            System.out.println(e.getMessage());
        }
        /**
         *  为每个最终操作（terminal operation）创建一个新的stream链的方式来解决上面的重用问题,
         *  Stream api中已经提供了一个stream supplier类来在已经存在的中间操作（intermediate operations ）的stream基础上构建一个新的stream。
         */
        Supplier<Stream<String>> streamSupplier = () -> Stream.of("d2", "a2", "b1", "b3", "c").filter(s -> s.startsWith("a"));
        streamSupplier.get().anyMatch(s -> true);
        streamSupplier.get().noneMatch(s -> true);

        System.out.println();

        List<String> stringCollection = new ArrayList<>();
        stringCollection.add("ddd2");
        stringCollection.add("aaa2");
        stringCollection.add("bbb1");
        stringCollection.add("aaa1");
        stringCollection.add("bbb3");
        stringCollection.add("ccc");
        stringCollection.add("bbb2");
        stringCollection.add("bbb2");
        /**
         * 重复使用的流获取器
         */
        Supplier<Stream<String>> stringCollectionStream = () -> stringCollection.stream();
        //filter
        stringCollectionStream.get().filter((s) -> s.startsWith("a")).forEach(System.out::print);
        //sorted
        stringCollectionStream.get().filter(s -> s.startsWith("b")).sorted().forEach(System.out::print);
        //map:map是一种中间过程操作，借助函数表达式将元素转换成另一种形式。可以使用map将每个对象转换为另一种类型。最终输出的结果类型依赖于你传入的函数表达式。
        System.out.println();
        stringCollectionStream.get().map(String::toUpperCase).map(s -> {
            return s + "sufix";
        }).sorted().forEach(System.out::print);
        System.out.println();
        System.out.print("映射然后去重复:" + stringCollectionStream.get().map(String::toLowerCase).distinct().collect(Collectors.toList()));
        System.out.println();
        //Match 匹配 //任意一个元素满足
        boolean anyStartsWithA = stringCollectionStream.get().anyMatch((s) -> s.startsWith("a"));
        System.out.print("任意一个匹配到a:" + anyStartsWithA);
        System.out.println();

        //所有元素满足
        boolean allStartsWithA = stringCollectionStream.get().allMatch((s) -> s.startsWith("b"));
        System.out.print("全部匹配到b:" + allStartsWithA);
        System.out.println();

        //没有元素满足
        boolean noneStartsWithZ = stringCollectionStream.get().noneMatch((s) -> s.startsWith("z"));
        System.out.print("没有元素匹配z:" + noneStartsWithZ);
        System.out.println();

        //计数
        Long count = stringCollection.stream().count();
        System.out.print("元素个数:" + count);

        System.out.println();
        //todo
        stringCollection.stream().map(s -> {
            return s + s;
        }).peek(System.out::println).forEach(System.out::println);

        System.out.println();
        //每个元素经过处理后生成一个多个元素的Stream对象，然后将返回的所有Stream对象中的所有元素组合成一个统一的Stream并返回
        stringCollection.stream().flatMap(s -> Stream.of(s.split(""))).forEach(System.out::println);

        System.out.println();
        Stream<String> s = Stream.of("test", "t1", "t2", "teeeee", "aaaa", "taaa");
        // Java9 : 如果Stream是有序的（Ordered），那么返回最长命中序列（符合传入的Predicate的最长命中序列）组成的Stream；如果是无序的，那么返回的是所有符合传入的Predicate的元素序列组成的Stream。
        //s.takeWhile(n -> n.contains("t")).forEach(System.out::println);
        //java9: dropWhile 与takeWhile相反，如果是有序的，返回除最长命中序列外的所有元素组成的Stream；如果是无序的，返回所有未命中的元素组成的Stream


        System.out.println("user start");

        User user1 = new User(1, "nivelle");
        User user2 = new User(2, "nivelle2");
        User user3 = new User(2, "jessy");
        User user4 = new User(100, "jessy2");

        List<User> userList = Lists.newArrayList();
        userList.add(user1);
        userList.add(user2);
        userList.add(user3);
        userList.add(user4);
        /**
         * reduce: 根据一定的规则将Stream中的元素进行计算后返回一个唯一的值。它有三个变种，输入参数分别是一个参数、二个参数以及三个参数；
         *
         * 1. Optional<T> reduce(BinaryOperator<T> accumulator)//二合运算
         * 2. T reduce(T identity, BinaryOperator<T> accumulator)//初始化值,二合运算
         * 3. <U> U reduce(U identity,BiFunction<U, ? super T, U> accumulator,BinaryOperator<U> combiner)
         *
         *    - identity: 一个初始化的值；这个初始化的值其类型是泛型U，与Reduce方法返回的类型一致
         *
         *    - accumulator:其类型是BiFunction，输入是U与T两个类型的数据，而返回的是U类型；也就是说返回的类型与输入的第一个参数类型是一样的，而输入的第二个参数类型与Stream中元素类型是一样的。
         *
         *    - combiner: 其类型是BinaryOperator，支持的是对U类型的对象进行操作；参数combiner主要是使用在并行计算的场景下；如果Stream是非并行时，第三个参数实际上是不生效的。
         * */
        userList.stream().reduce((p1, p2) -> p1.age > p2.age ? p1 : p2).ifPresent(System.out::print);

        //通过给定的函数表达式来处理流中的前后两个元素、或者中间结果与下一个元素。并将最终返回的结果放入Optional
        userList.stream().reduce((s1, s2) -> new User(s1.age, s1.name + "//" + s2.name)).ifPresent(System.out::print);

        Integer ageSum = userList.stream().reduce(
                0, (sum, p) -> {
                    System.out.format("accumulator: sum=%s; person=%s\n", sum, p);
                    return sum += p.age;
                },
                (sum1, sum2) -> {
                    System.out.format("combiner: sum1=%s; sum2=%s\n", sum1, sum2);
                    return sum1 + sum2;
                });

        System.out.print("累加ageSum" + ageSum);

        Integer ageSum1 = userList
                .parallelStream()
                .reduce(0,
                        (sum, p) -> {
                            System.out.format("accumulator: sum=%s; person=%s\n", sum, p);
                            return sum += p.age;
                        },
                        (sum1, sum2) -> {
                            System.out.format("combiner: sum1=%s; sum2=%s\n", sum1, sum2);
                            return sum1 + sum2;
                        });
        System.out.print("累加 ageSum1" + ageSum1);


        /**
         *
         * Collect（收集）是一种是十分有用的最终操作,它可以把stream中的元素转换成另外一种形式,比如:list，set，map。Collect使用Collector作为参数，
         *
         * Collector包含四种不同的操作：supplier（初始构造器）, accumulator（累加器）, combiner（组合器）， finisher（终结者）
         *
         */
        System.out.print("list转Map:" + userList.stream().collect(Collectors.toMap(User::getAge, a -> a, (k1, k2) -> k1)));
        System.out.print("根据age排序:" + userList.stream().sorted(Comparator.comparing(User::getAge)).collect(Collectors.toList()));
        System.out.print("根据age分组然:" + userList.stream().collect(Collectors.groupingBy(User::getAge)));

        List<String> lines = Arrays.asList(new String[]{
                "hello abc", "老马 编程"
        });
        List<String> words = lines.stream().flatMap(line -> Arrays.stream(line.split("\\s+"))).collect(Collectors.toList());
        System.out.print(words);

        Stream skipStream = users.stream().skip(3);
        skipStream.forEach(System.out::print);

        Stream userStream = users.stream().filter(x -> x.getAge() > 20).limit(2);
        userStream.forEach(System.out::print);

        /**
         * peek是个中间操作，它提供了一种对流中所有元素操作的方法，而不会把这个流消费掉
         * peek：返回的流与原始流相同。当原始流中的元素被消费时，会首先调用 peek 方法中指定的 Consumer 实现对元素进行处理。
         */
        users2.stream().peek(x -> x.setAge(3)).forEach(System.out::print);

        List<List<User>> allUser = Lists.newArrayList();
        allUser.add(users);
        allUser.add(users2);

        //两个list合并在一个list里面展示
        List<User> users = allUser.stream().flatMap(x -> x.stream()).collect(Collectors.toList());

        users.forEach(System.out::print);
        editAge();
        /**
         * 对于Stream中包含的元素进行去重操作（去重逻辑依赖元素的equals方法），新生成的Stream中没有重复的元素
         */
        users.stream().distinct().forEach(System.out::print);

        /**
         * 对于Stream中包含的元素使用给定的过滤函数进行过滤操作，新生成的Stream只包含符合条件的元素；
         */
        Stream userStream2 = users.stream().filter(x -> (x.getAge() > 18));
        userStream2.forEach(System.out::print);

        List<User> persons =
                Arrays.asList(
                        new User(18, "Max"),
                        new User(23, "Peter"),
                        new User(23, "Pamela"),
                        new User(12, "David"));

        Map<Integer, List<User>> personsByAge = persons.stream().collect(Collectors.groupingBy(p -> p.getAge()));

        personsByAge.forEach((age, p) -> System.out.format("age %s: %s\n", age, p));

        Double averageAge = persons.stream().collect(Collectors.averagingInt(p -> p.getAge()));

        System.out.print(averageAge);

        IntSummaryStatistics ageSummary = persons.stream().collect(Collectors.summarizingInt(p -> p.getAge()));

        System.out.print(ageSummary);

        System.out.println();

        System.out.println("===");
        String phrase = persons
                .stream()
                .filter(p -> p.getAge() >= 18)
                .map(p -> p.getName())
                .collect(Collectors.joining(" and ", "In Germany ", " are of legal age."));

        System.out.print("join 拼接之后的值：" + phrase);

        System.out.println();

        Map<Integer, String> map = persons.stream().collect(Collectors.toMap(p -> p.getAge(), p -> p.getName(), (name1, name2) -> name1 + ";" + name2));

        System.out.print(map);

        /**
         * 通过map方法可以将stream中的一种对象转换成另外一种对象。但是map方法还是有使用场景限制，只能将一种对象映射为另外一种特定的已经存在的对象。
         * 是否能够将一个对象映射为多种对象，或者映射成一个根本不存在的对象呢。这就是flatMap方法出现的目的。
         */
        List<Foo> fooList = new ArrayList<>();
        IntStream.range(1, 4).forEach(i -> fooList.add(new Foo("Foo" + i)));

        fooList.forEach(f -> IntStream.range(1, 4).forEach(i -> f.bars.add(new Bar("Bar" + i + " <- " + f.name))));
        fooList.stream().flatMap(f -> f.bars.stream()).forEach(b -> System.out.print(b.name));

        System.out.println();
        User user11 = new User(1, "nivelle");
        User user22 = new User(2, "nivelle");
        User user33 = new User(2, "nivelle");
        User user44 = new User(100, "jessy");
        List<User> testList = Lists.newArrayList();
        testList.add(user11);
        testList.add(user22);
        testList.add(user33);
        testList.add(user44);
        Map<String, Long> countMap = testList.stream().collect(Collectors.groupingBy(User::getName, Collectors.counting()));
        System.out.print("分组测试计数测试：" + countMap);
        System.out.println();

        System.out.println("testList=" + testList);
        List<User> distList = testList.stream().filter(distinctByKey(p -> p.getName())).collect(Collectors.toList());
        System.out.print("distList=" + distList);
        System.out.println();

        Map<String, Long> countMap1 = testList.stream().collect(Collectors.groupingBy(User::getName, Collectors.summingLong(User::getAge)));
        System.out.print("countMap1:" + countMap1);


        System.out.println();
        long totalAge = testList.stream().collect(Collectors.summingLong(x -> x.getAge()));
        System.out.print(totalAge);


    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }


    /**
     * 对于Stream中包含的元素使用给定的转换函数进行转换操作，**新生成的Stream只包含转换生成的元素**。
     */
    public static void editAge() {
        Stream userStream = users.stream().map(x -> x.getAge() + 1);

        Stream userStream2 = users.stream().map(x -> x.getName().replace("三", "全蛋"));

        userStream2.forEach(System.out::print);

        //产生操作的数据源的流
        userStream.forEach(System.out::print);
        //不会改变数据源
        users.forEach(System.out::print);
    }


}
