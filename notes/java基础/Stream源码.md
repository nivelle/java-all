
### java8 函数式接口

#### Function: 类似于 y = F(x)
````java
@FunctionalInterface
public interface Function<R,T> {

    /**
     * 函数式接口
     * 类似于 y = F(x)
     * */
    R apply(T t);
}
````

#### BiFunction: 类似于 z = F(x,y)

````java
@FunctionalInterface
public interface BiFunction<R, T, U> {

    /**
     * 函数式接口
     * 类似于 z = F(x,y)
     * */
    R apply(T t, U u);
}
````

#### ForEach: 遍历处理

````java
@FunctionalInterface
public interface ForEach <T>{

    /**
     * 迭代器遍历
     * @param item 被迭代的每一项
     * */
    void apply(T item);
}

````

#### Comparator: 比较器

````java
@FunctionalInterface
public interface Comparator<T>  {

    /**
     * 比较方法逻辑
     * @param o1    参数1
     * @param o2    参数2
     * @return      返回值大于0 ---> (o1 > o2)
     *              返回值等于0 ---> (o1 = o2)
     *              返回值小于0 ---> (o1 < o2)
     */
    int compare(T o1, T o2);
}

````

#### Predicate: 条件判断

````java
@FunctionalInterface
public interface Predicate <T>{

    /**
     * 函数式接口
     * @param item 迭代的每一项
     * @return true 满足条件
     *          false 不满足条件
     * */
    boolean satisfy(T item);
}
````

#### Supplier：提供初始值

````java
@FunctionalInterface
public interface Supplier<T> {

    /**
     * 提供初始值
     * @return 初始化的值
     * */
    T get();
}
````

#### EvalFunction：stream求值函数

````java
@FunctionalInterface
public interface EvalFunction<T> {

    /**
     * stream流的强制求值方法
     * @return 求值返回一个新的stream
     * */
    MyStream<T> apply();
}
````

#### stream API接口：


````java
/**
 * stream流的API接口
 */
public interface Stream<T> {

    /**
     * 映射 lazy 惰性求值
     * @param mapper 转换逻辑 T->R
     * @return 一个新的流
     * */
    <R> MyStream<R> map(Function<R,T> mapper);

    /**
     * 扁平化 映射 lazy 惰性求值
     * @param mapper 转换逻辑 T->MyStream<R>
     * @return  一个新的流(扁平化之后)
     * */
    <R> MyStream<R> flatMap(Function<? extends MyStream<R>, T> mapper);

    /**
     * 过滤 lazy 惰性求值
     * @param predicate 谓词判断
     * @return 一个新的流，其中元素是满足predicate条件的
     * */
    MyStream<T> filter(Predicate<T> predicate);

    /**
     * 截断 lazy 惰性求值
     * @param n 截断流，只获取部分
     * @return 一个新的流，其中的元素不超过 n
     * */
    MyStream<T> limit(int n);

    /**
     * 去重操作 lazy 惰性求值
     * @return 一个新的流，其中的元素不重复(!equals）
     * */
    MyStream<T> distinct();

    /**
     * 窥视 lazy 惰性求值
     * @return 同一个流，peek不改变流的任何行为
     * */
    MyStream<T> peek(ForEach<T> consumer);

    /**
     * 遍历 eval 强制求值
     * @param consumer 遍历逻辑
     * */
    void forEach(ForEach<T> consumer);

    /**
     * 浓缩 eval 强制求值
     * @param initVal 浓缩时的初始值
     * @param accumulator 浓缩时的 累加逻辑
     * @return 浓缩之后的结果
     * */
    <R> R reduce(R initVal, BiFunction<R, R, T> accumulator);

    /**
     * 收集 eval 强制求值
     * @param collector 传入所需的函数组合子，生成高阶函数
     * @return 收集之后的结果
     * */
    <R, A> R collect(Collector<T,A,R> collector);

    /**
     * 最大值 eval 强制求值
     * @param comparator 大小比较逻辑
     * @return 流中的最大值
     * */
    T max(Comparator<T> comparator);

    /**
     * 最小值 eval 强制求值
     * @param comparator 大小比较逻辑
     * @return 流中的最小值
     * */
    T min(Comparator<T> comparator);

    /**
     * 计数 eval 强制求值
     * @return  当前流的个数
     * */
    int count();

    /**
     * 流中是否存在满足predicate的项
     * @return true 存在 匹配项
     *         false 不存在 匹配项
     * */
    boolean anyMatch(Predicate<? super T> predicate);

    /**
     * 流中的元素是否全部满足predicate
     * @return true 全部满足
     *          false 不全部满足
     * */
    boolean allMatch(Predicate<? super T> predicate);

    /**
     * 返回空的 stream
     * @return 空stream
     * */
    static <T> MyStream<T> makeEmptyStream(){
        // isEnd = true
        return new MyStream.Builder<T>().isEnd(true).build();
    }
}
````