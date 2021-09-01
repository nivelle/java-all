package com.nivelle.core.javacore.jdk.java8;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 归约
 *
 * @author fuxinzhong
 * @date 2021/07/02
 */
public class StreamReduceTest {

    //归约，也称缩减，顾名思义，是把一个流缩减成一个值，能实现对集合求和、求乘积和求最值操作。
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(1, 1, 2);
        // 求和方式1
        Optional<Integer> sum = list.stream().reduce(Integer::sum);
        // 求和方式2
        Optional<Integer> sum2 = list.stream().reduce(Integer::sum);
        // 求和方式3  identity 是初始化值
        Integer sum3 = list.stream().reduce(0, Integer::sum);
        // 求乘积
        Optional<Integer> product = list.stream().reduce((x, y) -> x * y);
        // 求最大值方式1
        Optional<Integer> max = list.stream().reduce((x, y) -> x > y ? x : y);
        // 求最大值写法2
        Integer max2 = list.stream().reduce(1, Integer::max);
        System.out.println("list求和：" + sum.get() + "," + sum2.get() + "," + sum3);
        System.out.println("list求积：" + product.get());
        System.out.println("list求和：" + max.get() + "," + max2);
    }

}
